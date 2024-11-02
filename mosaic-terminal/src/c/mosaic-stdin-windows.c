#include "mosaic.h"

#if defined(_WIN32)

#include "cutils.h"
#include <windows.h>

typedef struct stdinReaderImpl {
	HANDLE handles[2];
} stdinReaderImpl;

typedef struct stdinWriterImpl {
	HANDLE readHandle;
	HANDLE writeHandle;
	stdinReader *reader;
} stdinWriterImpl;

stdinReaderResult stdinReader_initWithHandle(HANDLE stdin) {
	stdinReaderResult result = {};

	stdinReaderImpl *reader = calloc(1, sizeof(stdinReaderImpl));
	if (unlikely(reader == NULL)) {
		// result.reader is set to 0 which will trigger OOM.
		goto ret;
	}

	if (unlikely(stdin == INVALID_HANDLE_VALUE)) {
		result.error = GetLastError();
		goto err;
	}
	reader->handles[0] = stdin;

	HANDLE interruptEvent = CreateEvent(NULL, FALSE, FALSE, TEXT("TODO UUID"));
	if (unlikely(interruptEvent == NULL)) {
		result.error = GetLastError();
		goto err;
	}
	reader->handles[1] = interruptEvent;

	result.reader = reader;

	ret:
	return result;

	err:
	free(reader);
	goto ret;
}

stdinReaderResult stdinReader_init() {
	return stdinReader_initWithHandle(GetStdHandle(STD_INPUT_HANDLE));
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
	DWORD waitResult = WaitForMultipleObjects(2, reader->handles, FALSE, timeoutMillis);
	if (likely(waitResult == WAIT_OBJECT_0)) {
		DWORD read = 0;
		if (likely(ReadFile(reader->handles[0], buffer, count, &read, NULL) != 0)) {
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
	return likely(SetEvent(reader->handles[1]) != 0)
		? 0
		: GetLastError();
}

platformError stdinReader_free(stdinReader *reader) {
	DWORD result = 0;
	if (unlikely(CloseHandle(reader->handles[1]) != 0)) {
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

	stdinReaderResult readerResult = stdinReader_initWithHandle(writer->readHandle);
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
	return likely(WriteFile(writer->writeHandle, buffer, count, NULL, NULL))
		? 0
		: GetLastError();
}

platformError stdinWriter_free(stdinWriter *writer) {
	DWORD result = 0;
	if (unlikely(CloseHandle(writer->writeHandle) != 0)) {
		result = GetLastError();
	}
	if (unlikely(CloseHandle(writer->readHandle) != 0 && result == 0)) {
		result = GetLastError();
	}
	free(writer);
	return result;
}

#endif
