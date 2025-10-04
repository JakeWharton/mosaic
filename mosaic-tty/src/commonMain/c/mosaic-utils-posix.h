#ifndef MOSAIC_UTILS_POSIX_H
#define MOSAIC_UTILS_POSIX_H

#include "mosaic-utils.h"

#include <sys/time.h>

MosaicIoResult mosaic_utils_read(
	int fd,
	int interruptFd,
	uint8_t *buffer,
	int count,
	struct timeval *timeout
);

MosaicIoResult mosaic_utils_write(
	int writeFd,
	uint8_t *buffer,
	int count
);

#endif // MOSAIC_UTILS_POSIX_H
