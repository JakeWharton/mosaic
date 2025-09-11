#if defined(_WIN32)

#include "mosaic-tty-windows.h"
#include "mosaic-utils.h"

#include <assert.h>
#include <stdatomic.h>
#include <windows.h>

MosaicTtyInitResult tty_initWithHandles(
	HANDLE conin,
	HANDLE conoutForWrite,
	bool conoutForWriteFake,
	HANDLE conoutForSize,
	HANDLE stdin,
	HANDLE stdout,
	HANDLE stderr
) {
	MosaicTtyInitResult result = {};

	MosaicTtyImpl *tty = calloc(1, sizeof(MosaicTtyImpl));
	if (unlikely(tty == NULL)) {
		// result.tty is set to 0 which will trigger OOM.
		goto ret;
	}

	HANDLE interruptEvent = CreateEvent(NULL, FALSE, FALSE, NULL);
	if (unlikely(interruptEvent == NULL)) {
		result.error = GetLastError();
		goto err;
	}

	tty->conin = conin;
	tty->conout_for_write = conoutForWrite;
	tty->conout_for_write_fake = conoutForWriteFake;
	tty->conout_for_size = conoutForSize;
	tty->interrupt_event = interruptEvent;
	tty->stdin = stdin;
	tty->stdout = stdout;
	tty->stderr = stderr;

	result.tty = tty;

	ret:
	return result;

	err:
	free(tty);
	goto ret;
}

static _Atomic(MosaicTty *) globalTty;

MOSAIC_EXPORT MosaicTtyInitResult tty_init() {
	MosaicTtyInitResult result = {};

	HANDLE conin = CreateFile(TEXT("CONIN$"), GENERIC_READ | GENERIC_WRITE, FILE_SHARE_READ | FILE_SHARE_WRITE, 0, OPEN_EXISTING, 0, 0);
	if (unlikely(conin == INVALID_HANDLE_VALUE)) {
		goto err;
	}
	HANDLE conout = CreateFile(TEXT("CONOUT$"), GENERIC_READ | GENERIC_WRITE, FILE_SHARE_READ | FILE_SHARE_WRITE, 0, OPEN_EXISTING, 0, 0);
	if (unlikely(conout == INVALID_HANDLE_VALUE)) {
		goto err;
	}

	HANDLE stdin = GetStdHandle(STD_INPUT_HANDLE);
	if (unlikely(stdin == INVALID_HANDLE_VALUE)) {
		goto err;
	}
	HANDLE stdout = GetStdHandle(STD_OUTPUT_HANDLE);
	if (unlikely(stdout == INVALID_HANDLE_VALUE)) {
		goto err;
	}
	HANDLE stderr = GetStdHandle(STD_ERROR_HANDLE);
	if (unlikely(stderr == INVALID_HANDLE_VALUE)) {
		goto err;
	}

	result = tty_initWithHandles(conin, conout, false, conout, stdin, stdout, stderr);

	MosaicTty *tty = result.tty;
	MosaicTty *expected = NULL;
	if (likely(tty) && !atomic_compare_exchange_strong(&globalTty, &expected, tty)) {
		// We initialized an instance but there already was a global instance.
		result.tty = NULL;
		result.error = tty_free(tty);
		result.already_bound = true;
	}

	ret:
	return result;

	err:
	result.error = GetLastError();
	goto ret;
}

MOSAIC_EXPORT void tty_setCallback(MosaicTty *tty, MosaicTtyCallback *callback) {
	tty->callback = callback;
}

MOSAIC_EXPORT MosaicIoResult tty_read(
	MosaicTty *tty,
	uint8_t *buffer,
	int count
) {
	return tty_readWithTimeout(tty, buffer, count, INFINITE);
}

