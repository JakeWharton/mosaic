#ifndef MOSAIC_TTY_POSIX_H
#define MOSAIC_TTY_POSIX_H

#include "mosaic-tty.h"
#include <sys/select.h>

typedef struct MosaicTtyImpl {
	int fd;
	int interrupt_read_fd;
	int interrupt_write_fd;
	MosaicTtyCallback *callback;
	bool sigwinch;
	struct termios *saved;
} MosaicTtyImpl;

MosaicTtyInitResult tty_initWithFd(int fd);

#endif // MOSAIC_TTY_POSIX_H
