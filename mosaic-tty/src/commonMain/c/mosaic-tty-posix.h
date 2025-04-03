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

MosaicTtyIoResult tty_readInternal(
	int fd,
	int interruptReadFd,
	uint8_t *buffer,
	int count,
	struct timeval *timeout
);

MosaicTtyIoResult tty_writeInternal(int writeFd, uint8_t *buffer, int count);

#endif // MOSAIC_TTY_POSIX_H
