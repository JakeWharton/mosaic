#if defined(_WIN32)

#include "mosaic-tty-windows.h"
#include "mosaic-test-tty.h"

#include "cutils.h"
#include <windows.h>

typedef struct MosaicTestTtyImpl {
	MosaicTty *tty;
	HANDLE stdout_read;
	HANDLE stdout_write;
} MosaicTestTtyImpl;

MosaicTestTtyInitResult testTty_init() {
	MosaicTestTtyInitResult result = {};

	MosaicTestTtyImpl *testTty = calloc(1, sizeof(MosaicTestTtyImpl));
	if (unlikely(testTty == NULL)) {
		// result.testTty is set to 0 which will trigger OOM.
		goto ret;
	}

	HANDLE stdin = CreateFile(TEXT("CONIN$"), GENERIC_READ | GENERIC_WRITE, FILE_SHARE_READ | FILE_SHARE_WRITE, 0, OPEN_EXISTING, 0, 0);
	if (unlikely(stdin == INVALID_HANDLE_VALUE)) {
		result.error = GetLastError();
		goto err;
	}
	if (unlikely(SetConsoleMode(stdin, ENABLE_WINDOW_INPUT | ENABLE_MOUSE_INPUT | ENABLE_EXTENDED_FLAGS) == 0)) {
		result.error = GetLastError();
		goto err;
	}

	// Ensure we don't start with existing records in the buffer.
	FlushConsoleInputBuffer(stdin);

	HANDLE stdoutRead;
	HANDLE stdoutWrite;
	if (unlikely(!CreatePipe(&stdoutRead, &stdoutWrite, NULL, 0))) {
		result.error = GetLastError();
		goto err;
	}

	MosaicTtyInitResult ttyInitResult = tty_initWithHandles(stdin, stdoutWrite, true);
	if (unlikely(ttyInitResult.error)) {
		CloseHandle(stdoutRead);
		CloseHandle(stdoutWrite);
		result.error = ttyInitResult.error;
		goto err;
	}
	testTty->tty = ttyInitResult.tty;
	testTty->stdout_read = stdoutRead;
	testTty->stdout_write = stdoutWrite;

	result.testTty = testTty;

	ret:
	return result;

	err:
	free(testTty);
	goto ret;
}

MosaicTty *testTty_getTty(MosaicTestTty *testTty) {
	return testTty->tty;
}

MosaicTtyIoResult testTty_write(MosaicTestTty *testTty, uint8_t *buffer, int count) {
	MosaicTtyIoResult result = {};

	INPUT_RECORD *records = calloc(count, sizeof(INPUT_RECORD));
	if (!records) {
		result.error = ERROR_NOT_ENOUGH_MEMORY;
		goto ret;
	}
	for (int i = 0; i < count; i++) {
		records[i].EventType = KEY_EVENT;
		records[i].Event.KeyEvent.uChar.AsciiChar = buffer[i];
	}

	DWORD written;
	if (WriteConsoleInputW(testTty->tty->stdin, records, count, &written)) {
		result.count = written;
	} else {
		result.error = GetLastError();
	}

	free(records);

	ret:
	return result;
}

MosaicTtyIoResult testTty_read(MosaicTestTty *testTty, uint8_t *buffer, int count) {
	MosaicTtyIoResult result = {};

	DWORD c;
	if (ReadFile(testTty->stdout_read, buffer, count, &c, NULL)) {
		result.count = c;
	} else {
		result.error = GetLastError();
	}

	return result;
}

static uint32_t writeRecord(HANDLE h, INPUT_RECORD *record) {
	DWORD written;
	if (likely(WriteConsoleInputW(h, record, 1, &written))) {
		if (likely(written == 1)) {
			return 0;
		}
		return ERROR_WRITE_FAULT;
	}
	return GetLastError();
}

uint32_t testTty_focusEvent(MosaicTestTty *testTty, bool focused) {
	INPUT_RECORD record;
	record.EventType = FOCUS_EVENT;
	record.Event.FocusEvent.bSetFocus = focused;
	return writeRecord(testTty->tty->stdin, &record);
}

uint32_t testTty_keyEvent(MosaicTestTty *testTty UNUSED) {
	// TODO
	return 0;
}

uint32_t testTty_mouseEvent(MosaicTestTty *testTty UNUSED) {
	// TODO
	return 0;
}

uint32_t testTty_resizeEvent(MosaicTestTty *testTty, int columns, int rows, int width UNUSED, int height UNUSED) {
	INPUT_RECORD record;
	record.EventType = WINDOW_BUFFER_SIZE_EVENT;
	record.Event.WindowBufferSizeEvent.dwSize.X = columns;
	record.Event.WindowBufferSizeEvent.dwSize.Y = rows;
	return writeRecord(testTty->tty->stdin, &record);
}

uint32_t testTty_free(MosaicTestTty *testTty) {
	uint32_t result = 0;

	if (!CloseHandle(testTty->stdout_read)) {
		result = GetLastError();
	}
	if (!CloseHandle(testTty->stdout_write) && result == 0) {
		result = GetLastError();
	}

	free(testTty);
	return result;
}

#endif
