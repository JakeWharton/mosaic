#if defined(_WIN32)

#include "cutils.h"
#include "mosaic-terminal-windows.h"
#include <windows.h>

const int RECORDS_COUNT = 64;

typedef struct MosaicTerminalImpl {
	HANDLE waitHandles[2];
	HANDLE stdoutWrite;
	HANDLE stderrWrite;
	INPUT_RECORD records[RECORDS_COUNT];
	MosaicTerminalEventCallback *callback;
	bool windowResizeEvents;
	DWORD savedInputMode;
	DWORD savedOutputMode;
	UINT savedOutputCodePage;
} MosaicTerminalImpl;

MosaicTerminalInitResult MosaicTerminalInitWithHandle(HANDLE stdinRead, MosaicTerminalEventCallback *callback) {
	MosaicTerminalInitResult result = {};

	MosaicTerminalImpl *terminal = calloc(1, sizeof(MosaicTerminalImpl));
	if (unlikely(terminal == NULL)) {
		// result.terminal is set to 0 which will trigger OOM.
		goto ret;
	}

	if (unlikely(stdinRead == INVALID_HANDLE_VALUE)) {
		result.error = GetLastError();
		goto err;
	}
	HANDLE stdoutWrite = GetStdHandle(STD_OUTPUT_HANDLE);
	if (unlikely(stdoutWrite == INVALID_HANDLE_VALUE)) {
		result.error = GetLastError();
		goto err;
	}
	HANDLE stderrWrite = GetStdHandle(STD_ERROR_HANDLE);
	if (unlikely(stderrWrite == INVALID_HANDLE_VALUE)) {
		result.error = GetLastError();
		goto err;
	}

	HANDLE interruptEvent = CreateEvent(NULL, FALSE, FALSE, NULL);
	if (unlikely(interruptEvent == NULL)) {
		result.error = GetLastError();
		goto err;
	}

	terminal->waitHandles[0] = stdinRead;
	terminal->waitHandles[1] = interruptEvent;
	terminal->stdoutWrite = stdoutWrite;
	terminal->stderrWrite = stderrWrite;
	terminal->callback = callback;

	result.terminal = terminal;

	ret:
	return result;

	err:
	free(terminal);
	goto ret;
}

MosaicTerminalInitResult MosaicTerminalInit(MosaicTerminalEventCallback *callback) {
	HANDLE stdinRead = GetStdHandle(STD_INPUT_HANDLE);
	return MosaicTerminalInitWithHandle(stdinRead, callback);
}

MosaicTerminalResult MosaicTerminalRead(MosaicTerminal *terminal, uint8_t *buffer, int32_t count) {
	return MosaicTerminalReadWithTimeout(terminal, buffer, count, INFINITE);
}

