#if defined(__APPLE__) || defined(__linux__)

#include "mosaic-streams-posix.h"

#include <errno.h>
#include <stdlib.h>
#include <unistd.h>

typedef struct MosaicStreamsImpl {
	int stdin;
	int stdout;
	int stderr;
} MosaicStreamsImpl;

MosaicStreamsInitResult mosaic_streams_init_internal(int stdin, int stdout, int stderr) {
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

MosaicStreamsInitResult mosaic_streams_init() {
	return mosaic_streams_init_internal(STDIN_FILENO, STDOUT_FILENO, STDERR_FILENO);
}

static MosaicStreamsTtyResult mosaic_streams_is_tty(int fd) {
	MosaicStreamsTtyResult result = {};
	if (isatty(fd)) {
		result.is_tty = true;
	} else {
		int error = errno;
		if (error != ENOTTY && error != EINVAL) {
			result.error = error;
		}
	}
	return result;
}

MosaicStreamsTtyResult mosaic_streams_is_stdin_tty(MosaicStreams *streams) {
	return mosaic_streams_is_tty(streams->stdin);
}

MosaicStreamsTtyResult mosaic_streams_is_stdout_tty(MosaicStreams *streams) {
	return mosaic_streams_is_tty(streams->stdout);
}

MosaicStreamsTtyResult mosaic_streams_is_stderr_tty(MosaicStreams *streams) {
	return mosaic_streams_is_tty(streams->stderr);
}

uint32_t mosaic_streams_free(MosaicStreams *streams) {
	free(streams);
	return 0;
}

#endif