MOSAIC_EXPORT MosaicIoResult tty_readWithTimeout(
	MosaicTty *tty,
	uint8_t *buffer,
	int count,
	int timeoutMillis
) {
	MosaicIoResult result = {};

	DWORD waitResult;
	HANDLE waitHandles[2] = { tty->conin, tty->interrupt_event };

	loop:
	waitResult = WaitForMultipleObjects(2, waitHandles, FALSE, timeoutMillis);
	if (likely(waitResult == WAIT_OBJECT_0)) {
		INPUT_RECORD *records = tty->records;
		int recordRequest = recordsCount > count ? count : recordsCount;
		DWORD recordsRead = 0;
		if (unlikely(!ReadConsoleInputW(tty->conin, records, recordRequest, &recordsRead))) {
			goto err;
		}

		MosaicTtyCallback *callback = tty->callback;
		int nextBufferIndex = 0;
		for (int i = 0; i < (int) recordsRead; i++) {
			INPUT_RECORD record = records[i];
			if (record.EventType == KEY_EVENT) {
				if (record.Event.KeyEvent.wVirtualKeyCode == 0) {
					buffer[nextBufferIndex++] = record.Event.KeyEvent.uChar.AsciiChar;
				}
				// TODO else other key shit
			} else if (record.EventType == MOUSE_EVENT) {
				// TODO mouse shit
			} else if (record.EventType == FOCUS_EVENT) {
				if (callback) {
					callback->onFocus(callback->opaque, record.Event.FocusEvent.bSetFocus);
				}
			} else if (record.EventType == WINDOW_BUFFER_SIZE_EVENT && tty->window_resize_events) {
				if (callback) {
					CONSOLE_SCREEN_BUFFER_INFO info;
					if (unlikely(!GetConsoleScreenBufferInfo(tty->conout_for_size, &info))) {
						goto err;
					}
					int columns = info.srWindow.Right - info.srWindow.Left + 1;
					int rows = info.srWindow.Bottom - info.srWindow.Top + 1;
					callback->onResize(callback->opaque, columns, rows, 0, 0);
				}
			}
		}

		// Returning 0 would indicate an interrupt, so loop if we haven't read any raw bytes.
		if (nextBufferIndex == 0) {
			goto loop;
		}
		result.count = nextBufferIndex;
	} else if (unlikely(waitResult == WAIT_FAILED)) {
		goto err;
	}
	// Else return a count of 0 because either:
	// - The interrupt event was selected (which auto resets its state).
	// - The user-supplied, non-infinite timeout ran out.

	ret:
	return result;

	err:
	result.error = GetLastError();
	goto ret;
}

MOSAIC_EXPORT uint32_t tty_interruptRead(MosaicTty *tty) {
	return likely(SetEvent(tty->interrupt_event) != 0)
		? 0
		: GetLastError();
}

MOSAIC_EXPORT MosaicIoResult tty_write(MosaicTty *tty, uint8_t *buffer, int count) {
	MosaicIoResult result = {};

	DWORD written;
	if (WriteFile(tty->conout_for_write, buffer, count, &written, NULL)) {
		result.count = written;
	} else {
		result.error = GetLastError();
	}

	return result;
}

MosaicTtyIsTtyResult tty_handle_is_tty(HANDLE h) {
	MosaicTtyIsTtyResult result = {};
	DWORD type = GetFileType(h);
	if (type == FILE_TYPE_CHAR) {
		result.is_tty = true;
	} else if (type == FILE_TYPE_UNKNOWN) {
		DWORD error = GetLastError();
		if (error != NO_ERROR) {
			result.error = error;
		}
	}
	return result;
}

MOSAIC_EXPORT MosaicTtyIsTtyResult tty_stdin_is_tty(MosaicTty *tty) {
	return tty_handle_is_tty(tty->stdin);
}

MOSAIC_EXPORT MosaicTtyIsTtyResult tty_stdout_is_tty(MosaicTty *tty) {
	return tty_handle_is_tty(tty->stdout);
}

MOSAIC_EXPORT MosaicTtyIsTtyResult tty_stderr_is_tty(MosaicTty *tty) {
	return tty_handle_is_tty(tty->stderr);
}

