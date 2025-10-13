#if defined(_WIN32)

#include "mosaic-streams-windows.h"
#include "mosaic-tty-windows.h"
#include "mosaic-utils-windows.h"

#include <stdatomic.h>
#include <windows.h>

static atomic_flag global_stream_intercept = ATOMIC_FLAG_INIT;

typedef struct MosaicStreamsImpl {
	HANDLE stdin;
	/** Event for overlapped reads of `stdin`, or `INVALID_HANDLE_VALUE` if it's a console (TTY). */
	HANDLE stdin_overlapped_event;
	HANDLE stdin_interrupt_event;
	HANDLE stdout_for_tty;
	HANDLE stdout_for_write;
	HANDLE stderr_for_tty;
	HANDLE stderr_for_write;
	HANDLE intercepted_stdout_read;
	HANDLE intercepted_stdout_write;
	HANDLE intercepted_stdout_overlapped_event;
	HANDLE intercepted_stdout_interrupt_event;
	HANDLE intercepted_stderr_read;
	HANDLE intercepted_stderr_write;
	HANDLE intercepted_stderr_overlapped_event;
	HANDLE intercepted_stderr_interrupt_event;
	bool is_test;
} MosaicStreamsImpl;

static MosaicStreamsTtyResult mosaic_streams_is_tty(HANDLE h) {
	MosaicStreamsTtyResult result = {};
	DWORD type = GetFileType(h);
	if (type == FILE_TYPE_CHAR) {
		// Per https://learn.microsoft.com/en-us/windows/console/console-handles
		// "A console handle presents as FILE_TYPE_CHAR."
		result.is_tty = true;
	} else if (type == FILE_TYPE_UNKNOWN) {
		DWORD error = GetLastError();
		if (error != NO_ERROR) {
			result.error = error;
		}
	}
	return result;
}

MosaicStreamsInitResult mosaic_streams_init_internal(
	HANDLE stdin,
	HANDLE stdoutForTty,
	HANDLE stdoutForWrite,
	HANDLE stderrForTty,
	HANDLE stderrForWrite,
	bool isTest
) {
	MosaicStreamsInitResult result = {};

	MosaicStreamsImpl *streams = calloc(1, sizeof(MosaicStreamsImpl));
	if (unlikely(streams == NULL)) {
		// result.streams is set to 0 which will trigger OOM.
		goto ret;
	}

	HANDLE stdinOverlappedEvent;
	HANDLE stdinInterruptEvent;
	uint32_t stdinEventsResult = mosaic_utils_create_events(&stdinOverlappedEvent, &stdinInterruptEvent);
	if (unlikely(stdinEventsResult)) {
		result.error = stdinEventsResult;
		goto err_free;
	}

	// We need to know if the stdin is a TTY in order to read it correctly.
	MosaicStreamsTtyResult stdinTtyResult = mosaic_streams_is_tty(stdin);
	if (unlikely(stdinTtyResult.error)) {
		result.error = stdinTtyResult.error;
		goto err_stdin_overlapped_event;
	}
	if (stdinTtyResult.is_tty) {
		if (unlikely(CloseHandle(stdinOverlappedEvent) == 0)) {
			result.error = GetLastError();
			goto err_stdin_interrupt_event;
		}
		stdinOverlappedEvent = INVALID_HANDLE_VALUE;
	}

	streams->stdin = stdin;
	streams->stdin_overlapped_event = stdinOverlappedEvent;
	streams->stdin_interrupt_event = stdinInterruptEvent;
	streams->stdout_for_tty = stdoutForTty;
	streams->stdout_for_write = stdoutForWrite;
	streams->stderr_for_tty = stderrForTty;
	streams->stderr_for_write = stderrForWrite;
	streams->is_test = isTest;

	result.streams = streams;

	ret:
	return result;

	err_stdin_overlapped_event:
	CloseHandle(stdinOverlappedEvent);

	err_stdin_interrupt_event:
	CloseHandle(stdinInterruptEvent);

	err_free:
	free(streams);
	goto ret;
}

