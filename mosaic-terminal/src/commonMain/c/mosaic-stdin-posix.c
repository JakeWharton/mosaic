#include "mosaic.h"

#if defined(__APPLE__) || defined(__linux__)

#include "cutils.h"
#include <errno.h>
#include <fcntl.h>
#include <signal.h>
#include <stdlib.h>
#include <sys/ioctl.h>
#include <sys/select.h>
#include <termios.h>
#include <time.h>
#include <unistd.h>

typedef struct platformInputImpl {
	int stdinFd;
	int pipe[2];
	fd_set fds;
	int nfds;
	platformEventHandler *handler;
	bool sigwinch;
	struct termios *saved;
} platformInputImpl;

typedef struct platformInputWriterImpl {
	int pipe[2];
	platformInput *input;
} platformInputWriterImpl;

platformInputResult platformInput_initWithFd(int stdinFd, platformEventHandler *handler) {
	platformInputResult result = {};

	platformInputImpl *input = calloc(1, sizeof(platformInputImpl));
	if (unlikely(input == NULL)) {
		// result.input is set to 0 which will trigger OOM.
		goto ret;
	}

	if (unlikely(pipe(input->pipe)) != 0) {
		result.error = errno;
		goto err;
	}

	input->stdinFd = stdinFd;
	// TODO Consider forcing the writer pipe to always be lower than this pipe.
	//  If we did this, we could always assume pipe[0] + 1 is the value for nfds.
	input->nfds = ((stdinFd > input->pipe[0]) ? stdinFd : input->pipe[0]) + 1;
	input->handler = handler;

	result.input = input;

	ret:
	return result;

	err:
	free(input);
	goto ret;
}

platformInputResult platformInput_init(platformEventHandler *handler) {
	return platformInput_initWithFd(STDIN_FILENO, handler);
}

stdinRead platformInput_readInternal(
	platformInput *input,
	char *buffer,
	int count,
	struct timeval *timeout
) {
	int stdinFd = input->stdinFd;
	FD_SET(stdinFd, &input->fds);

	int pipeIn = input->pipe[0];
	FD_SET(pipeIn, &input->fds);

	stdinRead result = {};

	// TODO Consider setting up fd_set once in the struct and doing a stack copy here.
	if (likely(select(input->nfds, &input->fds, NULL, NULL, timeout) >= 0)) {
		if (likely(FD_ISSET(stdinFd, &input->fds) != 0)) {
			int c = read(stdinFd, buffer, count);
			if (likely(c > 0)) {
				result.count = c;
			} else if (c == 0) {
				result.count = -1; // EOF
			} else {
				goto err;
			}
		} else if (unlikely(FD_ISSET(pipeIn, &input->fds) != 0)) {
			// Consume the single notification byte to clear the ready state for the next call.
			int c = read(pipeIn, buffer, 1);
			if (unlikely(c < 0)) {
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

stdinRead platformInput_read(platformInput *input, char *buffer, int count) {
	return platformInput_readInternal(input, buffer, count, NULL);
}

stdinRead platformInput_readWithTimeout(
	platformInput *input,
	char *buffer,
	int count,
	int timeoutMillis
) {
	struct timeval timeout;
	timeout.tv_sec = 0;
	timeout.tv_usec = timeoutMillis * 1000;

	return platformInput_readInternal(input, buffer, count, &timeout);
}

uint32_t platformInput_interrupt(platformInput *input) {
	int pipeOut = input->pipe[1];
	int result = write(pipeOut, " ", 1);
	return unlikely(result == -1)
		? errno
		: 0;
}

// TODO Make sure this is only written once. Probably just one instance per process at a time.
platformInput *globalInput = NULL;

void sigwinchHandler(int value UNUSED) {
	struct winsize size;
	if (ioctl(globalInput->stdinFd, TIOCGWINSZ, &size) != -1) {
		platformEventHandler *handler = globalInput->handler;
		handler->onResize(handler->opaque, size.ws_col, size.ws_row, size.ws_xpixel, size.ws_ypixel);
	}
	// TODO Send errno somewhere? Maybe once we get debug logs working.
}

uint32_t platformInput_enableRawMode(platformInput *input) {
	uint32_t result = 0;

	if (unlikely(!input->saved)) {
		goto ret; // Already enabled!
	}

	struct termios *saved = calloc(1, sizeof(struct termios));
	if (unlikely(saved == NULL)) {
		result = ENOMEM;
		goto ret;
	}

	if (unlikely(tcgetattr(STDIN_FILENO, saved) != 0)) {
		result = errno;
		goto err;
	}

	struct termios current = (*saved);

	// Flags as defined by "Raw mode" section of https://linux.die.net/man/3/termios
	current.c_iflag &= ~(BRKINT | ICRNL | IGNBRK | IGNCR | INLCR | ISTRIP | IXON | PARMRK);
	current.c_oflag &= ~(OPOST);
	// Setting ECHONL should be useless here, but it is what is documented for cfmakeraw.
	current.c_lflag &= ~(ECHO | ECHONL | ICANON | IEXTEN | ISIG);
	current.c_cflag &= ~(CSIZE | PARENB);
	current.c_cflag |= (CS8);

	current.c_cc[VMIN] = 1;
	current.c_cc[VTIME] = 0;

	if (unlikely(tcsetattr(STDIN_FILENO, TCSAFLUSH, &current) != 0)) {
		result = errno;
		// Try to restore the saved config.
		tcsetattr(STDIN_FILENO, TCSAFLUSH, saved);
		goto err;
	}

	input->saved = saved;

	ret:
	return result;

	err:
	free(saved);
	goto ret;
}

uint32_t platformInput_enableWindowResizeEvents(platformInput *input) {
	if (input->sigwinch) {
		return 0; // Already installed.
	}

	struct sigaction action;
	action.sa_handler = sigwinchHandler;
	sigemptyset(&action.sa_mask);
	action.sa_flags = 0;

	globalInput = input;
	if (likely(sigaction(SIGWINCH, &action, NULL) == 0)) {
		input->sigwinch = true;
		return 0;
	}
	globalInput = NULL;
	return errno;
}

terminalSizeResult platformInput_currentTerminalSize(platformInput *input) {
	terminalSizeResult result = {};

	struct winsize size;
	if (ioctl(input->stdinFd, TIOCGWINSZ, &size) != -1) {
		result.size.columns = size.ws_col;
		result.size.rows = size.ws_row;
		result.size.width = size.ws_xpixel;
		result.size.height = size.ws_ypixel;
	} else {
		result.error = errno;
	}

	return result;
}

uint32_t platformInput_free(platformInput *input) {
	int *pipe = input->pipe;

	int result = 0;
	if (unlikely(close(pipe[0]) != 0)) {
		result = errno;
	}
	if (unlikely(close(pipe[1]) != 0 && result != 0)) {
		result = errno;
	}
	if (input->sigwinch && signal(SIGWINCH, SIG_DFL) == SIG_ERR && result != 0) {
		result = errno;
	}
	if (input->saved) {
		if (tcsetattr(STDIN_FILENO, TCSAFLUSH, input->saved) && result != 0) {
			result = errno;
		}
		free(input->saved);
	}
	free(input);
	return result;
}

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
