#include "mosaic.h"

#if defined(_WIN32)

#include "cutils.h"
#include <windows.h>

typedef struct stdinReaderImpl {
	HANDLE waitHandles[2];
} stdinReaderImpl;

typedef struct stdinWriterImpl {
	HANDLE handle;
	stdinReader *reader;
} stdinWriterImpl;

stdinReaderResult stdinReader_initWithHandle(HANDLE stdinRead) {
	stdinReaderResult result = {};

	stdinReaderImpl *reader = calloc(1, sizeof(stdinReaderImpl));
	if (unlikely(reader == NULL)) {
		// result.reader is set to 0 which will trigger OOM.
		goto ret;
	}

	if (unlikely(stdinRead == INVALID_HANDLE_VALUE)) {
		result.error = GetLastError();
		goto err;
	}
	reader->waitHandles[0] = stdinRead;

	HANDLE interruptEvent = CreateEvent(NULL, FALSE, FALSE, NULL);
	if (unlikely(interruptEvent == NULL)) {
		result.error = GetLastError();
		goto err;
	}
	reader->waitHandles[1] = interruptEvent;

	result.reader = reader;

	ret:
	return result;

	err:
	free(reader);
	goto ret;
}

stdinReaderResult stdinReader_init() {
	HANDLE h = GetStdHandle(STD_INPUT_HANDLE);
	return stdinReader_initWithHandle(h);
}

stdinRead stdinReader_read(
	stdinReader *reader,
	void *buffer,
	int count
) {
	return stdinReader_readWithTimeout(reader, buffer, count, INFINITE);
}

stdinRead stdinReader_readWithTimeout(
	stdinReader *reader,
	void *buffer,
	int count,
	int timeoutMillis
) {
	stdinRead result = {};
	DWORD waitResult = WaitForMultipleObjects(2, reader->waitHandles, FALSE, timeoutMillis);
	if (likely(waitResult == WAIT_OBJECT_0)) {
		DWORD read = 0;
		if (likely(ReadFile(reader->waitHandles[0], buffer, count, &read, NULL) != 0)) {
			// TODO EOF?
			result.count = read;
		} else {
			goto err;
		}
	} else if (unlikely(waitResult == WAIT_FAILED)) {
		goto err;
	}
	// Else if the interrupt event was selected or we timed out, return a count of 0.

	ret:
	return result;

	err:
	result.error = GetLastError();
	goto ret;
}

platformError stdinReader_interrupt(stdinReader *reader) {
	return likely(SetEvent(reader->waitHandles[1]) != 0)
		? 0
		: GetLastError();
}

platformError stdinReader_free(stdinReader *reader) {
	DWORD result = 0;
	if (unlikely(CloseHandle(reader->waitHandles[1]) == 0)) {
		result = GetLastError();
	}
	free(reader);
	return result;
}

stdinWriterResult stdinWriter_init() {
	stdinWriterResult result = {};

	stdinWriterImpl *writer = calloc(1, sizeof(stdinWriterImpl));
	if (unlikely(writer == NULL)) {
		// result.writer is set to 0 which will trigger OOM.
		goto ret;
	}

	HANDLE h = GetStdHandle(STD_INPUT_HANDLE);
	stdinReaderResult readerResult = stdinReader_initWithHandle(h);
	if (unlikely(readerResult.error)) {
		result.error = readerResult.error;
		goto err;
	}
	writer->handle = h;
	writer->reader = readerResult.reader;

	result.writer = writer;

	ret:
	return result;

	err:
	free(writer);
	goto ret;
}

stdinReader *stdinWriter_getReader(stdinWriter *writer) {
	return writer->reader;
}

platformError stdinWriter_write(stdinWriter *writer, void *buffer, int count) {
	INPUT_RECORD *records = calloc(count, sizeof(INPUT_RECORD));

	for (int i = 0; i < count; i++) {
		records[i].EventType = KEY_EVENT;
		records[i].Event.KeyEvent.bKeyDown = TRUE;
		records[i].Event.KeyEvent.uChar.UnicodeChar = (WCHAR) ((CHAR *)buffer)[i];
	}

	platformError result = 0;
	int written = 0;
	while (written < count) {
		DWORD wrote = 0;
		INPUT_RECORD *ptr = records + (written * sizeof(INPUT_RECORD));
		if (unlikely(WriteConsoleInput(writer->handle, ptr, count - written, &wrote) == 0)) {
			result = GetLastError();
			break;
		}
		written -= wrote;
	}

	FlushConsoleInputBuffer(writer->handle);

	free(records);
	return result;
}

platformError stdinWriter_free(stdinWriter *writer) {
	DWORD result = 0;
	free(writer);
	return result;
}

#endif