MOSAIC_EXPORT MosaicStreamsInitResult mosaic_streams_init() {
	HANDLE stdin = GetStdHandle(STD_INPUT_HANDLE);
	if (unlikely(stdin == INVALID_HANDLE_VALUE)) {
		goto err;
	}
	HANDLE stdout = GetStdHandle(STD_OUTPUT_HANDLE);
	if (unlikely(stdout == INVALID_HANDLE_VALUE)) {
		goto err;
	}
	HANDLE stderr = GetStdHandle(STD_ERROR_HANDLE);
	if (unlikely(stderr == INVALID_HANDLE_VALUE)) {
		goto err;
	}

	return mosaic_streams_init_internal(stdin, stdout, stdout, stderr, stderr, false);

	err:
	;
	MosaicStreamsInitResult result = {};
	result.error = GetLastError();
	return result;
}

MOSAIC_EXPORT MosaicStreamsTtyResult mosaic_streams_is_stdin_tty(MosaicStreams *streams) {
	return mosaic_streams_is_tty(streams->stdin);
}

MOSAIC_EXPORT MosaicStreamsTtyResult mosaic_streams_is_stdout_tty(MosaicStreams *streams) {
	return mosaic_streams_is_tty(streams->stdout_for_tty);
}

MOSAIC_EXPORT MosaicStreamsTtyResult mosaic_streams_is_stderr_tty(MosaicStreams *streams) {
	return mosaic_streams_is_tty(streams->stderr_for_tty);
}

MOSAIC_EXPORT MosaicIoResult mosaic_streams_read_input(MosaicStreams *streams, uint8_t *buffer, int count) {
	return mosaic_streams_read_input_with_timeout(streams, buffer, count, INFINITE);
}

MOSAIC_EXPORT MosaicIoResult mosaic_streams_read_input_with_timeout(MosaicStreams *streams, uint8_t *buffer, int count, int timeoutMillis) {
	return streams->stdin_overlapped_event == INVALID_HANDLE_VALUE
		? mosaic_utils_read_console(streams->stdin, streams->stdin_interrupt_event, buffer, count, timeoutMillis)
		: mosaic_utils_read_overlapped(streams->stdin, streams->stdin_overlapped_event, streams->stdin_interrupt_event, buffer, count, timeoutMillis);
}

MOSAIC_EXPORT uint32_t mosaic_streams_interrupt_input_read(MosaicStreams *streams) {
	return likely(SetEvent(streams->stdin_interrupt_event) != 0)
		? 0
		: GetLastError();
}

MOSAIC_EXPORT MosaicIoResult mosaic_streams_write_output(MosaicStreams *streams, uint8_t *buffer, int count) {
	return mosaic_utils_write(streams->stdout_for_write, buffer, count);
}

MOSAIC_EXPORT MosaicIoResult mosaic_streams_write_error(MosaicStreams *streams, uint8_t *buffer, int count) {
	return mosaic_utils_write(streams->stderr_for_write, buffer, count);
}

