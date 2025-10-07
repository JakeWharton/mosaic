#if defined(__APPLE__) || defined(__linux__)

#include "mosaic-streams-posix.h"
#include "mosaic-tty-posix.h"
#include "mosaic-utils-posix.h"

#include <errno.h>
#include <stdatomic.h>
#include <stdlib.h>
#include <unistd.h>

static atomic_flag global_stream_intercept = ATOMIC_FLAG_INIT;

typedef struct MosaicStreamsImpl {
	int stdin;
	int interrupt_stdin_reader;
	int interrupt_stdin_writer;
	int stdout;
	int stderr;
	int intercepted_stdout_reader;
	int intercepted_stdout_writer;
	int interrupt_intercepted_stdout_reader;
	int interrupt_intercepted_stdout_writer;
	int intercepted_stderr_reader;
	int intercepted_stderr_writer;
	int interrupt_intercepted_stderr_reader;
	int interrupt_intercepted_stderr_writer;
	bool is_test;
} MosaicStreamsImpl;

MosaicStreamsInitResult mosaic_streams_init_internal(int stdin, int stdout, int stderr, bool isTest) {
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
	streams->intercepted_stdout_reader = -1;
	streams->intercepted_stdout_writer = -1;
	streams->interrupt_intercepted_stdout_reader = -1;
	streams->interrupt_intercepted_stdout_writer = -1;
	streams->intercepted_stderr_reader = -1;
	streams->intercepted_stderr_writer = -1;
	streams->interrupt_intercepted_stderr_reader = -1;
	streams->interrupt_intercepted_stderr_writer = -1;
	streams->is_test = isTest;

	result.streams = streams;

	ret:
	return result;

	err_free:
	free(streams);
	goto ret;
}

MosaicStreamsInitResult mosaic_streams_init() {
	return mosaic_streams_init_internal(STDIN_FILENO, STDOUT_FILENO, STDERR_FILENO, false);
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
	return mosaic_utils_read(streams->stdin, streams->interrupt_stdin_reader, buffer, count, NULL);
}

MosaicIoResult mosaic_streams_read_input_with_timeout(MosaicStreams *streams, uint8_t *buffer, int count, int timeoutMillis) {
	struct timeval timeout;
	timeout.tv_sec = 0;
	timeout.tv_usec = timeoutMillis * 1000;

	return mosaic_utils_read(streams->stdin, streams->interrupt_stdin_reader, buffer, count, &timeout);
}

uint32_t mosaic_streams_interrupt_input_read(MosaicStreams *streams) {
	uint8_t space = ' ';
	MosaicIoResult result = mosaic_utils_write(streams->interrupt_stdin_writer, &space, 1);
	return result.error;
}

MosaicIoResult mosaic_streams_write_output(MosaicStreams *streams, uint8_t *buffer, int count) {
	return mosaic_utils_write(streams->stdout, buffer, count);
}

MosaicIoResult mosaic_streams_write_error(MosaicStreams *streams, uint8_t *buffer, int count) {
	return mosaic_utils_write(streams->stderr, buffer, count);
}

MosaicStreamsInterceptResult mosaic_streams_intercept_start(MosaicStreams *streams) {
	MosaicStreamsInterceptResult result = {};

	if (unlikely(streams->is_test)) {
		result.is_test = true;
		goto ret;
	}
	if (unlikely(atomic_flag_test_and_set(&global_stream_intercept))) {
		result.already_bound = true;
		goto ret;
	}

	int outputPipe[2];
	if (unlikely(pipe(outputPipe) != 0)) {
		result.error = errno;
		goto ret;
	}
	int interruptOutputPipe[2];
	if (unlikely(pipe(interruptOutputPipe) != 0)) {
		result.error = errno;
		goto err_output_pipe;
	}
	int errorPipe[2];
	if (unlikely(pipe(errorPipe) != 0)) {
		result.error = errno;
		goto err_interrupt_output_pipe;
	}
	int interruptErrorPipe[2];
	if (unlikely(pipe(interruptErrorPipe) != 0)) {
		result.error = errno;
		goto err_error_pipe;
	}

	// Create copies of stdout and stderr, since the following operation will otherwise close them.
	int dupedOutput = dup(streams->stdout);
	if (unlikely(dupedOutput == -1)) {
		result.error = errno;
		goto err_interrupt_error_pipe;
	}
	int dupedError = dup(streams->stderr);
	if (unlikely(dupedError == -1)) {
		result.error = errno;
		goto err_duped_output;
	}

	// Put the writer-end of the new pipes in place as the stdout and stderr file descriptors.
	if (unlikely(dup2(outputPipe[1], STDOUT_FILENO) == -1)) {
		result.error = errno;
		goto err_duped_error;
	}
	if (unlikely(dup2(errorPipe[1], STDERR_FILENO) == -1)) {
		result.error = errno;
		goto err_swap_output;
	}

	streams->stdout = dupedOutput;
	streams->stderr = dupedError;
	streams->intercepted_stdout_reader = outputPipe[0];
	streams->intercepted_stdout_writer = outputPipe[1];
	streams->interrupt_intercepted_stdout_reader = interruptOutputPipe[0];
	streams->interrupt_intercepted_stdout_writer = interruptOutputPipe[1];
	streams->intercepted_stderr_reader = errorPipe[0];
	streams->intercepted_stderr_writer = errorPipe[1];
	streams->interrupt_intercepted_stderr_reader = interruptErrorPipe[0];
	streams->interrupt_intercepted_stderr_writer = interruptErrorPipe[1];

	ret:
	return result;

	err_swap_output:
	dup2(dupedOutput, STDOUT_FILENO);
	outputPipe[1] = -1;

	err_duped_error:
	close(dupedError);

	err_duped_output:
	close(dupedOutput);

	err_interrupt_error_pipe:
	close(interruptErrorPipe[0]);
	close(interruptErrorPipe[1]);

	err_error_pipe:
	close(errorPipe[0]);
	close(errorPipe[1]);

	err_interrupt_output_pipe:
	close(interruptOutputPipe[0]);
	close(interruptOutputPipe[1]);

	err_output_pipe:
	close(outputPipe[0]);
	if (outputPipe[1] != -1) {
		close(outputPipe[1]);
	}

	atomic_flag_clear(&global_stream_intercept);
	goto ret;
}

