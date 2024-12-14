#include "mosaic.h"

#if defined(_WIN32)

#include "cutils.h"
#include <windows.h>

typedef struct stdinReaderImpl {
	HANDLE handles[2];
} stdinReaderImpl;

typedef struct stdinWriterImpl {
	HANDLE stdinRead;
	HANDLE stdinWrite;
	HANDLE stdoutRead;
	HANDLE stdoutWrite;
	HPCON pseudoConsole;
	stdinReader *reader;
} stdinWriterImpl;

stdinReaderResult stdinReader_initWithHandle(HANDLE console) {
	stdinReaderResult result = {};

	stdinReaderImpl *reader = calloc(1, sizeof(stdinReaderImpl));
	if (unlikely(reader == NULL)) {
		// result.reader is set to 0 which will trigger OOM.
		goto ret;
	}

	if (unlikely(console == INVALID_HANDLE_VALUE)) {
		result.error = GetLastError();
		goto err;
	}
	reader->handles[0] = console;

	HANDLE interruptEvent = CreateEvent(NULL, FALSE, FALSE, NULL);
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
		if (likely(ReadConsole(reader->handles[0], buffer, count, &read, NULL) != 0)) {
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
	if (unlikely(CloseHandle(reader->handles[1]) == 0)) {
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

	HANDLE stdinRead;
	HANDLE stdinWrite;
	if (unlikely(CreatePipe(&stdinRead, &stdinWrite, NULL, 0) == 0)) {
		result.error = GetLastError();
		goto err_writer;
	}

	HANDLE stdoutRead;
	HANDLE stdoutWrite;
	if (unlikely(CreatePipe(&stdoutRead, &stdoutWrite, NULL, 0) == 0)) {
		result.error = GetLastError();
		goto err_stdin;
	}

	COORD size;
	size.X = 80;
	size.Y = 24;

	HPCON pseudoConsole;
	HRESULT hr = CreatePseudoConsole(size, stdinRead, stdoutWrite, 0, &pseudoConsole);
	if (unlikely(hr != S_OK)) {
		result.error = GetLastError();
		goto err_stdout;
	}

	stdinReaderResult readerResult = stdinReader_initWithHandle(pseudoConsole);
	if (unlikely(readerResult.error)) {
		result.error = readerResult.error;
		goto err_pseudo;
	}
	writer->stdinRead = stdinRead;
	writer->stdinWrite = stdinWrite;
	writer->stdoutRead = stdoutRead;
	writer->stdoutWrite = stdoutWrite;
	writer->pseudoConsole = pseudoConsole;
	writer->reader = readerResult.reader;

	result.writer = writer;

	ret:
	return result;

	err_pseudo:
	ClosePseudoConsole(pseudoConsole);

	err_stdout:
	CloseHandle(stdoutRead);
	CloseHandle(stdoutWrite);

	err_stdin:
	CloseHandle(stdinRead);
	CloseHandle(stdinWrite);

	err_writer:
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
	if (likely(WriteFile(writer->stdinWrite, buffer, count, NULL, NULL))) {
		return 0;
	}
	return GetLastError();
}

platformError stdinWriter_free(stdinWriter *writer) {
	ClosePseudoConsole(writer->pseudoConsole);

	DWORD result = 0;
	if (unlikely(CloseHandle(writer->stdinRead) == 0)) {
		result = GetLastError();
	}
	if (unlikely(CloseHandle(writer->stdinWrite) == 0 && result == 0)) {
		result = GetLastError();
	}
	if (unlikely(CloseHandle(writer->stdoutRead) == 0 && result == 0)) {
		result = GetLastError();
	}
	if (unlikely(CloseHandle(writer->stdoutWrite) == 0 && result == 0)) {
		result = GetLastError();
	}
	free(writer);
	return result;
}

#endif
