#if defined(_WIN32)

#include "mosaic-utils-windows.h"

#include <stdio.h>
#include <windows.h>

uint32_t mosaic_utils_create_events(
	OUT LPHANDLE overlappedEvent,
	OUT LPHANDLE interruptEvent
) {
	uint32_t result = 0;

	HANDLE newOverlappedEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
	if (unlikely(newOverlappedEvent == INVALID_HANDLE_VALUE)) {
		result = GetLastError();
		goto ret;
	}

	HANDLE newInterruptEvent = CreateEvent(NULL, FALSE, FALSE, NULL);
	if (unlikely(newInterruptEvent == INVALID_HANDLE_VALUE)) {
		result = GetLastError();
		goto err_overlapped;
	}

	*overlappedEvent = newOverlappedEvent;
	*interruptEvent = newInterruptEvent;

	ret:
	return result;

	err_overlapped:
	CloseHandle(newOverlappedEvent);

	goto ret;
}

static volatile long globalPipeNumber;

uint32_t mosaic_utils_create_pipe(
	OUT LPHANDLE reader,
	OUT LPHANDLE writer
) {
	uint32_t result = 0;

	CHAR pipename[MAX_PATH];
	sprintf(
		pipename,
		"\\\\.\\Pipe\\MosaicTest.%08lx.%08lx",
		GetCurrentProcessId(),
		InterlockedIncrement(&globalPipeNumber)
	);

	HANDLE newReader = CreateNamedPipeA(
		pipename,
		PIPE_ACCESS_INBOUND | FILE_FLAG_OVERLAPPED,
		PIPE_TYPE_BYTE | PIPE_READMODE_BYTE | PIPE_WAIT,
		1,
		4096,
		4096,
		0,
		NULL
	);
	if (unlikely(newReader == INVALID_HANDLE_VALUE)) {
		result = GetLastError();
		goto ret;
	}

	HANDLE newWriter = CreateFileA(
		pipename,
		GENERIC_WRITE,
		0,
		NULL,
		OPEN_EXISTING,
		FILE_ATTRIBUTE_NORMAL | FILE_FLAG_WRITE_THROUGH,
		NULL
	);
	if (unlikely(newWriter == INVALID_HANDLE_VALUE)) {
		result = GetLastError();
		goto err_reader;
	}

	*reader = newReader;
	*writer = newWriter;

	ret:
	return result;

	err_reader:
	CloseHandle(newReader);

	goto ret;
}

MosaicIoResult mosaic_utils_read_console(
	HANDLE console,
	HANDLE interruptEvent,
	uint8_t *buffer,
	int count,
	int timeoutMillis
) {
	// TODO This function mostly duplicates what's in mosaic-tty-windows.c. Dedupe somehow?

	MosaicIoResult result = {};

	INPUT_RECORD *records = calloc(count, sizeof(INPUT_RECORD));
	if (!records) {
		result.error = ERROR_NOT_ENOUGH_MEMORY;
		goto ret;
	}

	DWORD waitResult;
	HANDLE waitHandles[2] = { console, interruptEvent };

	loop:
	waitResult = WaitForMultipleObjects(2, waitHandles, FALSE, timeoutMillis);
	if (likely(waitResult == WAIT_OBJECT_0)) {
		DWORD recordsRead = 0;
		if (unlikely(!ReadConsoleInputW(console, records, count, &recordsRead))) {
			goto err;
		}

		int nextBufferIndex = 0;
		for (int i = 0; i < (int) recordsRead; i++) {
			INPUT_RECORD record = records[i];
			if (record.EventType == KEY_EVENT) {
				if (record.Event.KeyEvent.wVirtualKeyCode == 0) {
					buffer[nextBufferIndex++] = record.Event.KeyEvent.uChar.AsciiChar;
				}
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

MosaicIoResult mosaic_utils_read_overlapped(
	HANDLE h,
	HANDLE overlappedEvent,
	HANDLE interruptEvent,
	uint8_t *buffer,
	int count,
	int timeoutMillis
) {
	MosaicIoResult result = {};

	OVERLAPPED overlapped = {};
	overlapped.hEvent = overlappedEvent;

	// Start the asynchronous read of the pipe. This should "fail" and return an error of pending.
	if (unlikely(ReadFile(h, buffer, count, NULL, &overlapped))) {
		goto success;
	}
	DWORD error = GetLastError();
	if (unlikely(error != ERROR_IO_PENDING)) {
		result.error = error;
		goto ret;
	}

	HANDLE waitHandles[2] = { overlappedEvent, interruptEvent };
	DWORD waitResult = WaitForMultipleObjects(2, waitHandles, FALSE, timeoutMillis);
	if (unlikely(waitResult != WAIT_OBJECT_0)) {
		goto cancel_read;
	}

	success:
	;
	DWORD c;
	if (unlikely(!GetOverlappedResult(h, &overlapped, &c, TRUE))) {
		result.error = GetLastError();
	} else {
		result.count = c;
	}

	ret:
	return result;

	cancel_read:
	if (waitResult == WAIT_FAILED) {
		// If the wait failed, we need to read the error before attempting to cancel overwrites it.
		result.error = GetLastError();
	}

	// Whether interrupted, timed out, or failed to wait, cancel the read to avoid writing memory.
	if (!CancelIo(h)) {
		// Don't overwrite a wait failure.
		if (result.error == 0) {
			result.error = GetLastError();
		}
	}

	goto ret;
}

MosaicIoResult mosaic_utils_write(
	HANDLE h,
	uint8_t *buffer,
	int count
) {
	MosaicIoResult result = {};

	DWORD written;
	if (likely(WriteFile(h, buffer, count, &written, NULL))) {
		result.count = written;
	} else {
		result.error = GetLastError();
	}

	return result;
}

#endif
