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

MosaicTtyInitResult mosaic_tty_init_with_fd(int ttyFd);

MosaicIoResult mosaic_tty_read_internal(
	int fd,
	int interruptFd,
	uint8_t *buffer,
	int count,
	struct timeval *timeout
);

MosaicIoResult mosaic_tty_write_internal(int writeFd, uint8_t *buffer, int count);

#endif // MOSAIC_TTY_POSIX_H
