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

typedef struct MosaicStreamsInterceptResult {
	bool already_bound;
	bool is_test;
	uint32_t error;
} MosaicStreamsInterceptResult;

MOSAIC_EXPORT MosaicStreamsInitResult mosaic_streams_init(void);
MOSAIC_EXPORT uint32_t mosaic_streams_free(MosaicStreams *streams);

MOSAIC_EXPORT MosaicStreamsTtyResult mosaic_streams_is_stdin_tty(MosaicStreams *streams);
MOSAIC_EXPORT MosaicStreamsTtyResult mosaic_streams_is_stdout_tty(MosaicStreams *streams);
MOSAIC_EXPORT MosaicStreamsTtyResult mosaic_streams_is_stderr_tty(MosaicStreams *streams);

MOSAIC_EXPORT MosaicIoResult mosaic_streams_read_input(MosaicStreams *streams, uint8_t *buffer, int count);
MOSAIC_EXPORT MosaicIoResult mosaic_streams_read_input_with_timeout(MosaicStreams *streams, uint8_t *buffer, int count, int timeoutMillis);
MOSAIC_EXPORT uint32_t mosaic_streams_interrupt_input_read(MosaicStreams *streams);

MOSAIC_EXPORT MosaicIoResult mosaic_streams_write_output(MosaicStreams *streams, uint8_t *buffer, int count);
MOSAIC_EXPORT MosaicIoResult mosaic_streams_write_error(MosaicStreams *streams, uint8_t *buffer, int count);

MOSAIC_EXPORT MosaicStreamsInterceptResult mosaic_streams_intercept_start(MosaicStreams *streams);
MOSAIC_EXPORT uint32_t mosaic_streams_intercept_stop(MosaicStreams *streams);

MOSAIC_EXPORT MosaicIoResult mosaic_streams_read_intercepted_output(MosaicStreams *streams, uint8_t *buffer, int count);
MOSAIC_EXPORT MosaicIoResult mosaic_streams_read_intercepted_output_with_timeout(MosaicStreams *streams, uint8_t *buffer, int count, int timeoutMillis);
MOSAIC_EXPORT uint32_t mosaic_streams_interrupt_intercepted_output_read(MosaicStreams *streams);

MOSAIC_EXPORT MosaicIoResult mosaic_streams_read_intercepted_error(MosaicStreams *streams, uint8_t *buffer, int count);
MOSAIC_EXPORT MosaicIoResult mosaic_streams_read_intercepted_error_with_timeout(MosaicStreams *streams, uint8_t *buffer, int count, int timeoutMillis);
MOSAIC_EXPORT uint32_t mosaic_streams_interrupt_intercepted_error_read(MosaicStreams *streams);

#endif // MOSAIC_STREAMS_H
