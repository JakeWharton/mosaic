#if defined(_WIN32)

#include "mosaic-tty-windows.h"
#include "mosaic-test-tty.h"

#include "cutils.h"
#include <windows.h>

typedef struct MosaicTestTtyImpl {
	MosaicTty *tty;
	bool fake_output_and_error;
} MosaicTestTtyImpl;

// A single global input writer into which fake data can be sent. Creating and closing this over
// and over eventually produces a failure, so we only do it once per process (since it's test only).
HANDLE writerConin = NULL;

MosaicTestTtyInitResult testTty_init(MosaicTtyCallback *callback, bool fakeOutputAndError UNUSED) {
	MosaicTestTtyInitResult result = {};

	MosaicTestTtyImpl *testTty = calloc(1, sizeof(MosaicTestTtyImpl));
	if (unlikely(testTty == NULL)) {
		// result.testTty is set to 0 which will trigger OOM.
		goto ret;
	}

	HANDLE stdin = writerConin;
	if (stdin == NULL) {
		// When run as a test, GetStdHandle(STD_INPUT_HANDLE) returns a closed handle which does not
		// work. Open a new console input handle for our non-display testing purposes.
		stdin = CreateFile(TEXT("CONIN$"), GENERIC_READ | GENERIC_WRITE, FILE_SHARE_READ | FILE_SHARE_WRITE, 0, OPEN_EXISTING, 0, 0);
		if (unlikely(stdin == INVALID_HANDLE_VALUE)) {
			result.error = GetLastError();
			goto err;
		}
		if (unlikely(SetConsoleMode(stdin, ENABLE_WINDOW_INPUT | ENABLE_MOUSE_INPUT | ENABLE_EXTENDED_FLAGS) == 0)) {
			result.error = GetLastError();
			goto err;
		}
		writerConin = stdin;
	}

	// Ensure we don't start with existing records in the buffer.
	FlushConsoleInputBuffer(stdin);

	// TODO Fake these if fakeOutputAndError is true.
	HANDLE stdout = GetStdHandle(STD_OUTPUT_HANDLE);
	HANDLE stderr = GetStdHandle(STD_ERROR_HANDLE);

	MosaicTtyInitResult ttyInitResult = tty_initWithHandles(stdin, stdout, stderr, callback);
	if (unlikely(ttyInitResult.error)) {
		result.error = ttyInitResult.error;
		goto err;
	}
	testTty->tty = ttyInitResult.tty;

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

MosaicTtyIoResult testTty_writeInput(MosaicTestTty *testTty, char *buffer, int count) {
	// TODO Can we just WriteFile to this and get INPUT_RECORDS on the way out?

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

	INPUT_RECORD *writeRecord = records;
	DWORD written;
	if (WriteConsoleInputW(testTty->tty->stdin, writeRecord, count, &written)) {
		result.count = written;
	} else {
		result.error = GetLastError();
	}

	free(records);

	ret:
	return result;
}

MosaicTtyIoResult testTty_readInternal(HANDLE h, char *buffer, int count) {
	MosaicTtyIoResult result = {};

	DWORD read;
	if (ReadFile(h, buffer, count, &read, NULL)) {
		result.count = read;
	} else {
		result.error = GetLastError();
	}

	return result;
}

MosaicTtyIoResult testTty_readOutput(MosaicTestTty *testTty, char *buffer, int count) {
	return testTty_readInternal(testTty->tty->stdout, buffer, count);
}

MosaicTtyIoResult testTty_readError(MosaicTestTty *testTty, char *buffer, int count) {
	return testTty_readInternal(testTty->tty->stderr, buffer, count);
}

uint32_t writeRecord(HANDLE h, INPUT_RECORD *record) {
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

	if (testTty->fake_output_and_error) {
		if (!CloseHandle(testTty->tty->stdout)) {
			result = GetLastError();
		}
		if (!CloseHandle(testTty->tty->stderr) && !result) {
			result = GetLastError();
		}
	}

	free(testTty);

	return result;
}

#endif