MOSAIC_EXPORT MosaicStreamsInterceptResult mosaic_streams_intercept_start(MosaicStreams *streams) {
	MosaicStreamsInterceptResult result = {};

	if (unlikely(streams->is_test)) {
		result.is_test = true;
		goto ret;
	}
	if (unlikely(atomic_flag_test_and_set(&global_stream_intercept))) {
		result.already_bound = true;
		goto ret;
	}

	HANDLE interceptedStdoutPipeRead;
	HANDLE interceptedStdoutPipeWrite;
	uint32_t interceptedStdoutPipeResult = mosaic_utils_create_pipe(&interceptedStdoutPipeRead, &interceptedStdoutPipeWrite);
	if (unlikely(interceptedStdoutPipeResult)) {
		result.error = interceptedStdoutPipeResult;
		goto ret;
	}

	HANDLE interceptedStdoutOverlappedEvent;
	HANDLE interceptedStdoutInterruptEvent;
	uint32_t interceptedStdoutEventsResult = mosaic_utils_create_events(&interceptedStdoutOverlappedEvent, &interceptedStdoutInterruptEvent);
	if (unlikely(interceptedStdoutEventsResult)) {
		result.error = interceptedStdoutEventsResult;
		goto err_intercepted_stdout_pipe;
	}

	HANDLE interceptedStderrPipeRead;
	HANDLE interceptedStderrPipeWrite;
	uint32_t interceptedStderrPipeResult = mosaic_utils_create_pipe(&interceptedStderrPipeRead, &interceptedStderrPipeWrite);
	if (unlikely(interceptedStdoutPipeResult)) {
		result.error = interceptedStderrPipeResult;
		goto err_intercepted_stdout_events;
	}

	HANDLE interceptedStderrOverlappedEvent;
	HANDLE interceptedStderrInterruptEvent;
	uint32_t interceptedStderrEventsResult = mosaic_utils_create_events(&interceptedStderrOverlappedEvent, &interceptedStderrInterruptEvent);
	if (unlikely(interceptedStderrEventsResult)) {
		result.error = interceptedStderrEventsResult;
		goto err_intercepted_stderr_pipe;
	}

	if (unlikely(SetStdHandle(STD_OUTPUT_HANDLE, interceptedStdoutPipeWrite) == 0)) {
		result.error = GetLastError();
		goto err_interrupt_stderr_events;
	}
	if (unlikely(SetStdHandle(STD_ERROR_HANDLE, interceptedStderrPipeWrite) == 0)) {
		result.error = GetLastError();
		goto err_set_stdout;
	}

	streams->intercepted_stdout_read = interceptedStdoutPipeRead;
  streams->intercepted_stdout_write = interceptedStdoutPipeWrite;
	streams->intercepted_stdout_overlapped_event = interceptedStdoutOverlappedEvent;
	streams->intercepted_stdout_interrupt_event = interceptedStdoutInterruptEvent;
	streams->intercepted_stderr_read = interceptedStderrPipeRead;
  streams->intercepted_stderr_write = interceptedStderrPipeWrite;
	streams->intercepted_stderr_overlapped_event = interceptedStderrOverlappedEvent;
	streams->intercepted_stderr_interrupt_event = interceptedStderrInterruptEvent;

	ret:
	return result;

	err_set_stdout:
	SetStdHandle(STD_OUTPUT_HANDLE, streams->stdout_for_write);

	err_interrupt_stderr_events:
	CloseHandle(interceptedStderrOverlappedEvent);
	CloseHandle(interceptedStderrInterruptEvent);

	err_intercepted_stderr_pipe:
	CloseHandle(interceptedStderrPipeRead);
	CloseHandle(interceptedStderrPipeWrite);

	err_intercepted_stdout_events:
	CloseHandle(interceptedStdoutOverlappedEvent);
	CloseHandle(interceptedStdoutInterruptEvent);

	err_intercepted_stdout_pipe:
	CloseHandle(interceptedStdoutPipeRead);
	CloseHandle(interceptedStdoutPipeWrite);

	atomic_flag_clear(&global_stream_intercept);
	goto ret;
}

