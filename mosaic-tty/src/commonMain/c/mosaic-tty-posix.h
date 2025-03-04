#ifndef MOSAIC_TTY_POSIX_H
#define MOSAIC_TTY_POSIX_H

#include "mosaic-tty.h"
#include <sys/select.h>

typedef struct MosaicTtyImpl {
	int stdin_read_fd;
	int stdout_write_fd;
	int stderr_write_fd;
	int interrupt_read_fd;
	int interrupt_write_fd;
	MosaicTtyCallback *callback;
	bool sigwinch;
	struct termios *saved;
} MosaicTtyImpl;

MosaicTtyInitResult tty_initWithFds(
	int stdinReadFd,
	int stdoutWriteFd,
	int stderrWriteFd
);

#endif // MOSAIC_TTY_POSIX_H
