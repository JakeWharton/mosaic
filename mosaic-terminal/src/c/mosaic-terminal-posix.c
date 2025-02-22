#if defined(__APPLE__) || defined(__linux__)

#include "cutils.h"
#include "mosaic-terminal-posix.h"
#include <errno.h>
#include <stdlib.h>
#include <sys/ioctl.h>
#include <termios.h>
#include <unistd.h>

typedef struct MosaicTerminalImpl {
	int stdinFd;
	int stdoutFd;
	int stderrFd;
	int interruptPipe[2];
	fd_set fds;
	int nfds;
	MosaicTerminalEventCallback *callback;
	struct termios *saved;
} MosaicTerminalImpl;

MosaicTerminalInitResult MosaicTerminalInitWithFd(int stdinFd, MosaicTerminalEventCallback *callback) {
	MosaicTerminalInitResult result = {};

	MosaicTerminalImpl *terminal = calloc(1, sizeof(MosaicTerminalImpl));
	if (unlikely(!terminal)) {
		// result.terminal is set to 0 which will trigger OOM.
		goto ret;
	}

	if (unlikely(pipe(terminal->interruptPipe))) {
		result.error = errno;
		goto err;
	}

	int interruptPipeIn = terminal->interruptPipe[0];
	terminal->stdinFd = stdinFd;
	terminal->stdoutFd = STDOUT_FILENO;
	terminal->stderrFd = STDERR_FILENO;
	// TODO Consider forcing the writer pipe to always be lower than this pipe.
	//  If we did this, we could always assume pipe[0] + 1 is the value for nfds.
	terminal->nfds = ((stdinFd > interruptPipeIn) ? stdinFd : interruptPipeIn) + 1;
	terminal->callback = callback;

	result.terminal = terminal;

	ret:
	return result;

	err:
	free(terminal);
	goto ret;
}

MosaicTerminalInitResult MosaicTerminalInit(MosaicTerminalEventCallback *callback) {
	return MosaicTerminalInitWithFd(STDIN_FILENO, callback);
}

MosaicTerminalResult MosaicTerminalReadInternal(
	MosaicTerminalImpl *terminal,
	uint8_t *buffer,
	int count,
	struct timeval *timeout
) {
	int stdinFd = terminal->stdinFd;
	FD_SET(stdinFd, &terminal->fds);

	int interruptPipeIn = terminal->interruptPipe[0];
	FD_SET(interruptPipeIn, &terminal->fds);

	MosaicTerminalResult result = {};

	// TODO Consider setting up fd_set once in the struct and doing a stack copy here.
	if (likely(select(terminal->nfds, &terminal->fds, NULL, NULL, timeout) >= 0)) {
		if (likely(FD_ISSET(stdinFd, &terminal->fds) != 0)) {
			int c = read(stdinFd, buffer, count);
			if (likely(c > 0)) {
				result.count = c;
			} else if (c == 0) {
				result.count = -1; // EOF
			} else {
				goto err;
			}
		} else if (unlikely(FD_ISSET(interruptPipeIn, &terminal->fds) != 0)) {
			// Consume the single notification byte to clear the ready state for the next call.
			int c = read(interruptPipeIn, buffer, 1);
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

MosaicTerminalResult MosaicTerminalRead(MosaicTerminal *terminal, uint8_t *buffer, int32_t count) {
	return MosaicTerminalReadInternal(terminal, buffer, count, NULL);
}

MosaicTerminalResult MosaicTerminalReadWithTimeout(MosaicTerminal *terminal, uint8_t *buffer, int32_t count, uint32_t timeoutMillis) {
	struct timeval timeout;
	timeout.tv_sec = 0;
	timeout.tv_usec = timeoutMillis * 1000;

	return MosaicTerminalReadInternal(terminal, buffer, count, &timeout);
}

uint32_t MosaicTerminalInterrupt(MosaicTerminal *terminal) {
	int interruptPipeOut = terminal->interruptPipe[1];
	ssize_t written = write(interruptPipeOut, " ", 1);
	return unlikely(written == -1)
		? errno
		: 0;
}

MosaicTerminalResult MosaicTerminalWrite(int fd, uint8_t *buffer, int32_t count) {
	ssize_t written = write(fd, buffer, count);

	MosaicTerminalResult result = {};
	if (written == -1) {
		result.count = written;
	} else {
		result.error = errno;
	}
	return result;
}

MosaicTerminalResult MosaicTerminalWriteOutput(MosaicTerminal *terminal, uint8_t *buffer, int32_t count) {
	return MosaicTerminalWrite(terminal->stdoutFd, buffer, count);
}

MosaicTerminalResult MosaicTerminalWriteError(MosaicTerminal *terminal, uint8_t *buffer, int32_t count) {
	return MosaicTerminalWrite(terminal->stderrFd, buffer, count);
}

uint32_t MosaicTerminalEnableRawMode(MosaicTerminal *terminal) {
	if (terminal->saved) {
		return 0; // Already enabled!
	}

	struct termios *saved = calloc(1, sizeof(struct termios));
	if (unlikely(saved == NULL)) {
		return ENOMEM;
	}

	if (unlikely(tcgetattr(STDIN_FILENO, saved) != 0)) {
		return errno;
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
		uint32_t error = errno;
		// Try to restore the saved config.
		tcsetattr(STDIN_FILENO, TCSAFLUSH, saved);
		return error;
	}

	terminal->saved = saved;
	return 0;
}

//uint32_t MosaicTerminalEnableOutputRedirection(MosaicTerminal *terminal UNUSED) {
//	return 0; // TODO
//}

uint32_t MosaicTerminalEnableResizeEvents(MosaicTerminal *terminal UNUSED) {
	return 0; // TODO
}

MosaicTerminalSizeResult MosaicTerminalCurrentSize(MosaicTerminal *terminal) {
	MosaicTerminalSizeResult result = {};

	struct winsize size;
	if (likely(ioctl(terminal->stdinFd, TIOCGWINSZ, &size) != -1)) {
		result.columns = size.ws_col;
		result.rows = size.ws_row;
		result.width = size.ws_xpixel;
		result.height = size.ws_ypixel;
	} else {
		result.error = errno;
	}

	return result;
}

uint32_t MosaicTerminalFree(MosaicTerminal *terminal) {
	uint32_t result = 0;

	int *interruptPipe = terminal->interruptPipe;
	if (unlikely(close(interruptPipe[0]) != 0)) {
		result = errno;
	}
	if (unlikely(close(interruptPipe[1]) != 0 && result != 0)) {
		result = errno;
	}

	struct termios *saved = terminal->saved;
	if (likely(saved)) {
		if (likely(tcsetattr(STDIN_FILENO, TCSAFLUSH, saved))) {
			result = errno;
		}
		free(saved);
	}

	free(terminal);
	return result;
}

#endif
