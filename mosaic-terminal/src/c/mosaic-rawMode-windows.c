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

	if (unlikely(SetConsoleMode(stdin, ENABLE_WINDOW_INPUT | ENABLE_MOUSE_INPUT | ENABLE_EXTENDED_FLAGS) == 0)) {
		result.error = GetLastError();
		goto err;
	}
	if (unlikely(SetConsoleMode(stdout, ENABLE_PROCESSED_OUTPUT | ENABLE_VIRTUAL_TERMINAL_PROCESSING | DISABLE_NEWLINE_AUTO_RETURN | ENABLE_LVB_GRID_WORLDWIDE) == 0)) {
		result.error = GetLastError();
		SetConsoleMode(stdin, saved->input_mode);
		goto err;
	}
	if (unlikely(SetConsoleOutputCP(65001 /* UTF-8 */) == 0)) {
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
