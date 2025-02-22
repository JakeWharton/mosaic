#if defined(_WIN32)

#include "cutils.h"
#include "mosaic-terminal-windows.h"
#include "mosaic-test-terminal.h"
#include <windows.h>

typedef struct MosaicTestTerminalImpl {
	MosaicTerminal *terminal;
} MosaicTestTerminalImpl;

// A single global input writer into which fake data can be sent. Creating and closing this over
// and over eventually produces a failure, so we only do it once per process (since it's test only).
HANDLE testConin = NULL;

MosaicTestTerminalInitResult MosaicTestTerminalInit(MosaicTerminalEventCallback *callback) {
	MosaicTestTerminalInitResult result = {};

	MosaicTestTerminalImpl *testTerminal = calloc(1, sizeof(MosaicTestTerminalImpl));
	if (unlikely(testTerminal == NULL)) {
		// result.writer is set to 0 which will trigger OOM.
		goto ret;
	}

	HANDLE h = testConin;
	if (h == NULL) {
		// When run as a test, GetStdHandle(STD_INPUT_HANDLE) returns a closed handle which does not
		// work. Open a new console input handle for our non-display testing purposes.
		h = CreateFile(TEXT("CONIN$"), GENERIC_READ | GENERIC_WRITE, FILE_SHARE_READ | FILE_SHARE_WRITE, 0, OPEN_EXISTING, 0, 0);
		if (unlikely(h == INVALID_HANDLE_VALUE)) {
			result.error = GetLastError();
			goto err;
		}
		if (unlikely(SetConsoleMode(h, ENABLE_WINDOW_INPUT | ENABLE_MOUSE_INPUT | ENABLE_EXTENDED_FLAGS) == 0)) {
			result.error = GetLastError();
			goto err;
		}
		testConin = h;
	}

	// Ensure we don't start with existing records in the buffer.
	FlushConsoleInputBuffer(testConin);

	MosaicTerminalInitResult initResult = MosaicTerminalInitWithHandle(testConin, callback);
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

uint32_t MosaicTestTerminalWrite(MosaicTestTerminal *testTerminal UNUSED, int8_t *buffer, int count) {
	uint32_t result = 0;
	INPUT_RECORD *records = calloc(count, sizeof(INPUT_RECORD));
	if (!records) {
		result = ERROR_NOT_ENOUGH_MEMORY;
		goto ret;
	}
	for (int i = 0; i < count; i++) {
		records[i].EventType = KEY_EVENT;
		records[i].Event.KeyEvent.uChar.AsciiChar = buffer[i];
	}

	INPUT_RECORD *writeRecord = records;
	while (count > 0) {
		DWORD written;
		if (!WriteConsoleInputW(testConin, writeRecord, count, &written)) {
			goto err;
		}
		count -= (int) written;
		writeRecord += (int) written;
	}

	ret:
	free(records);

	return result;

	err:
	result = GetLastError();
	goto ret;
}

uint32_t MosaicTestTerminalFree(MosaicTestTerminal *testTerminal) {
	free(testTerminal);
	return 0;
}

#endif
