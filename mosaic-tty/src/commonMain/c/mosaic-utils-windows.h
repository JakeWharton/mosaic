#ifndef MOSAIC_UTILS_WINDOWS_H
#define MOSAIC_UTILS_WINDOWS_H

#include "mosaic-utils.h"

uint32_t mosaic_utils_create_events(
	OUT LPHANDLE overlappedEvent,
	OUT LPHANDLE interruptEvent
);

MosaicIoResult mosaic_utils_read_overlapped(
	HANDLE h,
	HANDLE overlappedEvent,
	HANDLE interruptEvent,
	uint8_t *buffer,
	int count,
	int timeoutMillis
);

MosaicIoResult mosaic_utils_write(
	HANDLE h,
	uint8_t *buffer,
	int count
);

#endif // MOSAIC_UTILS_WINDOWS_H
