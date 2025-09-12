#ifndef MOSAIC_TTY_POSIX_H
#define MOSAIC_TTY_POSIX_H

#include "mosaic-tty.h"

#include <sys/select.h>

typedef struct MosaicTtyImpl {
	int fd;
	int interrupt_fd_reader;
	int interrupt_fd_writer;
	MosaicTtyCallback *callback;
	bool sigwinch;
	struct termios *saved;
} MosaicTtyImpl;

MosaicTtyInitResult tty_initWithFd(int ttyFd);

MosaicIoResult tty_readInternal(
	int fd,
	int interruptFd,
	uint8_t *buffer,
	int count,
	struct timeval *timeout
);

MosaicIoResult tty_writeInternal(int writeFd, uint8_t *buffer, int count);

#endif // MOSAIC_TTY_POSIX_H
