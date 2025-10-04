#if defined(__APPLE__) || defined(__linux__)

#include "mosaic-utils-posix.h"

#include <errno.h>
#include <stdlib.h>
#include <sys/select.h>
#include <sys/time.h>
#include <unistd.h>

MosaicIoResult mosaic_utils_read(
	int fd,
	int interruptFd,
	uint8_t *buffer,
	int count,
	struct timeval *timeout
) {
	MosaicIoResult result = {};

	fd_set fds;
	FD_ZERO(&fds);
	FD_SET(fd, &fds);
	FD_SET(interruptFd, &fds);

	int nfds = 1 + ((fd > interruptFd) ? fd : interruptFd);
	if (likely(select(nfds, &fds, NULL, NULL, timeout) >= 0)) {
		if (likely(FD_ISSET(fd, &fds) != 0)) {
			int c = read(fd, buffer, count);
			if (likely(c > 0)) {
				result.count = c;
			} else if (c == 0) {
				result.count = -1; // EOF
			} else {
				goto err;
			}
		} else if (unlikely(FD_ISSET(interruptFd, &fds) != 0)) {
			// Consume the single notification byte to clear the ready state for the next call.
			uint8_t space;
			if (unlikely(read(interruptFd, &space, 1) < 0)) {
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

MosaicIoResult mosaic_utils_write(int writeFd, uint8_t *buffer, int count) {
	MosaicIoResult result = {};

	int written = write(writeFd, buffer, count);
	if (written != -1) {
		result.count = written;
	} else {
		result.error = errno;
	}

	return result;
}

#endif
