#ifndef MOSAIC_TTY_POSIX_H
#define MOSAIC_TTY_POSIX_H

#include "mosaic-tty.h"
#include <sys/select.h>

typedef struct platformInputImpl {
	int stdinFd;
	int pipe[2];
	fd_set fds;
	int nfds;
	platformEventHandler *handler;
	bool sigwinch;
	struct termios *saved;
} platformInputImpl;

platformInputResult platformInput_initWithFd(int stdinFd, platformEventHandler *handler);

#endif // MOSAIC_TTY_POSIX_H
