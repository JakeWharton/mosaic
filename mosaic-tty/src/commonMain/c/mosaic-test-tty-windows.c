#if defined(_WIN32)

#include "mosaic-tty-windows.h"
#include "mosaic-test-tty.h"

#include "cutils.h"
#include <stdatomic.h>
#include <windows.h>

typedef struct MosaicTestTtyImpl {
	MosaicTty *tty;
	HANDLE conout_pipe_read;
	HANDLE conout_pipe_write;
	atomic_bool interrupt;
} MosaicTestTtyImpl;

static atomic_flag globalTestTty = ATOMIC_FLAG_INIT;

MosaicTestTtyInitResult testTty_init() {
	MosaicTestTtyInitResult result = {};

	MosaicTestTtyImpl *testTty = calloc(1, sizeof(MosaicTestTtyImpl));
	if (unlikely(testTty == NULL)) {
		// result.testTty is set to 0 which will trigger OOM.
		goto ret;
	}

	HANDLE conin = CreateFile(TEXT("CONIN$"), GENERIC_READ | GENERIC_WRITE, FILE_SHARE_READ | FILE_SHARE_WRITE, 0, OPEN_EXISTING, 0, 0);
	if (unlikely(conin == INVALID_HANDLE_VALUE)) {
		result.error = GetLastError();
		goto err_free;
	}
	if (unlikely(SetConsoleMode(conin, ENABLE_WINDOW_INPUT | ENABLE_MOUSE_INPUT | ENABLE_EXTENDED_FLAGS) == 0)) {
		result.error = GetLastError();
		goto err_conin;
	}

	// Ensure we don't start with existing records in the buffer.
	FlushConsoleInputBuffer(conin);

	HANDLE conout = CreateFile(TEXT("CONOUT$"), GENERIC_WRITE, FILE_SHARE_WRITE, 0, OPEN_EXISTING, 0, 0);
	if (unlikely(conout == INVALID_HANDLE_VALUE)) {
		result.error = GetLastError();
		goto err_conin;
	}

	HANDLE conoutPipeRead;
	HANDLE conoutPipeWrite;
	if (unlikely(!CreatePipe(&conoutPipeRead, &conoutPipeWrite, NULL, 0))) {
		result.error = GetLastError();
		goto err_conout;
	}

	MosaicTtyInitResult ttyInitResult = tty_initWithHandles(conin, conoutPipeWrite, true, conout);
	if (unlikely(!ttyInitResult.tty)) {
		result.error = ttyInitResult.error;
		result.already_bound = ttyInitResult.already_bound;
		goto err_conout_pipe;
	}

	testTty->tty = ttyInitResult.tty;
	testTty->conout_pipe_read = conoutPipeRead;
	testTty->conout_pipe_write = conoutPipeWrite;

	result.testTty = testTty;

	if (unlikely(atomic_flag_test_and_set(&globalTestTty))) {
		// We initialized an instance but there already was a global instance.
		result.testTty = NULL;
		result.error = tty_free(ttyInitResult.tty);
		result.already_bound = true;
		goto err_conout;
	}

	ret:
	return result;

	err_conout_pipe:
	CloseHandle(conoutPipeRead);
	CloseHandle(conoutPipeWrite);

	err_conout:
	CloseHandle(conout);

	err_conin:
	CloseHandle(conin);

	err_free:
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
	if (WriteConsoleInputW(testTty->tty->conin, records, count, &written)) {
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

	// Perform a spin loop to check for data or interrupt. We can't do a normal wait because pipes
	// do not signal properly. Since this is only for testing, the busy wait isn't a huge deal.
	for (;;) {
		DWORD available;
		if (!PeekNamedPipe(testTty->conout_pipe_read, NULL, 0, NULL, &available, NULL)) {
			goto err;
		}
		if (available) {
			DWORD c;
			if (!ReadFile(testTty->conout_pipe_read, buffer, count, &c, NULL)) {
				goto err;
			}
			result.count = c;
			break;
		}
		if (atomic_load(&testTty->interrupt)) {
			atomic_store(&testTty->interrupt, false);
			// result.count will be 0
			break;
		}
	}

	ret:
	return result;

	err:
	result.error = GetLastError();
	goto ret;
}

uint32_t testTty_interruptRead(MosaicTestTty *testTty) {
	atomic_store(&testTty->interrupt, true);
	return 0;
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
	return writeRecord(testTty->tty->conin, &record);
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
	return writeRecord(testTty->tty->conin, &record);
}

uint32_t testTty_free(MosaicTestTty *testTty) {
	uint32_t result = 0;

	if (!CloseHandle(testTty->conout_pipe_read)) {
		result = GetLastError();
	}
	if (!CloseHandle(testTty->conout_pipe_write) && result == 0) {
		result = GetLastError();
	}

	atomic_flag_clear(&globalTestTty);
	free(testTty);
	return result;
}

#endif
