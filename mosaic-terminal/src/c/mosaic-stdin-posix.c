#include "mosaic.h"

#if defined(__APPLE__) || defined(__linux__)

#include "cutils.h"
#include <errno.h>
#include <fcntl.h>
#include <stdlib.h>
#include <sys/select.h>
#include <time.h>
#include <unistd.h>

typedef struct stdinReaderImpl {
	int stdinFd;
	int pipe[2];
	fd_set fds;
	int nfds;
} stdinReaderImpl;

typedef struct stdinWriterImpl {
	int pipe[2];
	stdinReader *reader;
} stdinWriterImpl;

stdinReaderResult stdinReader_initWithFd(int stdinFd) {
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

	reader->stdinFd = stdinFd;
	// TODO Consider forcing the writer pipe to always be lower than this pipe.
	//  If we did this, we could always assume pipe[0] + 1 is the value for nfds.
	reader->nfds = (stdinFd > reader->pipe[0]) ? stdinFd : reader->pipe[0];

	result.reader = reader;

	ret:
	return result;

	err:
	free(reader);
	goto ret;
}

stdinReaderResult stdinReader_init() {
	return stdinReader_initWithFd(STDIN_FILENO);
}

stdinRead stdinReader_readInternal(
	stdinReader *reader,
	void *buffer,
	int count,
	struct timeval *timeout
) {
	int stdinFd = reader->stdinFd;
	FD_SET(stdinFd, &reader->fds);

	int pipeIn = reader->pipe[0];
	FD_SET(pipeIn, &reader->fds);

	stdinRead result = {};

	// TODO Consider setting up fd_set once in the struct and doing a stack copy here.
	if (likely(select(reader->nfds, &reader->fds, NULL, NULL, timeout) >= 0)) {
		if (likely(FD_ISSET(stdinFd, &reader->fds) != 0)) {
			int c = read(stdinFd, buffer, count);
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

stdinWriterResult stdinWriter_init() {
	stdinWriterResult result = {};

	stdinWriterImpl *writer = calloc(1, sizeof(stdinWriterImpl));
	if (unlikely(writer == NULL)) {
		// result.writer is set to 0 which will trigger OOM.
		goto ret;
	}

	if (unlikely(pipe(writer->pipe)) != 0) {
		result.error = errno;
		goto err;
	}

	stdinReaderResult readerResult = stdinReader_initWithFd(writer->pipe[0]);
	if (unlikely(readerResult.error)) {
		result.error = readerResult.error;
		goto err;
	}
	writer->reader = readerResult.reader;

	result.writer = writer;

	ret:
	return result;

	err:
	free(writer);
	goto ret;
}

stdinReader *stdinWriter_getReader(stdinWriter *writer) {
	return writer->reader;
}

platformError stdinWriter_write(stdinWriter *writer, void *buffer, int count) {
	int pipeOut = writer->pipe[1];
	while (count > 0) {
		int result = write(pipeOut, buffer, count);
		if (unlikely(result == -1)) {
			goto err;
		}
		count = count - result;
	}
	return 0;

	err:
	return errno;
}

platformError stdinWriter_free(stdinWriter *writer) {
	int *pipe = writer->pipe;

	int result = 0;
	if (unlikely(close(pipe[0]) != 0)) {
		result = errno;
	}
	if (unlikely(close(pipe[1]) != 0 && result != 0)) {
		result = errno;
	}
	free(writer);
	return result;
}

#endif
