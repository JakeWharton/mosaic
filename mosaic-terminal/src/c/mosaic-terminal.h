#ifndef MOSAIC_TERMINAL_H
#define MOSAIC_TERMINAL_H

#include <stdbool.h>
#include <stdint.h>

typedef void MosaicTerminalEventCallbackOnFocus(void *opaque, bool focused);
typedef void MosaicTerminalEventCallbackOnKey(void *opaque); // TODO params
typedef void MosaicTerminalEventCallbackOnMouse(void *opaque); // TODO params
typedef void MosaicTerminalEventCallbackOnResize(void *opaque, uint16_t columns, uint16_t rows, uint16_t width, uint16_t height); // TODO int16_t?
//typedef void MosaicTerminalEventCallbackOnWrite(void *opaque, uint8_t *bytes, int32_t count);

typedef struct MosaicTerminalEventCallback {
	void *opaque;
	MosaicTerminalEventCallbackOnFocus *onFocus;
	MosaicTerminalEventCallbackOnKey *onKey;
	MosaicTerminalEventCallbackOnMouse *onMouse;
	MosaicTerminalEventCallbackOnResize *onResize;
	//MosaicTerminalEventCallbackOnWrite *onStandardOutput;
	//MosaicTerminalEventCallbackOnWrite *onStandardError;
} MosaicTerminalEventCallback;

typedef struct MosaicTerminalImpl MosaicTerminal;

typedef struct MosaicTerminalInitResult {
	MosaicTerminal* terminal;
	uint32_t error;
} MosaicTerminalInitResult;

typedef struct MosaicTerminalResult {
	int32_t count;
	uint32_t error;
} MosaicTerminalResult;

typedef struct MosaicTerminalSizeResult {
	uint16_t columns;
	uint16_t rows;
	uint16_t width;
	uint16_t height;
	uint32_t error;
} MosaicTerminalSizeResult;

MosaicTerminalInitResult MosaicTerminalInit(MosaicTerminalEventCallback *callback);
MosaicTerminalResult MosaicTerminalRead(MosaicTerminal *terminal, uint8_t *buffer, int32_t count);
MosaicTerminalResult MosaicTerminalReadWithTimeout(MosaicTerminal *terminal, uint8_t *buffer, int32_t count, uint32_t timeoutMillis);
uint32_t MosaicTerminalInterrupt(MosaicTerminal *terminal);
//MosaicTerminalResult MosaicTerminalWriteOutput(MosaicTerminal *terminal, uint8_t *buffer, int32_t count);
//MosaicTerminalResult MosaicTerminalWriteError(MosaicTerminal *terminal, uint8_t *buffer, int32_t count);
uint32_t MosaicTerminalEnableRawMode(MosaicTerminal *terminal);
//uint32_t MosaicTerminalEnableOutputRedirection(MosaicTerminal *terminal);
uint32_t MosaicTerminalEnableResizeEvents(MosaicTerminal *terminal);
MosaicTerminalSizeResult MosaicTerminalCurrentSize(MosaicTerminal *terminal);
uint32_t MosaicTerminalFree(MosaicTerminal *terminal);

#endif // MOSAIC_TERMINAL_H
