#if defined(_WIN32)

#include "mosaic-streams-windows.h"

typedef struct MosaicStreamsImpl {
	HANDLE stdin;
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

	streams->stdin = stdin;
	streams->stdout = stdout;
	streams->stderr = stderr;

	result.streams = streams;

	ret:
	return result;
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

uint32_t mosaic_streams_free(MosaicStreams *streams) {
	free(streams);
	return 0;
}

#endif