MOSAIC_EXPORT uint32_t mosaic_streams_intercept_stop(MosaicStreams *streams) {
	DWORD result = 0;
	if (streams->intercepted_stdout_read == NULL) {
		goto ret;
	}

	if (unlikely(SetStdHandle(STD_OUTPUT_HANDLE, streams->stdout_for_write) == 0)) {
		result = GetLastError();
	}
	if (unlikely(SetStdHandle(STD_ERROR_HANDLE, streams->stderr_for_write) == 0) && result == 0) {
		result = GetLastError();
	}

	if (unlikely(CloseHandle(streams->intercepted_stdout_read) == 0 && result == 0)) {
		result = GetLastError();
	}
	if (unlikely(CloseHandle(streams->intercepted_stdout_write) == 0 && result == 0)) {
		result = GetLastError();
	}
	if (unlikely(CloseHandle(streams->intercepted_stdout_overlapped_event) == 0 && result == 0)) {
		result = GetLastError();
	}
	if (unlikely(CloseHandle(streams->intercepted_stdout_interrupt_event) == 0 && result == 0)) {
		result = GetLastError();
	}
	if (unlikely(CloseHandle(streams->intercepted_stderr_read) == 0 && result == 0)) {
		result = GetLastError();
	}
	if (unlikely(CloseHandle(streams->intercepted_stderr_write) == 0 && result == 0)) {
		result = GetLastError();
	}
	if (unlikely(CloseHandle(streams->intercepted_stderr_overlapped_event) == 0 && result == 0)) {
		result = GetLastError();
	}
	if (unlikely(CloseHandle(streams->intercepted_stderr_interrupt_event) == 0 && result == 0)) {
		result = GetLastError();
	}

	streams->intercepted_stdout_read = NULL;
  streams->intercepted_stdout_write = NULL;
	streams->intercepted_stdout_overlapped_event = NULL;
	streams->intercepted_stdout_interrupt_event = NULL;
	streams->intercepted_stderr_read = NULL;
  streams->intercepted_stderr_write = NULL;
	streams->intercepted_stderr_overlapped_event = NULL;
	streams->intercepted_stderr_interrupt_event = NULL;

	atomic_flag_clear(&global_stream_intercept);

	ret:
	return result;
}

MOSAIC_EXPORT MosaicIoResult mosaic_streams_read_intercepted_output(MosaicStreams *streams, uint8_t *buffer, int count) {
	return mosaic_streams_read_intercepted_output_with_timeout(streams, buffer, count, INFINITE);
}

MOSAIC_EXPORT MosaicIoResult mosaic_streams_read_intercepted_output_with_timeout(MosaicStreams *streams, uint8_t *buffer, int count, int timeoutMillis) {
	return mosaic_utils_read_overlapped(
		streams->intercepted_stdout_read,
		streams->intercepted_stdout_overlapped_event,
		streams->intercepted_stdout_interrupt_event,
		buffer,
		count,
		timeoutMillis
	);
}

MOSAIC_EXPORT uint32_t mosaic_streams_interrupt_intercepted_output_read(MosaicStreams *streams) {
	return likely(SetEvent(streams->intercepted_stdout_interrupt_event) != 0)
		? 0
		: GetLastError();
}

MOSAIC_EXPORT MosaicIoResult mosaic_streams_read_intercepted_error(MosaicStreams *streams, uint8_t *buffer, int count) {
	return mosaic_streams_read_intercepted_error_with_timeout(streams, buffer, count, INFINITE);
}

MOSAIC_EXPORT MosaicIoResult mosaic_streams_read_intercepted_error_with_timeout(MosaicStreams *streams, uint8_t *buffer, int count, int timeoutMillis) {
	return mosaic_utils_read_overlapped(
		streams->intercepted_stderr_read,
		streams->intercepted_stderr_overlapped_event,
		streams->intercepted_stderr_interrupt_event,
		buffer,
		count,
		timeoutMillis
	);
}

MOSAIC_EXPORT uint32_t mosaic_streams_interrupt_intercepted_error_read(MosaicStreams *streams) {
	return likely(SetEvent(streams->intercepted_stderr_interrupt_event) != 0)
		? 0
		: GetLastError();
}

MOSAIC_EXPORT uint32_t mosaic_streams_free(MosaicStreams *streams) {
	DWORD result = 0;

	if (streams->intercepted_stdout_read != NULL) {
		result = mosaic_streams_intercept_stop(streams);
	}

	if (unlikely(streams->stdin_overlapped_event != INVALID_HANDLE_VALUE && CloseHandle(streams->stdin_overlapped_event) == 0 && result == 0)) {
		result = GetLastError();
	}
	if (unlikely(CloseHandle(streams->stdin_interrupt_event) == 0 && result == 0)) {
		result = GetLastError();
	}

	free(streams);

	return result;
}

#endif
