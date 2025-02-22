#ifndef MOSAIC_TEST_TERMINAL_H
#define MOSAIC_TEST_TERMINAL_H

#include "mosaic-terminal.h"

typedef struct MosaicTestTerminalImpl MosaicTestTerminal;

typedef struct MosaicTestTerminalInitResult {
	MosaicTestTerminal* testTerminal;
	uint32_t error;
} MosaicTestTerminalInitResult;

MosaicTestTerminalInitResult MosaicTestTerminalInit(MosaicTerminalEventCallback *callback);
MosaicTerminal *MosaicTestTerminalGetTerminal(MosaicTestTerminal *testTerminal);
uint32_t MosaicTestTerminalWrite(MosaicTestTerminal *testTerminal, int8_t *buffer, int count);
uint32_t MosaicTestTerminalFree(MosaicTestTerminal *testTerminal);

#endif // MOSAIC_TEST_TERMINAL_H
