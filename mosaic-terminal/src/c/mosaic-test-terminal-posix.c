#if defined(__APPLE__) || defined(__linux__)

#include "cutils.h"
#include "mosaic-terminal-posix.h"
#include "mosaic-test-terminal.h"
#include <errno.h>
#include <stdlib.h>
#include <unistd.h>

typedef struct MosaicTestTerminalImpl {
	int pipe[2];
	MosaicTerminal *terminal;
} MosaicTestTerminalImpl;

MosaicTestTerminalInitResult MosaicTestTerminalInit(MosaicTerminalEventCallback *callback) {
	MosaicTestTerminalInitResult result = {};

	MosaicTestTerminalImpl *testTerminal = calloc(1, sizeof(MosaicTestTerminalImpl));
	if (unlikely(testTerminal == NULL)) {
		// result.testTerminal is set to 0 which will trigger OOM.
		goto ret;
	}

	if (unlikely(pipe(testTerminal->pipe)) != 0) {
		result.error = errno;
		goto err;
	}

	MosaicTerminalInitResult initResult = MosaicTerminalInitWithFd(testTerminal->pipe[0], callback);
	if (unlikely(initResult.error)) {
		result.error = initResult.error;
		goto err;
	}
	testTerminal->terminal = initResult.terminal;

	result.testTerminal = testTerminal;

	ret:
	return result;

	err:
	free(testTerminal);
	goto ret;
}

MosaicTerminal *MosaicTestTerminalGetTerminal(MosaicTestTerminal *testTerminal) {
	return testTerminal->terminal;
}

uint32_t MosaicTestTerminalWrite(MosaicTestTerminal *testTerminal, int8_t *buffer, int count) {
	int pipeOut = testTerminal->pipe[1];
	while (count > 0) {
		int result = write(pipeOut, buffer, count);
		if (unlikely(result == -1)) {
			goto err;
		}
		count = count - result;
	}
	return 0;

	err:
	return errno;
}

uint32_t MosaicTestTerminalFree(MosaicTestTerminal *testTerminal) {
	int *pipe = testTerminal->pipe;

	int result = 0;
	if (unlikely(close(pipe[0]) != 0)) {
		result = errno;
	}
	if (unlikely(close(pipe[1]) != 0 && result != 0)) {
		result = errno;
	}
	free(testTerminal);
	return result;
}

#endif
