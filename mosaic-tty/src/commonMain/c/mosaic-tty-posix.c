#if defined(__APPLE__) || defined(__linux__)

#include "mosaic-tty-posix.h"

#include "cutils.h"
#include <errno.h>
#include <fcntl.h>
#include <signal.h>
#include <stdatomic.h>
#include <stdlib.h>
#include <sys/ioctl.h>
#include <sys/select.h>
#include <termios.h>
#include <time.h>
#include <unistd.h>

MosaicTtyInitResult tty_initWithFd(int fd) {
	MosaicTtyInitResult result = {};

	MosaicTtyImpl *tty = calloc(1, sizeof(MosaicTtyImpl));
	if (unlikely(tty == NULL)) {
		// result.tty is set to 0 which will trigger OOM.
		goto ret;
	}

	int interrupt_pipe[2];
	if (unlikely(pipe(interrupt_pipe)) != 0) {
		result.error = errno;
		goto err;
	}

	tty->fd = fd;
	tty->interrupt_read_fd = interrupt_pipe[0];
	tty->interrupt_write_fd = interrupt_pipe[1];

	result.tty = tty;

	ret:
	return result;

	err:
	free(tty);
	goto ret;
}

static _Atomic(MosaicTty *) globalTty;

MosaicTtyInitResult tty_init() {
	MosaicTtyInitResult result = {};

	int fd = open("/dev/tty", O_RDWR);
	if (unlikely(fd == -1)) {
		result.error = errno;
		result.no_tty = true;
		goto ret;
	}

	result = tty_initWithFd(fd);

	MosaicTty *tty = result.tty;
	MosaicTty *expected = NULL;
	if (likely(tty) && !atomic_compare_exchange_strong(&globalTty, &expected, tty)) {
		// We initialized an instance but there already was a global instance.
		result.tty = NULL;
		result.error = tty_free(tty);
		result.already_bound = true;
	}

	ret:
	return result;
}

void tty_setCallback(MosaicTty *tty, MosaicTtyCallback *callback) {
	tty->callback = callback;
}

