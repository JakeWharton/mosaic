#if defined(_WIN32)

#include "mosaic-streams-windows.h"
#include "mosaic-tty-windows.h"
#include "mosaic-utils-windows.h"

typedef struct MosaicStreamsImpl {
	HANDLE stdin;
	HANDLE stdin_overlapped_event;
	HANDLE stdin_interrupt_event;
	HANDLE stdout;
	HANDLE stderr;
	bool is_test;
} MosaicStreamsImpl;

MosaicStreamsInitResult mosaic_streams_init_internal(HANDLE stdin, HANDLE stdout, HANDLE stderr, bool isTest) {
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

	streams->stdin = stdin;
	streams->stdin_overlapped_event = stdinOverlappedEvent;
	streams->stdin_interrupt_event = stdinInterruptEvent;
	streams->stdout = stdout;
	streams->stderr = stderr;
	streams->is_test = isTest;

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

	return mosaic_streams_init_internal(stdin, stdout, stderr, false);

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
	return mosaic_utils_read_overlapped(streams->stdin, streams->stdin_overlapped_event, streams->stdin_interrupt_event, buffer, count, timeoutMillis);
}

MOSAIC_EXPORT uint32_t mosaic_streams_interrupt_input_read(MosaicStreams *streams) {
	return likely(SetEvent(streams->stdin_interrupt_event) != 0)
		? 0
		: GetLastError();
}

MOSAIC_EXPORT MosaicIoResult mosaic_streams_write_output(MosaicStreams *streams, uint8_t *buffer, int count) {
	return mosaic_utils_write(streams->stdout, buffer, count);
}

MOSAIC_EXPORT MosaicIoResult mosaic_streams_write_error(MosaicStreams *streams, uint8_t *buffer, int count) {
	return mosaic_utils_write(streams->stderr, buffer, count);
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