MosaicTerminalResult MosaicTerminalReadWithTimeout(MosaicTerminal *terminal, uint8_t *buffer, int32_t count, uint32_t timeoutMillis) {
	MosaicTerminalResult result = {};

	DWORD waitResult;

	loop:
	waitResult = WaitForMultipleObjects(2, terminal->waitHandles, FALSE, timeoutMillis);
	if (likely(waitResult == WAIT_OBJECT_0)) {
		INPUT_RECORD *records = terminal->records;
		int recordRequest = RECORDS_COUNT > count ? count : RECORDS_COUNT;
		DWORD recordsRead = 0;
		if (unlikely(!ReadConsoleInputW(terminal->waitHandles[0], records, recordRequest, &recordsRead))) {
			goto err;
		}

		MosaicTerminalEventCallback *callback = terminal->callback;
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
				callback->onFocus(callback->opaque, record.Event.FocusEvent.bSetFocus);
			} else if (record.EventType == WINDOW_BUFFER_SIZE_EVENT && terminal->windowResizeEvents) {
				callback->onResize(
					callback->opaque,
					record.Event.WindowBufferSizeEvent.dwSize.X,
					record.Event.WindowBufferSizeEvent.dwSize.Y,
					0, 0
				);
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

uint32_t MosaicTerminalInterrupt(MosaicTerminal *terminal) {
	return likely(SetEvent(terminal->waitHandles[1]) != 0)
		? 0
		: GetLastError();
}

MosaicTerminalResult MosaicTerminalWrite(HANDLE handle, uint8_t *buffer, int32_t count) {
	MosaicTerminalResult result = {};

	DWORD written;
	if (likely(WriteFile(handle, buffer, count, &written, NULL))) {
		result.count = written;
	} else {
		result.error = GetLastError();
	}

	return result;
}

MosaicTerminalResult MosaicTerminalWriteOutput(MosaicTerminal *terminal, uint8_t *buffer, int32_t count) {
	return MosaicTerminalWrite(terminal->stdoutWrite, buffer, count);
}

MosaicTerminalResult MosaicTerminalWriteError(MosaicTerminal *terminal, uint8_t *buffer, int32_t count) {
	return MosaicTerminalWrite(terminal->stderrWrite, buffer, count);
}

uint32_t MosaicTerminalEnableRawMode(MosaicTerminal *terminal) {
	if (terminal->savedInputMode) {
		return 0; // Already enabled!
	}

	HANDLE stdinRead = terminal->waitHandles[0];
	HANDLE stdoutWrite = terminal->stdoutWrite;

	DWORD savedInputMode;
	DWORD savedOutputMode;
	UINT savedOutputCodePage;
	if (unlikely(GetConsoleMode(stdinRead, &savedInputMode) == 0)) {
		return GetLastError();
	}
	if (unlikely(GetConsoleMode(stdoutWrite, &savedOutputMode) == 0)) {
		return GetLastError();
	}
	if (unlikely((savedOutputCodePage = GetConsoleOutputCP()) == 0)) {
		return GetLastError();
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

	if (unlikely(SetConsoleMode(stdinRead, stdinMode) == 0)) {
		return GetLastError();
	}
	if (unlikely(SetConsoleMode(stdoutWrite, stdoutMode) == 0)) {
		uint32_t error = GetLastError();
		SetConsoleMode(stdinRead, savedInputMode);
		return error;
	}
	if (unlikely(SetConsoleOutputCP(stdoutCp) == 0)) {
		uint32_t error = GetLastError();
		SetConsoleMode(stdinRead, savedInputMode);
		SetConsoleMode(stdoutWrite, savedOutputMode);
		return error;
	}

	terminal->savedInputMode = savedInputMode;
	terminal->savedOutputMode = savedOutputMode;
	terminal->savedOutputCodePage = savedOutputCodePage;
	return 0;
}

//uint32_t MosaicTerminalEnableOutputRedirection(MosaicTerminal *terminal UNUSED) {
//	return 0; // TODO
//}

uint32_t MosaicTerminalEnableResizeEvents(MosaicTerminal *terminal) {
	terminal->windowResizeEvents = true;
	return 0;
}

MosaicTerminalSizeResult MosaicTerminalCurrentSize(MosaicTerminal *terminal) {
	MosaicTerminalSizeResult result = {};

	CONSOLE_SCREEN_BUFFER_INFO info;
	if (likely(GetConsoleScreenBufferInfo(terminal->stdoutWrite, &info))) {
		result.columns = info.dwSize.X;
		result.rows = info.dwSize.Y;
	} else {
		result.error = GetLastError();
	}

	return result;
}

uint32_t MosaicTerminalFree(MosaicTerminal *terminal) {
	uint32_t result = 0;

	if (unlikely(CloseHandle(terminal->waitHandles[1]) == 0)) {
		result = GetLastError();
	}

	// Try to restore all three properties even if some fail.
	if (unlikely(SetConsoleMode(terminal->waitHandles[0], terminal->savedInputMode) == 0) && result == 0) {
		result = GetLastError();
	}
	if (unlikely(SetConsoleMode(terminal->stdoutWrite, terminal->savedOutputMode) == 0 && result == 0)) {
		result = GetLastError();
	}
	if (unlikely(SetConsoleOutputCP(terminal->savedOutputCodePage) == 0 && result == 0)) {
		result = GetLastError();
	}

	free(terminal);
	return result;
}

#endif
