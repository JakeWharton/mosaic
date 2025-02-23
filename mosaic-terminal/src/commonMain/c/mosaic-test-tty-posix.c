#if defined(__APPLE__) || defined(__linux__)

#include "mosaic-tty-posix.h"
#include "mosaic-test-tty.h"

#include "cutils.h"
#include <errno.h>
#include <fcntl.h>
#include <stdlib.h>
#include <unistd.h>

typedef struct platformInputWriterImpl {
	int pipe[2];
	platformInput *input;
} platformInputWriterImpl;

platformInputWriterResult platformInputWriter_init(platformEventHandler *handler) {
	platformInputWriterResult result = {};

	platformInputWriterImpl *writer = calloc(1, sizeof(platformInputWriterImpl));
	if (unlikely(writer == NULL)) {
		// result.writer is set to 0 which will trigger OOM.
		goto ret;
	}

	if (unlikely(pipe(writer->pipe)) != 0) {
		result.error = errno;
		goto err;
	}

	platformInputResult inputResult = platformInput_initWithFd(writer->pipe[0], handler);
	if (unlikely(inputResult.error)) {
		result.error = inputResult.error;
		goto err;
	}
	writer->input = inputResult.input;

	result.writer = writer;

	ret:
	return result;

	err:
	free(writer);
	goto ret;
}

platformInput *platformInputWriter_getPlatformInput(platformInputWriter *writer) {
	return writer->input;
}

uint32_t platformInputWriter_write(platformInputWriter *writer, char *buffer, int count) {
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

uint32_t platformInputWriter_focusEvent(platformInputWriter *writer UNUSED, bool focused UNUSED) {
	// Focus events are delivered through VT sequences.
	return 0;
}

uint32_t platformInputWriter_keyEvent(platformInputWriter *writer UNUSED) {
	// Key events are delivered through VT sequences.
	return 0;
}

uint32_t platformInputWriter_mouseEvent(platformInputWriter *writer UNUSED) {
	// Mouse events are delivered through VT sequences.
	return 0;
}

uint32_t platformInputWriter_resizeEvent(platformInputWriter *writer, int columns, int rows, int width, int height) {
	platformEventHandler *handler = writer->input->handler;
	handler->onResize(handler->opaque, columns, rows, width, height);
	return 0;
}

uint32_t platformInputWriter_free(platformInputWriter *writer) {
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
