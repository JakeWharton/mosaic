#include "mosaic.h"

#if defined(_WIN32)

#include "cutils.h"
#include <windows.h>

typedef struct rawModeConfigWindows {
	DWORD input_mode;
	DWORD output_mode;
	UINT output_code_page;
} rawModeConfigWindows;

rawModeResult enterRawMode() {
	rawModeResult result = {};

	HANDLE stdin = GetStdHandle(STD_INPUT_HANDLE);
	if (unlikely(stdin == INVALID_HANDLE_VALUE)) {
		result.error = GetLastError();
		goto ret;
	}
	HANDLE stdout = GetStdHandle(STD_OUTPUT_HANDLE);
	if (unlikely(stdout == INVALID_HANDLE_VALUE)) {
		result.error = GetLastError();
		goto ret;
	}

	rawModeConfigWindows *saved = malloc(sizeof(rawModeConfigWindows));
	if (unlikely(saved == NULL)) {
		// result.saved is set to 0 which will trigger OOM.
		goto ret;
	}

	if (unlikely(GetConsoleMode(stdin, &saved->input_mode) == 0)) {
		result.error = GetLastError();
		goto err;
	}
	if (unlikely(GetConsoleMode(stdout, &saved->output_mode) == 0)) {
		result.error = GetLastError();
		goto err;
	}
	if (unlikely((saved->output_code_page = GetConsoleOutputCP()) == 0)) {
		result.error = GetLastError();
		goto err;
	}

	// https://learn.microsoft.com/en-us/windows/console/setconsolemode
	const int stdinMode = 0
		// Disable quick edit mode.
		| ENABLE_EXTENDED_FLAGS
		// Report changes to the mouse position.
		| ENABLE_MOUSE_INPUT
		// Encode key and mouse events as VT sequences rather than input records.
		| ENABLE_VIRTUAL_TERMINAL_INPUT
		// Report changes to the buffer size.
		| ENABLE_WINDOW_INPUT
		;
	const int stdoutMode = 0
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
	const int stdoutCp = 65001;

	if (unlikely(SetConsoleMode(stdin, stdinMode) == 0)) {
		result.error = GetLastError();
		goto err;
	}
	if (unlikely(SetConsoleMode(stdout, stdoutMode) == 0)) {
		result.error = GetLastError();
		SetConsoleMode(stdin, saved->input_mode);
		goto err;
	}
	if (unlikely(SetConsoleOutputCP(stdoutCp) == 0)) {
		result.error = GetLastError();
		SetConsoleMode(stdin, saved->input_mode);
		SetConsoleMode(stdout, saved->input_mode);
		goto err;
	}

	result.saved = saved;

	ret:
	return result;

	err:
	free(saved);
	goto ret;
}

platformError exitRawMode(rawModeConfig *saved) {
	platformError result = 0;

	HANDLE stdin = GetStdHandle(STD_INPUT_HANDLE);
	if (unlikely(stdin == INVALID_HANDLE_VALUE)) {
		result = GetLastError();
		goto done;
	}
	HANDLE stdout = GetStdHandle(STD_OUTPUT_HANDLE);
	if (unlikely(stdout == INVALID_HANDLE_VALUE)) {
		result = GetLastError();
		goto done;
	}

	// Try to restore all three properties even if some fail.
	if (unlikely(SetConsoleMode(stdin, saved->input_mode) == 0)) {
		result = GetLastError();
	}
	if (unlikely(SetConsoleMode(stdout, saved->output_mode) == 0 && result == 0)) {
		result = GetLastError();
	}
	if (unlikely(SetConsoleOutputCP(saved->output_code_page) == 0 && result == 0)) {
		result = GetLastError();
	}

	done:
	free(saved);
	return result;
}

#endif
