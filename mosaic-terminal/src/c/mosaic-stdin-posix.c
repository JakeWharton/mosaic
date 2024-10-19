#include "mosaic.h"

#if defined(__APPLE__) || defined(__linux__)

#include "cutils.h"
#include <errno.h>
#include <stdlib.h>
#include <sys/select.h>
#include <time.h>
#include <unistd.h>

typedef struct stdinReaderImpl {
	int pipe[2];
	fd_set fds;
} stdinReaderImpl;

stdinReaderResult stdinReader_init() {
	stdinReaderResult result = {};

	stdinReaderImpl *reader = calloc(1, sizeof(stdinReaderImpl));
	if (unlikely(reader == NULL)) {
		// result.reader is set to 0 which will trigger OOM.
		goto ret;
	}

	if (unlikely(pipe(reader->pipe)) != 0) {
		result.error = errno;
		goto err;
	}

	result.reader = reader;

	ret:
	return result;

	err:
	free(reader);
	goto ret;
}

stdinRead stdinReader_readInternal(
	stdinReader *reader,
	void *buffer,
	int count,
	struct timeval *timeout
) {
	int pipeIn = reader->pipe[0];

	FD_SET(STDIN_FILENO, &reader->fds);
	FD_SET(pipeIn, &reader->fds);

	// Our pipe's FD is always going to be higher than stdin, so use it as the max value.
	int nfds = pipeIn + 1;

	stdinRead result = {};

	if (likely(select(nfds, &reader->fds, NULL, NULL, timeout) >= 0)) {
		if (likely(FD_ISSET(STDIN_FILENO, &reader->fds) != 0)) {
			int c = read(STDIN_FILENO, buffer, count);
			if (likely(c > 0)) {
				result.count = c;
			} else if (c == 0) {
				result.count = -1; // EOF
			} else {
				goto err;
			}
		}
		// Otherwise if the interrupt pipe was selected or we timed out, return a count of 0.
	} else {
		goto err;
	}

	ret:
	return result;

	err:
	result.error = errno;
	goto ret;
}

stdinRead stdinReader_read(stdinReader *reader, void *buffer, int count) {
	return stdinReader_readInternal(reader, buffer, count, NULL);
}

stdinRead stdinReader_readWithTimeout(
	stdinReader *reader,
	void *buffer,
	int count,
	int timeoutMillis
) {
	struct timeval timeout;
	timeout.tv_sec = 0;
	timeout.tv_usec = timeoutMillis * 1000;

	return stdinReader_readInternal(reader, buffer, count, &timeout);
}

platformError stdinReader_interrupt(stdinReader *reader) {
	int pipeOut = reader->pipe[1];
	int result = write(pipeOut, " ", 1);
	return unlikely(result == -1)
		? errno
		: 0;
}

platformError stdinReader_free(stdinReader *reader) {
	int *pipe = reader->pipe;

	int result = 0;
	if (unlikely(close(pipe[0]) != 0)) {
		result = errno;
	}
	if (unlikely(close(pipe[1]) != 0 && result != 0)) {
		result = errno;
	}
	free(reader);
	return result;
}

#endif
