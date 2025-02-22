#ifndef MOSAIC_TERMINAL_WINDOWS_H
#define MOSAIC_TERMINAL_WINDOWS_H

#include "mosaic-terminal.h"
#include <windows.h>

MosaicTerminalInitResult MosaicTerminalInitWithHandle(HANDLE stdinRead, MosaicTerminalEventCallback *callback);

#endif // MOSAIC_TERMINAL_WINDOWS_H
