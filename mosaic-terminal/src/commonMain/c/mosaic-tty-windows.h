#ifndef MOSAIC_TTY_WINDOWS_H
#define MOSAIC_TTY_WINDOWS_H

#include "mosaic-tty.h"
#include <windows.h>

static const int recordsCount = 64;

typedef struct platformInputImpl {
	HANDLE stdin;
	HANDLE stdout;
	HANDLE waitHandles[2];
	INPUT_RECORD records[recordsCount];
	platformInputCallback *callback;
	bool windowResizeEvents;
	DWORD saved_input_mode;
	DWORD saved_output_mode;
	UINT saved_output_code_page;
} platformInputImpl;

platformInputResult platformInput_initWithHandle(
	HANDLE stdinRead,
	platformInputCallback *callback
);

#endif // MOSAIC_TTY_WINDOWS_H
