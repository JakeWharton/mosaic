#if defined(_WIN32)

#include "mosaic-streams-windows.h"
#include "mosaic-tty-windows.h"

typedef struct MosaicStreamsImpl {
	HANDLE stdin;
	HANDLE stdin_interrupt_event;
	HANDLE stdout;
	HANDLE stderr;
} MosaicStreamsImpl;

MosaicStreamsInitResult mosaic_streams_init_internal(HANDLE stdin, HANDLE stdout, HANDLE stderr) {
	MosaicStreamsInitResult result = {};

	MosaicStreamsImpl *streams = calloc(1, sizeof(MosaicStreamsImpl));
	if (unlikely(streams == NULL)) {
		// result.streams is set to 0 which will trigger OOM.
		goto ret;
	}

	HANDLE stdinInterruptEvent = CreateEvent(NULL, FALSE, FALSE, NULL);
	if (unlikely(stdinInterruptEvent == NULL)) {
		result.error = GetLastError();
		goto err_free;
	}

	streams->stdin = stdin;
	streams->stdin_interrupt_event = stdinInterruptEvent;
	streams->stdout = stdout;
	streams->stderr = stderr;

	result.streams = streams;

	ret:
	return result;

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

	return mosaic_streams_init_internal(stdin, stdout, stderr);

	err:
	;
	MosaicStreamsInitResult result = {};
	result.error = GetLastError();
	return result;
}

static MosaicStreamsTtyResult mosaic_streams_is_tty(HANDLE h) {
	MosaicStreamsTtyResult result = {};
	DWORD type = GetFileType(h);
	if (type == FILE_TYPE_CHAR) {
		result.is_tty = true;
	} else if (type == FILE_TYPE_UNKNOWN) {
		DWORD error = GetLastError();
		if (error != NO_ERROR) {
			result.error = error;
		}
	}
	return result;
}

MOSAIC_EXPORT MosaicStreamsTtyResult mosaic_streams_is_stdin_tty(MosaicStreams *streams) {
	return mosaic_streams_is_tty(streams->stdin);
}

MOSAIC_EXPORT MosaicStreamsTtyResult mosaic_streams_is_stdout_tty(MosaicStreams *streams) {
	return mosaic_streams_is_tty(streams->stdout);
}

MOSAIC_EXPORT MosaicStreamsTtyResult mosaic_streams_is_stderr_tty(MosaicStreams *streams) {
	return mosaic_streams_is_tty(streams->stderr);
}

MOSAIC_EXPORT MosaicIoResult mosaic_streams_read_input(MosaicStreams *streams, uint8_t *buffer, int count) {
	return mosaic_streams_read_input_with_timeout(streams, buffer, count, INFINITE);
}

MOSAIC_EXPORT MosaicIoResult mosaic_streams_read_input_with_timeout(MosaicStreams *streams, uint8_t *buffer, int count, int timeoutMillis) {
	MosaicIoResult result = {};

	HANDLE waitHandles[2] = { streams->stdin, streams->stdin_interrupt_event };

	DWORD waitResult = WaitForMultipleObjects(2, waitHandles, FALSE, timeoutMillis);
	if (likely(waitResult == WAIT_OBJECT_0)) {
		DWORD c;
		if (!ReadFile(streams->stdin, buffer, count, &c, NULL)) {
			goto err;
		}
		result.count = c;
	} else if (unlikely(waitResult == WAIT_FAILED)) {
		goto err;
	}
	// Else return a count of 0 because either:
	// - The interrupt event was selected (which auto resets its state).
	// - The user-supplied, non-infinite timeout ran out.

	ret:
	return result;

	err:
	result.error = GetLastError();
	goto ret;
}

MOSAIC_EXPORT uint32_t mosaic_streams_interrupt_input_read(MosaicStreams *streams) {
	return likely(SetEvent(streams->stdin_interrupt_event) != 0)
		? 0
		: GetLastError();
}

MOSAIC_EXPORT MosaicIoResult mosaic_streams_write_output(MosaicStreams *streams, uint8_t *buffer, int count) {
	return mosaic_tty_write_internal(streams->stdout, buffer, count);
}

MOSAIC_EXPORT MosaicIoResult mosaic_streams_write_error(MosaicStreams *streams, uint8_t *buffer, int count) {
	return mosaic_tty_write_internal(streams->stderr, buffer, count);
}

uint32_t mosaic_streams_free(MosaicStreams *streams) {
	DWORD result = 0;

	if (unlikely(CloseHandle(streams->stdin_interrupt_event) == 0)) {
		result = GetLastError();
	}

	free(streams);

	return result;
}

#endif
