#ifndef MOSAIC_STREAMS_POSIX_H
#define MOSAIC_STREAMS_POSIX_H

#include "mosaic-streams.h"

MosaicStreamsInitResult mosaic_streams_init_internal(int stdin, int stdout, int stderr, bool isTest);

#endif // MOSAIC_STREAMS_POSIX_H
