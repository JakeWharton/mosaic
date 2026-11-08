#if defined(_WIN32)

#include "mosaic-tty-windows.h"
#include "mosaic-utils-windows.h"

#include <assert.h>
#include <stdatomic.h>
#include <windows.h>

MosaicTtyInitResult mosaic_tty_init_with_handles(
	HANDLE conin,
	HANDLE conoutForSize,
	HANDLE conoutForWrite,
	bool conoutForWriteFake
) {
	MosaicTtyInitResult result = {};

	MosaicTtyImpl *tty = calloc(1, sizeof(MosaicTtyImpl));
	if (unlikely(tty == NULL)) {
		// result.tty is set to 0 which will trigger OOM.
		goto ret;
	}

	HANDLE coninInterruptEvent = CreateEvent(NULL, FALSE, FALSE, NULL);
	if (unlikely(coninInterruptEvent == NULL)) {
		result.error = GetLastError();
		goto err_free;
	}

	tty->conin = conin;
	tty->conin_interrupt_event = coninInterruptEvent;
	tty->conout_for_write = conoutForWrite;
	tty->conout_for_write_fake = conoutForWriteFake;
	tty->conout_for_size = conoutForSize;

	result.tty = tty;

	ret:
	return result;

	err_free:
	free(tty);
	goto ret;
}

static _Atomic(MosaicTty *) globalTty;

MOSAIC_EXPORT MosaicTtyInitResult mosaic_tty_init(void) {
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

	result = mosaic_tty_init_with_handles(conin, conout, conout, false);

	MosaicTty *tty = result.tty;
	MosaicTty *expected = NULL;
	if (likely(tty) && !atomic_compare_exchange_strong(&globalTty, &expected, tty)) {
		// We initialized an instance but there already was a global instance.
		result.tty = NULL;
		result.error = mosaic_tty_free(tty);
		result.already_bound = true;
	}

	ret:
	return result;

	err:
	result.error = GetLastError();
	goto ret;
}

MOSAIC_EXPORT void mosaic_tty_set_callback(MosaicTty *tty, MosaicTtyCallback *callback) {
	tty->callback = callback;
}

MOSAIC_EXPORT MosaicIoResult mosaic_tty_read(
	MosaicTty *tty,
	uint8_t *buffer,
	int count
) {
	return mosaic_tty_read_with_timeout(tty, buffer, count, INFINITE);
}

MOSAIC_EXPORT MosaicIoResult mosaic_tty_read_with_timeout(
	MosaicTty *tty,
	uint8_t *buffer,
	int count,
	int timeoutMillis
) {
	MosaicIoResult result = {};

	DWORD waitResult;
	HANDLE waitHandles[2] = { tty->conin, tty->conin_interrupt_event };

	loop:
	waitResult = WaitForMultipleObjects(2, waitHandles, FALSE, timeoutMillis);
	if (likely(waitResult == WAIT_OBJECT_0)) {
		// Peek at pending events without consuming them, so that ReadFile
		// can later consume keyboard/mouse INPUT_RECORDs as VT sequences.
		// FOCUS_EVENT and WINDOW_BUFFER_SIZE_EVENT only exist as INPUT_RECORDs
		// and are handled via callbacks.
		INPUT_RECORD *records = tty->records;
		DWORD recordsRead = 0;
		if (unlikely(!PeekConsoleInputW(tty->conin, records, recordsCount, &recordsRead))) {
			goto err;
		}

		MosaicTtyCallback *callback = tty->callback;
		bool hadKeyboardEvents = false;

		for (DWORD i = 0; i < recordsRead; i++) {
			INPUT_RECORD *rec = &records[i];

			switch (rec->EventType) {
			case KEY_EVENT:
			case MOUSE_EVENT:
				hadKeyboardEvents = true;
				break;

			case FOCUS_EVENT:
				if (callback) {
					callback->onFocus(callback->opaque, rec->Event.FocusEvent.bSetFocus);
				}
				break;

			case WINDOW_BUFFER_SIZE_EVENT:
				if (tty->window_resize_events && callback) {
					CONSOLE_SCREEN_BUFFER_INFO info;
					if (GetConsoleScreenBufferInfo(tty->conout_for_size, &info)) {
						int columns = info.srWindow.Right - info.srWindow.Left + 1;
						int rows = info.srWindow.Bottom - info.srWindow.Top + 1;
						callback->onResize(callback->opaque, columns, rows, 0, 0);
					}
				}
				break;
			}
		}

		if (hadKeyboardEvents) {
			// Read VT sequences for keyboard input. With ENABLE_VIRTUAL_TERMINAL_INPUT,
			// the console encodes key and mouse events as VT escape sequences in UTF-8,
			// including full Unicode support for IME-composed characters.
			// This call internally consumes the keyboard/mouse INPUT_RECORDs.
			DWORD bytesRead = 0;
			BOOL ok = ReadFile(tty->conin, buffer, count, &bytesRead, NULL);
			if (ok && bytesRead > 0) {
				result.count = bytesRead;
			} else if (!ok) {
				result.error = GetLastError();
			}
		}

		if (result.count == 0 && result.error == 0) {
			goto loop;
		}
	} else if (unlikely(waitResult == WAIT_FAILED)) {
		goto err;
	}

	ret:
	return result;

	err:
	result.error = GetLastError();
	goto ret;
}

MOSAIC_EXPORT uint32_t mosaic_tty_interrupt_read(MosaicTty *tty) {
	return likely(SetEvent(tty->conin_interrupt_event) != 0)
		? 0
		: GetLastError();
}

MOSAIC_EXPORT MosaicIoResult mosaic_tty_write(MosaicTty *tty, uint8_t *buffer, int count) {
	return mosaic_utils_write(tty->conout_for_write, buffer, count);
}

MOSAIC_EXPORT uint32_t mosaic_tty_enable_raw_mode(MosaicTty *tty) {
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

MOSAIC_EXPORT uint32_t mosaic_tty_enable_window_resize_events(MosaicTty *tty) {
	tty->window_resize_events = true;
	return 0;
}

MOSAIC_EXPORT MosaicTtyTerminalSizeResult mosaic_tty_current_terminal_size(MosaicTty *tty) {
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

MOSAIC_EXPORT uint32_t mosaic_tty_reset(MosaicTty *tty) {
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

MOSAIC_EXPORT uint32_t mosaic_tty_free(MosaicTty *tty) {
	uint32_t result = 0;

	if (unlikely(CloseHandle(tty->conin_interrupt_event) == 0)) {
		result = GetLastError();
	}

	uint32_t resetResult = mosaic_tty_reset(tty);
	if (resetResult != 0 && result == 0) {
		result = resetResult;
	}

	atomic_store(&globalTty, NULL);
	free(tty);
	return result;
}

#endif
