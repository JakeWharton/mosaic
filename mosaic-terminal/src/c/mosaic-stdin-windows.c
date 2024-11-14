#include "mosaic.h"

#if defined(_WIN32)

#include "cutils.h"
#include <windows.h>

typedef struct stdinReaderImpl {
	HANDLE waitHandles[2];
	HANDLE readHandle;
} stdinReaderImpl;

typedef struct stdinWriterImpl {
	HANDLE readHandle;
	HANDLE writeHandle;
	HANDLE eventHandle;
	stdinReader *reader;
} stdinWriterImpl;

stdinReaderResult stdinReader_initWithHandle(HANDLE stdinRead, HANDLE stdinWait) {
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
	reader->readHandle = stdinRead;
	reader->waitHandles[0] = stdinWait;

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
	return stdinReader_initWithHandle(h, h);
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
		if (likely(ReadFile(reader->readHandle, buffer, count, &read, NULL) != 0)) {
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

	if (unlikely(CreatePipe(&writer->readHandle, &writer->writeHandle, NULL, 0) == 0)) {
		result.error = GetLastError();
		goto err;
	}

	HANDLE writeEvent = CreateEvent(NULL, FALSE, FALSE, NULL);
	if (unlikely(writeEvent == NULL)) {
		result.error = GetLastError();
		goto err;
	}
	writer->eventHandle = writeEvent;

	stdinReaderResult readerResult = stdinReader_initWithHandle(writer->readHandle, writer->eventHandle);
	if (unlikely(readerResult.error)) {
		result.error = readerResult.error;
		goto err;
	}
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
	// Per https://learn.microsoft.com/en-us/windows/win32/api/namedpipeapi/nf-namedpipeapi-createpipe#remarks
	// "When a process uses WriteFile to write to an anonymous pipe,
	//  the write operation is not completed until all bytes are written."
	if (likely(WriteFile(writer->writeHandle, buffer, count, NULL, NULL)
			&& SetEvent(writer->eventHandle))) {
		return 0;
	}
	return GetLastError();
}

platformError stdinWriter_free(stdinWriter *writer) {
	DWORD result = 0;
	if (unlikely(CloseHandle(writer->eventHandle) == 0)) {
		result = GetLastError();
	}
	if (unlikely(CloseHandle(writer->writeHandle) == 0 && result == 0)) {
		result = GetLastError();
	}
	if (unlikely(CloseHandle(writer->readHandle) == 0 && result == 0)) {
		result = GetLastError();
	}
	free(writer);
	return result;
}

#endif
