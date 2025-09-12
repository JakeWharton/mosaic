#ifndef MOSAIC_STREAMS_WINDOWS_H
#define MOSAIC_STREAMS_WINDOWS_H

#include "mosaic-streams.h"

#include <windows.h>

MosaicStreamsInitResult mosaic_streams_init_internal(HANDLE stdin, HANDLE stdout, HANDLE stderr);

#endif // MOSAIC_STREAMS_WINDOWS_H
