#include "mosaic.h"

#if defined(_WIN32)

#include "cutils.h"
#include <assert.h>
#include <windows.h>

const int recordsCount = 64;

typedef struct platformInputImpl {
	HANDLE stdin;
	HANDLE stdout;
	HANDLE waitHandles[2];
	INPUT_RECORD records[recordsCount];
	platformEventHandler *handler;
	bool windowResizeEvents;
	DWORD saved_input_mode;
	DWORD saved_output_mode;
	UINT saved_output_code_page;
} platformInputImpl;

typedef struct platformInputWriterImpl {
	platformInput *input;
} platformInputWriterImpl;

platformInputResult platformInput_initWithHandle(
	HANDLE stdinRead,
	platformEventHandler *handler
) {
	platformInputResult result = {};

	platformInputImpl *input = calloc(1, sizeof(platformInputImpl));
	if (unlikely(input == NULL)) {
		// result.input is set to 0 which will trigger OOM.
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

	HANDLE interruptEvent = CreateEvent(NULL, FALSE, FALSE, NULL);
	if (unlikely(interruptEvent == NULL)) {
		result.error = GetLastError();
		goto err;
	}

	input->stdin = stdinRead;
	input->stdout = stdoutWrite;
	input->waitHandles[0] = stdinRead;
	input->waitHandles[1] = interruptEvent;
	input->handler = handler;

	result.input = input;

	ret:
	return result;

	err:
	free(input);
	goto ret;
}

platformInputResult platformInput_init(platformEventHandler *handler) {
	HANDLE h = GetStdHandle(STD_INPUT_HANDLE);
	return platformInput_initWithHandle(h, handler);
}

stdinRead platformInput_read(
	platformInput *input,
	char *buffer,
	int count
) {
	return platformInput_readWithTimeout(input, buffer, count, INFINITE);
}

stdinRead platformInput_readWithTimeout(
	platformInput *input,
	char *buffer,
	int count,
	int timeoutMillis
) {
	stdinRead result = {};

	DWORD waitResult;

	loop:
	waitResult = WaitForMultipleObjects(2, input->waitHandles, FALSE, timeoutMillis);
	if (likely(waitResult == WAIT_OBJECT_0)) {
		INPUT_RECORD *records = input->records;
		int recordRequest = recordsCount > count ? count : recordsCount;
		DWORD recordsRead = 0;
		if (unlikely(!ReadConsoleInputW(input->waitHandles[0], records, recordRequest, &recordsRead))) {
			goto err;
		}

		platformEventHandler *handler = input->handler;
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
				handler->onFocus(handler->opaque, record.Event.FocusEvent.bSetFocus);
			} else if (record.EventType == WINDOW_BUFFER_SIZE_EVENT && input->windowResizeEvents) {
				handler->onResize(
					handler->opaque,
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

uint32_t platformInput_interrupt(platformInput *input) {
	return likely(SetEvent(input->waitHandles[1]) != 0)
		? 0
		: GetLastError();
}

uint32_t platformInput_enableRawMode(platformInput *input) {
	uint32_t result = 0;

	if (input->saved_input_mode) {
		goto ret; // Already enabled!
	}

	DWORD input_mode;
	DWORD output_mode;
	UINT output_code_page;
	if (unlikely(GetConsoleMode(input->stdin, &input_mode) == 0)) {
		result = GetLastError();
		goto ret;
	}
	if (unlikely(GetConsoleMode(input->stdout, &output_mode) == 0)) {
		result = GetLastError();
		goto ret;
	}
	if (unlikely((output_code_page = GetConsoleOutputCP()) == 0)) {
		result = GetLastError();
		goto ret;
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

	if (unlikely(SetConsoleMode(input->stdin, stdinMode) == 0)) {
		result = GetLastError();
		goto ret;
	}
	if (unlikely(SetConsoleMode(input->stdout, stdoutMode) == 0)) {
		result = GetLastError();
		SetConsoleMode(input->stdin, input_mode);
		goto ret;
	}
	if (unlikely(SetConsoleOutputCP(stdoutCp) == 0)) {
		result = GetLastError();
		SetConsoleMode(input->stdin, input_mode);
		SetConsoleMode(input->stdout, input_mode);
		goto ret;
	}

	input->saved_input_mode = input_mode;
	input->saved_output_mode = output_mode;
	input->saved_output_code_page = output_code_page;

	ret:
	return result;
}

uint32_t platformInput_enableWindowResizeEvents(platformInput *input) {
	input->windowResizeEvents = true;
	return 0;
}

terminalSizeResult platformInput_currentTerminalSize(platformInput *input) {
	terminalSizeResult result = {};

	CONSOLE_SCREEN_BUFFER_INFO info;
	if (likely(GetConsoleScreenBufferInfo(input->stdout, &info))) {
		result.size.columns = info.dwSize.X;
		result.size.rows = info.dwSize.Y;
	} else {
		result.error = GetLastError();
	}

	return result;
}

uint32_t platformInput_free(platformInput *input) {
	uint32_t result = 0;

	if (unlikely(CloseHandle(input->waitHandles[1]) == 0)) {
		result = GetLastError();
	}

	if (input->saved_input_mode) {
		if (unlikely(!SetConsoleMode(input->stdin, input->saved_input_mode) && result == 0)) {
			result = GetLastError();
		}
		if (unlikely(!SetConsoleMode(input->stdout, input->saved_output_mode) && result == 0)) {
			result = GetLastError();
		}
		if (unlikely(!SetConsoleOutputCP(input->saved_output_code_page) && result == 0)) {
			result = GetLastError();
		}
	}

	free(input);
	return result;
}

// A single global input writer into which fake data can be sent. Creating and closing this over
// and over eventually produces a failure, so we only do it once per process (since it's test only).
HANDLE writerConin = NULL;

platformInputWriterResult platformInputWriter_init(platformEventHandler *handler) {
	platformInputWriterResult result = {};

	platformInputWriterImpl *writer = calloc(1, sizeof(platformInputWriterImpl));
	if (unlikely(writer == NULL)) {
		// result.writer is set to 0 which will trigger OOM.
		goto ret;
	}

	HANDLE h = writerConin;
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
		writerConin = h;
	}

	// Ensure we don't start with existing records in the buffer.
	FlushConsoleInputBuffer(writerConin);

	platformInputResult inputResult = platformInput_initWithHandle(writerConin, handler);
	if (unlikely(inputResult.error)) {
		result.error = inputResult.error;
		goto err;
	}
	writer->input = inputResult.input;

	result.writer = writer;

	ret:
	return result;

	err:
	free(writer);
	goto ret;
}

platformInput *platformInputWriter_getPlatformInput(platformInputWriter *writer) {
	return writer->input;
}

uint32_t platformInputWriter_write(platformInputWriter *writer UNUSED, char *buffer, int count) {
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
		if (!WriteConsoleInputW(writerConin, writeRecord, count, &written)) {
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

uint32_t writeRecord(INPUT_RECORD *record) {
	DWORD written;
	if (likely(WriteConsoleInputW(writerConin, record, 1, &written))) {
		if (likely(written == 1)) {
			return 0;
		}
		return ERROR_WRITE_FAULT;
	}
	return GetLastError();
}

uint32_t platformInputWriter_focusEvent(platformInputWriter *writer UNUSED, bool focused) {
	INPUT_RECORD record;
	record.EventType = FOCUS_EVENT;
	record.Event.FocusEvent.bSetFocus = focused;
	return writeRecord(&record);
}

uint32_t platformInputWriter_keyEvent(platformInputWriter *writer UNUSED) {
	// TODO
	return 0;
}

uint32_t platformInputWriter_mouseEvent(platformInputWriter *writer UNUSED) {
	// TODO
	return 0;
}

uint32_t platformInputWriter_resizeEvent(platformInputWriter *writer UNUSED, int columns, int rows, int width UNUSED, int height UNUSED) {
	INPUT_RECORD record;
	record.EventType = WINDOW_BUFFER_SIZE_EVENT;
	record.Event.WindowBufferSizeEvent.dwSize.X = columns;
	record.Event.WindowBufferSizeEvent.dwSize.Y = rows;
	return writeRecord(&record);
}

uint32_t platformInputWriter_free(platformInputWriter *writer) {
	free(writer);
	return 0;
}

#endif
