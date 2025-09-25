#if defined(__APPLE__) || defined(__linux__)

#include "mosaic-streams-posix.h"
#include "mosaic-tty-posix.h"

#include <errno.h>
#include <stdlib.h>
#include <unistd.h>

typedef struct MosaicStreamsImpl {
	int stdin;
	int interrupt_stdin_reader;
	int interrupt_stdin_writer;
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

	int interruptPipe[2];
	if (unlikely(pipe(interruptPipe)) != 0) {
		result.error = errno;
		goto err_free;
	}

	streams->stdin = stdin;
	streams->interrupt_stdin_reader = interruptPipe[0];
	streams->interrupt_stdin_writer = interruptPipe[1];
	streams->stdout = stdout;
	streams->stderr = stderr;

	result.streams = streams;

	ret:
	return result;

	err_free:
	free(streams);
	goto ret;
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

MosaicIoResult mosaic_streams_read_input(MosaicStreams *streams, uint8_t *buffer, int count) {
	return mosaic_tty_read_internal(streams->stdin, streams->interrupt_stdin_reader, buffer, count, NULL);
}

MosaicIoResult mosaic_streams_read_input_with_timeout(MosaicStreams *streams, uint8_t *buffer, int count, int timeoutMillis) {
	struct timeval timeout;
	timeout.tv_sec = 0;
	timeout.tv_usec = timeoutMillis * 1000;

	return mosaic_tty_read_internal(streams->stdin, streams->interrupt_stdin_reader, buffer, count, &timeout);
}

uint32_t mosaic_streams_interrupt_input_read(MosaicStreams *streams) {
	uint8_t space = ' ';
	MosaicIoResult result = mosaic_tty_write_internal(streams->interrupt_stdin_writer, &space, 1);
	return result.error;
}

MosaicIoResult mosaic_streams_write_output(MosaicStreams *streams, uint8_t *buffer, int count) {
	return mosaic_tty_write_internal(streams->stdout, buffer, count);
}

MosaicIoResult mosaic_streams_write_error(MosaicStreams *streams, uint8_t *buffer, int count) {
	return mosaic_tty_write_internal(streams->stderr, buffer, count);
}

uint32_t mosaic_streams_free(MosaicStreams *streams) {
	free(streams);
	return 0;
}

#endif
