#ifndef MOSAIC_STREAMS_H
#define MOSAIC_STREAMS_H

#include "mosaic-utils.h"

#include <stdbool.h>
#include <stdint.h>

typedef struct MosaicStreamsImpl MosaicStreams;

typedef struct MosaicStreamsInitResult {
	MosaicStreams *streams;
	uint32_t error;
} MosaicStreamsInitResult;

typedef struct MosaicStreamsTtyResult {
	bool is_tty;
	uint32_t error;
} MosaicStreamsTtyResult;

MOSAIC_EXPORT MosaicStreamsInitResult mosaic_streams_init();

MOSAIC_EXPORT MosaicStreamsTtyResult mosaic_streams_is_stdin_tty(MosaicStreams *streams);
MOSAIC_EXPORT MosaicStreamsTtyResult mosaic_streams_is_stdout_tty(MosaicStreams *streams);
MOSAIC_EXPORT MosaicStreamsTtyResult mosaic_streams_is_stderr_tty(MosaicStreams *streams);

MOSAIC_EXPORT uint32_t mosaic_streams_free(MosaicStreams *streams);

#endif // MOSAIC_STREAMS_H
