#include "mosaic.h"

#if defined(_WIN32)

#include "cutils.h"
#include <assert.h>
#include <windows.h>

const int recordsCount = 64;

typedef struct platformInputImpl {
	HANDLE waitHandles[2];
	INPUT_RECORD records[recordsCount];
	platformEventHandler *handler;
	bool windowResizeEvents;
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

	HANDLE interruptEvent = CreateEvent(NULL, FALSE, FALSE, NULL);
	if (unlikely(interruptEvent == NULL)) {
		result.error = GetLastError();
		goto err;
	}

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

platformError platformInput_interrupt(platformInput *input) {
	return likely(SetEvent(input->waitHandles[1]) != 0)
		? 0
		: GetLastError();
}

platformError platformInput_enableWindowResizeEvents(platformInput *input) {
	input->windowResizeEvents = true;
	return 0;
}

terminalSizeResult platformInput_currentTerminalSize(platformInput *input UNUSED) {
	terminalSizeResult result = {};

	CONSOLE_SCREEN_BUFFER_INFO info;
	if (likely(GetConsoleScreenBufferInfo(GetStdHandle(STD_OUTPUT_HANDLE), &info))) {
		result.size.columns = info.dwSize.X;
		result.size.rows = info.dwSize.Y;
	} else {
		result.error = GetLastError();
	}

	return result;
}

platformError platformInput_free(platformInput *input) {
	DWORD result = 0;
	if (unlikely(CloseHandle(input->waitHandles[1]) == 0)) {
		result = GetLastError();
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

platformError platformInputWriter_write(platformInputWriter *writer UNUSED, char *buffer, int count) {
	DWORD result = 0;
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

platformError writeRecord(INPUT_RECORD *record) {
	DWORD written;
	if (likely(WriteConsoleInputW(writerConin, record, 1, &written))) {
		if (likely(written == 1)) {
			return 0;
		}
		return ERROR_WRITE_FAULT;
	}
	return GetLastError();
}

platformError platformInputWriter_focusEvent(platformInputWriter *writer UNUSED, bool focused) {
	INPUT_RECORD record;
	record.EventType = FOCUS_EVENT;
	record.Event.FocusEvent.bSetFocus = focused;
	return writeRecord(&record);
}

platformError platformInputWriter_keyEvent(platformInputWriter *writer UNUSED) {
	// TODO
	return 0;
}

platformError platformInputWriter_mouseEvent(platformInputWriter *writer UNUSED) {
	// TODO
	return 0;
}

platformError platformInputWriter_resizeEvent(platformInputWriter *writer UNUSED, int columns, int rows, int width UNUSED, int height UNUSED) {
	INPUT_RECORD record;
	record.EventType = WINDOW_BUFFER_SIZE_EVENT;
	record.Event.WindowBufferSizeEvent.dwSize.X = columns;
	record.Event.WindowBufferSizeEvent.dwSize.Y = rows;
	return writeRecord(&record);
}

platformError platformInputWriter_free(platformInputWriter *writer) {
	free(writer);
	return 0;
}

#endif