uint32_t mosaic_streams_intercept_stop(MosaicStreams *streams) {
	uint32_t result = 0;
	if (streams->intercepted_stdout_reader == -1) {
		goto ret;
	}

	// Move the original FD duplicates back onto the standard FDs. This will close the writer end of
	// their respective intercepting pipes. Finally, close the duplicated FDs.

	bool output_writer_closed = true;
	if (unlikely(dup2(streams->stdout, STDOUT_FILENO) == -1)) {
		result = errno;
		output_writer_closed = false;
	} else {
		if (unlikely(close(streams->stdout) == -1)) {
			result = errno;
		}
		streams->stdout = STDOUT_FILENO;
	}

	bool error_writer_closed = true;
	if (unlikely(dup2(streams->stderr, STDERR_FILENO) == -1 && result == 0)) {
		result = errno;
		error_writer_closed = false;
	} else {
		if (unlikely(close(streams->stderr) == -1 && result == 0)) {
			result = errno;
		}
		streams->stderr = STDERR_FILENO;
	}

	if (unlikely(close(streams->intercepted_stdout_reader) == -1 && result == 0)) {
		result = errno;
	}

	if (unlikely(!output_writer_closed && close(streams->intercepted_stdout_writer) == -1 && result == 0)) {
		result = errno;
	}
	if (unlikely(close(streams->interrupt_intercepted_stdout_reader) == -1 && result == 0)) {
		result = errno;
	}
	if (unlikely(close(streams->interrupt_intercepted_stdout_writer) == -1 && result == 0)) {
		result = errno;
	}

	if (unlikely(close(streams->intercepted_stderr_reader) == -1 && result == 0)) {
		result = errno;
	}
	if (unlikely(!error_writer_closed && close(streams->intercepted_stderr_writer) == -1 && result == 0)) {
		result = errno;
	}
	if (unlikely(close(streams->interrupt_intercepted_stderr_reader) == -1 && result == 0)) {
		result = errno;
	}
	if (unlikely(close(streams->interrupt_intercepted_stderr_writer) == -1 && result == 0)) {
		result = errno;
	}

	streams->intercepted_stdout_reader = -1;
	streams->intercepted_stdout_writer = -1;
	streams->interrupt_intercepted_stdout_reader = -1;
	streams->interrupt_intercepted_stdout_writer = -1;
	streams->intercepted_stderr_reader = -1;
	streams->intercepted_stderr_writer = -1;
	streams->interrupt_intercepted_stderr_reader = -1;
	streams->interrupt_intercepted_stderr_writer = -1;

	atomic_flag_clear(&global_stream_intercept);

	ret:
	return result;
}

MosaicIoResult mosaic_streams_read_intercepted_output(MosaicStreams *streams, uint8_t *buffer, int count) {
	return mosaic_utils_read(
		streams->intercepted_stdout_reader,
		streams->interrupt_intercepted_stdout_reader,
		buffer,
		count,
		NULL
	);
}

MosaicIoResult mosaic_streams_read_intercepted_output_with_timeout(MosaicStreams *streams, uint8_t *buffer, int count, int timeoutMillis) {
	struct timeval timeout;
	timeout.tv_sec = 0;
	timeout.tv_usec = timeoutMillis * 1000;

	return mosaic_utils_read(
		streams->intercepted_stdout_reader,
		streams->interrupt_intercepted_stdout_reader,
		buffer,
		count,
		&timeout
	);
}

uint32_t mosaic_streams_interrupt_intercepted_output_read(MosaicStreams *streams) {
	uint8_t space = ' ';
	MosaicIoResult result = mosaic_utils_write(streams->interrupt_intercepted_stdout_writer, &space, 1);
	return result.error;
}

MosaicIoResult mosaic_streams_read_intercepted_error(MosaicStreams *streams, uint8_t *buffer, int count) {
	return mosaic_utils_read(
		streams->intercepted_stderr_reader,
		streams->interrupt_intercepted_stderr_reader,
		buffer,
		count,
		NULL
	);
}

MosaicIoResult mosaic_streams_read_intercepted_error_with_timeout(MosaicStreams *streams, uint8_t *buffer, int count, int timeoutMillis) {
	struct timeval timeout;
	timeout.tv_sec = 0;
	timeout.tv_usec = timeoutMillis * 1000;

	return mosaic_utils_read(
		streams->intercepted_stderr_reader,
		streams->interrupt_intercepted_stderr_reader,
		buffer,
		count,
		&timeout
	);
}

uint32_t mosaic_streams_interrupt_intercepted_error_read(MosaicStreams *streams) {
	uint8_t space = ' ';
	MosaicIoResult result = mosaic_utils_write(streams->interrupt_intercepted_stderr_writer, &space, 1);
	return result.error;
}

uint32_t mosaic_streams_free(MosaicStreams *streams) {
	uint32_t result = 0;

	if (streams->intercepted_stdout_reader != -1) {
		result = mosaic_streams_intercept_stop(streams);
	}

	free(streams);
	return result;
}

#endif
