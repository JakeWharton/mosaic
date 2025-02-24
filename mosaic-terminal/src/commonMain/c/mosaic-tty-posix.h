#ifndef MOSAIC_TTY_POSIX_H
#define MOSAIC_TTY_POSIX_H

#include "mosaic-tty.h"
#include <sys/select.h>

typedef struct MosaicTtyImpl {
	int stdinFd;
	int pipe[2];
	fd_set fds;
	int nfds;
	MosaicTtyCallback *callback;
	bool sigwinch;
	struct termios *saved;
} MosaicTtyImpl;

MosaicTtyInitResult tty_initWithFd(int stdinFd, MosaicTtyCallback *callback);

#endif // MOSAIC_TTY_POSIX_H