static MosaicTtyIoResult tty_readInternal(
	MosaicTty *tty,
	uint8_t *buffer,
	int count,
	struct timeval *timeout
) {
	MosaicTtyIoResult result = {};

	int ttyFd = tty->fd;
	int interruptReadFd = tty->interrupt_read_fd;

	fd_set fds;
	FD_ZERO(&fds);
	FD_SET(ttyFd, &fds);
	FD_SET(interruptReadFd, &fds);

	int nfds = 1 + ((ttyFd > interruptReadFd) ? ttyFd : interruptReadFd);
	if (likely(select(nfds, &fds, NULL, NULL, timeout) >= 0)) {
		if (likely(FD_ISSET(ttyFd, &fds) != 0)) {
			int c = read(ttyFd, buffer, count);
			if (likely(c > 0)) {
				result.count = c;
			} else if (c == 0) {
				result.count = -1; // EOF
			} else {
				goto err;
			}
		} else if (unlikely(FD_ISSET(interruptReadFd, &fds) != 0)) {
			// Consume the single notification byte to clear the ready state for the next call.
			int c = read(interruptReadFd, buffer, 1);
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

MosaicTtyIoResult tty_read(MosaicTty *tty, uint8_t *buffer, int count) {
	return tty_readInternal(tty, buffer, count, NULL);
}

MosaicTtyIoResult tty_readWithTimeout(
	MosaicTty *tty,
	uint8_t *buffer,
	int count,
	int timeoutMillis
) {
	struct timeval timeout;
	timeout.tv_sec = 0;
	timeout.tv_usec = timeoutMillis * 1000;

	return tty_readInternal(tty, buffer, count, &timeout);
}

MosaicTtyIoResult tty_writeInternal(int writeFd, uint8_t *buffer, int count) {
	MosaicTtyIoResult result = {};

	int written = write(writeFd, buffer, count);
	if (written != -1) {
		result.count = written;
	} else {
		result.error = errno;
	}

	return result;
}

uint32_t tty_interruptRead(MosaicTty *tty) {
	uint8_t space[1] = { ' ' };
	MosaicTtyIoResult result = tty_writeInternal(tty->interrupt_write_fd, space, 1);
	return result.error;
}

MosaicTtyIoResult tty_write(MosaicTty *tty, uint8_t *buffer, int count) {
	return tty_writeInternal(tty->fd, buffer, count);
}

void sigwinchHandler(int value UNUSED) {
	MosaicTty *tty = atomic_load(&globalTty);
	if (likely(tty)) {
		struct winsize size;
		if (ioctl(tty->fd, TIOCGWINSZ, &size) != -1) {
			MosaicTtyCallback *callback = tty->callback;
			if (likely(callback)) {
				callback->onResize(callback->opaque, size.ws_col, size.ws_row, size.ws_xpixel, size.ws_ypixel);
			} else {
				// TODO Send warning somewhere? Maybe once we get debug logs working.
			}
		} else {
			// TODO Send errno somewhere? Maybe once we get debug logs working.
		}
	}
}

uint32_t tty_enableRawMode(MosaicTty *tty) {
	uint32_t result = 0;

	if (unlikely(tty->saved)) {
		goto ret; // Already enabled!
	}

	struct termios *saved = calloc(1, sizeof(struct termios));
	if (unlikely(saved == NULL)) {
		result = ENOMEM;
		goto ret;
	}

	if (unlikely(tcgetattr(tty->fd, saved) != 0)) {
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

	if (unlikely(tcsetattr(tty->fd, TCSAFLUSH, &current) != 0)) {
		result = errno;
		// Try to restore the saved config.
		tcsetattr(tty->fd, TCSAFLUSH, saved);
		goto err;
	}

	tty->saved = saved;

	ret:
	return result;

	err:
	free(saved);
	goto ret;
}

uint32_t tty_enableWindowResizeEvents(MosaicTty *tty) {
	if (tty->sigwinch) {
		return 0; // Already installed.
	}

	struct sigaction action;
	action.sa_handler = sigwinchHandler;
	sigemptyset(&action.sa_mask);
	action.sa_flags = 0;

	if (likely(sigaction(SIGWINCH, &action, NULL) == 0)) {
		tty->sigwinch = true;
		return 0;
	}
	return errno;
}

MosaicTtyTerminalSizeResult tty_currentTerminalSize(MosaicTty *tty) {
	MosaicTtyTerminalSizeResult result = {};

	struct winsize size;
	if (ioctl(tty->fd, TIOCGWINSZ, &size) != -1) {
		result.columns = size.ws_col;
		result.rows = size.ws_row;
		result.width = size.ws_xpixel;
		result.height = size.ws_ypixel;
	} else {
		result.error = errno;
	}

	return result;
}

uint32_t tty_reset(MosaicTty *tty) {
	uint32_t result = 0;

	if (tty->sigwinch) {
		if (unlikely(signal(SIGWINCH, SIG_DFL) == SIG_ERR)) {
			result = errno;
		}
		tty->sigwinch = false;
	}

	if (tty->saved) {
		if (unlikely(tcsetattr(tty->fd, TCSAFLUSH, tty->saved) && result == 0)) {
			result = errno;
		}
		free(tty->saved);
		tty->saved = NULL;
	}

	return result;
}

uint32_t tty_free(MosaicTty *tty) {
	uint32_t result = 0;

	uint32_t resetResult = tty_reset(tty);
	if (resetResult != 0) {
		result = resetResult;
	}

	if (unlikely(close(tty->fd) != 0 && result == 0)) {
		result = errno;
	}

	if (unlikely(close(tty->interrupt_read_fd) != 0 && result == 0)) {
		result = errno;
	}
	if (unlikely(close(tty->interrupt_write_fd) != 0 && result == 0)) {
		result = errno;
	}

	atomic_store(&globalTty, NULL);
	free(tty);
	return result;
}

#endif
