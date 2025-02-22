#ifndef MOSAIC_TERMINAL_POSIX_H
#define MOSAIC_TERMINAL_POSIX_H

#include "mosaic-terminal.h"

MosaicTerminalInitResult MosaicTerminalInitWithFd(int stdinFd, MosaicTerminalEventCallback *callback);

#endif // MOSAIC_TERMINAL_POSIX_H