MOSAIC_EXPORT uint32_t tty_enableRawMode(MosaicTty *tty) {
	uint32_t result = 0;

	if (tty->saved_input_mode) {
		goto ret; // Already enabled!
	}

	DWORD input_mode;
	DWORD output_mode;
	UINT output_code_page;
	if (unlikely(GetConsoleMode(tty->conin, &input_mode) == 0)) {
		result = GetLastError();
		goto ret;
	}
	if (!tty->conout_for_write_fake) {
		if (unlikely(GetConsoleMode(tty->conout_for_write, &output_mode) == 0)) {
			result = GetLastError();
			goto ret;
		}
		if (unlikely((output_code_page = GetConsoleOutputCP()) == 0)) {
			result = GetLastError();
			goto ret;
		}
	}

	// https://learn.microsoft.com/en-us/windows/console/setconsolemode
	const int coninMode = 0
		// Disable quick edit mode.
		| ENABLE_EXTENDED_FLAGS
		// Report changes to the mouse position.
		| ENABLE_MOUSE_INPUT
		// Encode key and mouse events as VT sequences rather than input records.
		| ENABLE_VIRTUAL_TERMINAL_INPUT
		// Report changes to the buffer size.
		| ENABLE_WINDOW_INPUT
		;
	const int conoutMode = 0
		// Do not wrap cursor to next line automatically when writing final column.
		| DISABLE_NEWLINE_AUTO_RETURN
		// Allow color sequences to affect characters in all locales.
		| ENABLE_LVB_GRID_WORLDWIDE
		// Process outgoing VT sequences for colors, etc.
		| ENABLE_PROCESSED_OUTPUT
		// Process outgoing VT sequences for cursor movement, etc.
		| ENABLE_VIRTUAL_TERMINAL_PROCESSING
		;
	// UTF-8 per https://learn.microsoft.com/en-us/windows/win32/intl/code-page-identifiers.
	const int conoutCp = 65001;

	if (unlikely(SetConsoleMode(tty->conin, coninMode) == 0)) {
		result = GetLastError();
		goto ret;
	}
	if (!tty->conout_for_write_fake) {
		if (unlikely(SetConsoleMode(tty->conout_for_write, conoutMode) == 0)) {
			result = GetLastError();
			SetConsoleMode(tty->conin, input_mode);
			goto ret;
		}
		if (unlikely(SetConsoleOutputCP(conoutCp) == 0)) {
			result = GetLastError();
			SetConsoleMode(tty->conin, input_mode);
			SetConsoleMode(tty->conout_for_write, output_mode);
			goto ret;
		}
	}

	tty->saved_input_mode = input_mode;
	tty->saved_output_mode = output_mode;
	tty->saved_output_code_page = output_code_page;

	ret:
	return result;
}

MOSAIC_EXPORT uint32_t tty_enableWindowResizeEvents(MosaicTty *tty) {
	tty->window_resize_events = true;
	return 0;
}

MOSAIC_EXPORT MosaicTtyTerminalSizeResult tty_currentTerminalSize(MosaicTty *tty) {
	MosaicTtyTerminalSizeResult result = {};

	CONSOLE_SCREEN_BUFFER_INFO info;
	if (likely(GetConsoleScreenBufferInfo(tty->conout_for_size, &info))) {
		result.columns = info.srWindow.Right - info.srWindow.Left + 1;
		result.rows = info.srWindow.Bottom - info.srWindow.Top + 1;
	} else {
		result.error = GetLastError();
	}

	return result;
}

MOSAIC_EXPORT uint32_t tty_reset(MosaicTty *tty) {
	uint32_t result = 0;

	if (tty->saved_input_mode) {
		if (unlikely(!SetConsoleMode(tty->conin, tty->saved_input_mode))) {
			result = GetLastError();
		}
		if (!tty->conout_for_write_fake) {
			if (unlikely(!SetConsoleMode(tty->conout_for_write, tty->saved_output_mode) && result == 0)) {
				result = GetLastError();
			}
			if (unlikely(!SetConsoleOutputCP(tty->saved_output_code_page) && result == 0)) {
				result = GetLastError();
			}
		}
		tty->saved_input_mode = 0;
		tty->saved_output_mode = 0;
		tty->saved_output_code_page = 0;
	}

	return result;
}

MOSAIC_EXPORT uint32_t tty_free(MosaicTty *tty) {
	uint32_t result = 0;

	if (unlikely(CloseHandle(tty->interrupt_event) == 0)) {
		result = GetLastError();
	}

	uint32_t resetResult = tty_reset(tty);
	if (resetResult != 0 && result == 0) {
		result = resetResult;
	}

	atomic_store(&globalTty, NULL);
	free(tty);
	return result;
}

#endif
