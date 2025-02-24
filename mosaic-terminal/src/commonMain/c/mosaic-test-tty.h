#ifndef MOSAIC_TEST_TTY_H
#define MOSAIC_TEST_TTY_H

#include "mosaic-tty.h"
#include <stdbool.h>
#include <stdint.h>

typedef struct platformInputWriterImpl platformInputWriter;

typedef struct platformInputWriterResult {
	platformInputWriter *writer;
	uint32_t error;
} platformInputWriterResult;

platformInputWriterResult platformInputWriter_init(platformInputCallback *callback);
platformInput *platformInputWriter_getPlatformInput(platformInputWriter *writer);
uint32_t platformInputWriter_write(platformInputWriter *writer, char *buffer, int count);
uint32_t platformInputWriter_focusEvent(platformInputWriter *writer, bool focused);
uint32_t platformInputWriter_keyEvent(platformInputWriter *writer);
uint32_t platformInputWriter_mouseEvent(platformInputWriter *writer);
uint32_t platformInputWriter_resizeEvent(platformInputWriter *writer, int columns, int rows, int width, int height);
uint32_t platformInputWriter_free(platformInputWriter *writer);

#endif // MOSAIC_TEST_TTY_H
