#include "mosaic.h"

#if defined(__APPLE__) || defined(__linux__)

#include "cutils.h"
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <termios.h>
#include <unistd.h>

rawModeResult enterRawMode() {
	rawModeResult result = {};

	struct termios *saved = calloc(1, sizeof(struct termios));
	if (unlikely(saved == NULL)) {
		// result.saved is set to 0 which will trigger OOM.
		goto ret;
	}

	if (unlikely(tcgetattr(STDIN_FILENO, saved) != 0)) {
		result.error = errno;
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
		result.error = errno;
		// Try to restore the saved config.
		tcsetattr(STDIN_FILENO, TCSAFLUSH, saved);
		goto err;
	}

	result.saved = saved;

	ret:
	return result;

	err:
	free(saved);
	goto ret;
}

platformError exitRawMode(rawModeConfig *saved) {
	int result = unlikely(tcsetattr(STDIN_FILENO, TCSAFLUSH, saved) != 0)
		? errno
		: 0;
	free(saved);
	return result;
}

#endif
