#if defined(_WIN32)

#include "mosaic-streams-windows.h"
#include "mosaic-test-tty.h"
#include "mosaic-tty-windows.h"
#include "mosaic-utils.h"

#include <stdatomic.h>
#include <stdio.h>
#include <windows.h>

typedef struct MosaicTestTtyImpl {
	MosaicStreams *streams;
	MosaicTty *tty;
	HANDLE conout_pipe_read;
	HANDLE conout_pipe_write;
	HANDLE conout_overlapped_event;
	HANDLE conout_interrupt_event;
} MosaicTestTtyImpl;

static atomic_flag globalTestTty = ATOMIC_FLAG_INIT;
static volatile long globalPipeNumber;

static uint32_t mosaic_test_resize_internal(HANDLE conout, int columns, int rows) {
	SMALL_RECT windowSize = {};
	windowSize.Left = 0;
	windowSize.Right = columns - 1;
	windowSize.Top = 0;
	windowSize.Bottom = rows - 1;
	if (likely(SetConsoleWindowInfo(conout, TRUE, &windowSize) == 0)) {
		return 0;
	}
	return GetLastError();
}

MOSAIC_EXPORT MosaicTestTtyInitResult mosaic_test_init(bool stdinIsTty, bool stdoutIsTty, bool stderrIsTty) {
	MosaicTestTtyInitResult result = {};

	MosaicTestTtyImpl *testTty = calloc(1, sizeof(MosaicTestTtyImpl));
	if (unlikely(testTty == NULL)) {
		// result.testTty is set to 0 which will trigger OOM.
		goto ret;
	}

	HANDLE conin = CreateFile(TEXT("CONIN$"), GENERIC_READ | GENERIC_WRITE, FILE_SHARE_READ | FILE_SHARE_WRITE, 0, OPEN_EXISTING, 0, 0);
	if (unlikely(conin == INVALID_HANDLE_VALUE)) {
		result.error = GetLastError();
		goto err_free;
	}
	if (unlikely(SetConsoleMode(conin, ENABLE_WINDOW_INPUT | ENABLE_MOUSE_INPUT | ENABLE_EXTENDED_FLAGS) == 0)) {
		result.error = GetLastError();
		goto err_conin;
	}

	HANDLE conout = CreateFile(TEXT("CONOUT$"), GENERIC_READ | GENERIC_WRITE, FILE_SHARE_READ | FILE_SHARE_WRITE, 0, OPEN_EXISTING, 0, 0);
	if (unlikely(conout == INVALID_HANDLE_VALUE)) {
		result.error = GetLastError();
		goto err_conin;
	}

	// Give the TTY a reasonable "default" size. This may cause a resize record to be written.
	uint32_t sizeResult = mosaic_test_resize_internal(conout, 80, 24);
	if (unlikely(sizeResult)) {
		result.error = sizeResult;
		goto err_conout;
	}

	// Ensure we don't start with existing records in the buffer.
	FlushConsoleInputBuffer(conin);

	CHAR pipename[MAX_PATH];
	sprintf(
		pipename,
		"\\\\.\\Pipe\\MosaicTest.%08lx.%08lx",
		GetCurrentProcessId(),
		InterlockedIncrement(&globalPipeNumber)
	);

	HANDLE conoutPipeRead = CreateNamedPipeA(
		pipename,
		PIPE_ACCESS_INBOUND | FILE_FLAG_OVERLAPPED,
		PIPE_TYPE_BYTE | PIPE_READMODE_BYTE | PIPE_WAIT,
		1,
		4096,
		4096,
		0,
		NULL
	);
	if (unlikely(conoutPipeRead == INVALID_HANDLE_VALUE)) {
		result.error = GetLastError();
		goto err_conout;
	}

	HANDLE conoutPipeWrite = CreateFileA(
		pipename,
		GENERIC_WRITE,
		0,
		NULL,
		OPEN_EXISTING,
		FILE_ATTRIBUTE_NORMAL | FILE_FLAG_WRITE_THROUGH,
		NULL
	);
	if (unlikely(conoutPipeWrite == INVALID_HANDLE_VALUE)) {
		result.error = GetLastError();
		goto err_conout_pipe_read;
	}

	HANDLE conoutOverlappedEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
	if (unlikely(conoutOverlappedEvent == INVALID_HANDLE_VALUE)) {
		result.error = GetLastError();
		goto err_conout_pipe_write;
	}

	HANDLE conoutInterruptEvent = CreateEvent(NULL, FALSE, FALSE, NULL);
	if (unlikely(conoutInterruptEvent == NULL)) {
		result.error = GetLastError();
		goto err_conout_overlapped_event;
	}

	// Any non-char handle will do.
	HANDLE stdinRead = stdinIsTty ? conin : conoutPipeRead;
	HANDLE stdoutWrite = stdoutIsTty ? conout : conoutPipeRead;
	HANDLE stderrWrite = stderrIsTty ? conout : conoutPipeRead;

	MosaicTtyInitResult ttyInitResult = mosaic_tty_init_with_handles(conin, conout, conoutPipeWrite, true);
	if (unlikely(!ttyInitResult.tty)) {
		result.error = ttyInitResult.error;
		result.already_bound = ttyInitResult.already_bound;
		goto err_conout_interrupt_event;
	}

	MosaicStreamsInitResult streamsInitResult = mosaic_streams_init_internal(stdinRead, stdoutWrite, stderrWrite);
	if (unlikely(!streamsInitResult.streams)) {
		result.error = streamsInitResult.error;
		goto err_conout_interrupt_event;
	}

	testTty->streams = streamsInitResult.streams;
	testTty->tty = ttyInitResult.tty;
	testTty->conout_pipe_read = conoutPipeRead;
	testTty->conout_pipe_write = conoutPipeWrite;
	testTty->conout_overlapped_event = conoutOverlappedEvent;
	testTty->conout_interrupt_event = conoutInterruptEvent;

	result.testTty = testTty;

	if (unlikely(atomic_flag_test_and_set(&globalTestTty))) {
		// We initialized an instance but there already was a global instance.
		result.testTty = NULL;
		result.error = mosaic_tty_free(ttyInitResult.tty);
		result.already_bound = true;
		goto err_conout_interrupt_event;
	}

	ret:
	return result;

	err_conout_interrupt_event:
	CloseHandle(conoutInterruptEvent);

	err_conout_overlapped_event:
	CloseHandle(conoutOverlappedEvent);

	err_conout_pipe_write:
	CloseHandle(conoutPipeWrite);

	err_conout_pipe_read:
	CloseHandle(conoutPipeRead);

	err_conout:
	CloseHandle(conout);

	err_conin:
	CloseHandle(conin);

	err_free:
	free(testTty);
	goto ret;
}

MOSAIC_EXPORT MosaicTty *mosaic_test_get_tty(MosaicTestTty *testTty) {
	return testTty->tty;
}

MOSAIC_EXPORT MosaicStreams *mosaic_test_get_streams(MosaicTestTty *testTty) {
	return testTty->streams;
}

MOSAIC_EXPORT MosaicIoResult mosaic_test_write(MosaicTestTty *testTty, uint8_t *buffer, int count) {
	MosaicIoResult result = {};

	INPUT_RECORD *records = calloc(count, sizeof(INPUT_RECORD));
	if (!records) {
		result.error = ERROR_NOT_ENOUGH_MEMORY;
		goto ret;
	}
	for (int i = 0; i < count; i++) {
		records[i].EventType = KEY_EVENT;
		records[i].Event.KeyEvent.uChar.AsciiChar = buffer[i];
	}

	DWORD written;
	if (WriteConsoleInputW(testTty->tty->conin, records, count, &written)) {
		result.count = written;
	} else {
		result.error = GetLastError();
	}

	free(records);

	ret:
	return result;
}

MOSAIC_EXPORT MosaicIoResult mosaic_test_read(MosaicTestTty *testTty, uint8_t *buffer, int count) {
	return mosaic_test_read_with_timeout(testTty, buffer, count, INFINITE);
}

MOSAIC_EXPORT MosaicIoResult mosaic_test_read_with_timeout(MosaicTestTty *testTty, uint8_t *buffer, int count, int timeoutMillis) {
	MosaicIoResult result = {};

	OVERLAPPED overlapped = {};
	overlapped.hEvent = testTty->conout_overlapped_event;

	// Start the asynchronous read of the pipe. This should "fail" and return an error of pending.
	if (unlikely(ReadFile(testTty->conout_pipe_read, buffer, count, NULL, &overlapped))) {
		goto success;
	}
	DWORD error = GetLastError();
	if (unlikely(error != ERROR_IO_PENDING)) {
		result.error = error;
		goto ret;
	}

	HANDLE waitHandles[2] = { testTty->conout_overlapped_event, testTty->conout_interrupt_event };
	DWORD waitResult = WaitForMultipleObjects(2, waitHandles, FALSE, timeoutMillis);
	if (unlikely(waitResult != WAIT_OBJECT_0)) {
		goto cancel_read;
	}

	success:
	;
	DWORD c;
	if (unlikely(!GetOverlappedResult(testTty->conout_pipe_read, &overlapped, &c, TRUE))) {
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
	if (!CancelIo(testTty->conout_pipe_read)) {
		// Don't overwrite a wait failure.
		if (result.error == 0) {
			result.error = GetLastError();
		}
	}

	goto ret;
}

MOSAIC_EXPORT uint32_t mosaic_test_interrupt_read(MosaicTestTty *testTty) {
	return likely(SetEvent(testTty->conout_interrupt_event) != 0)
		? 0
		: GetLastError();
}

static uint32_t mosaic_test_write_record(HANDLE h, INPUT_RECORD *record) {
	DWORD written;
	if (likely(WriteConsoleInputW(h, record, 1, &written))) {
		if (likely(written == 1)) {
			return 0;
		}
		return ERROR_WRITE_FAULT;
	}
	return GetLastError();
}

MOSAIC_EXPORT uint32_t mosaic_test_resize(MosaicTestTty *testTty, int columns, int rows, int width UNUSED, int height UNUSED) {
	uint32_t sizeResult = mosaic_test_resize_internal(testTty->tty->conout_for_size, columns, rows);
	if (unlikely(sizeResult)) {
		return sizeResult;
	}

	// Write an explicit resize record. The resize above may have already triggered this on newer
	// terminals, but for older ones the explicit send is required.
	INPUT_RECORD record;
	record.EventType = WINDOW_BUFFER_SIZE_EVENT;
	return mosaic_test_write_record(testTty->tty->conin, &record);
}

MOSAIC_EXPORT uint32_t mosaic_test_send_focus_event(MosaicTestTty *testTty, bool focused) {
	INPUT_RECORD record;
	record.EventType = FOCUS_EVENT;
	record.Event.FocusEvent.bSetFocus = focused;
	return mosaic_test_write_record(testTty->tty->conin, &record);
}

MOSAIC_EXPORT uint32_t mosaic_test_send_key_event(MosaicTestTty *testTty UNUSED) {
	// TODO
	return 0;
}

MOSAIC_EXPORT uint32_t mosaic_test_send_mouse_event(MosaicTestTty *testTty UNUSED) {
	// TODO
	return 0;
}

MOSAIC_EXPORT uint32_t mosaic_test_free(MosaicTestTty *testTty) {
	uint32_t result = 0;

	if (!CloseHandle(testTty->conout_pipe_read)) {
		result = GetLastError();
	}
	if (!CloseHandle(testTty->conout_pipe_write) && result == 0) {
		result = GetLastError();
	}
	if (!CloseHandle(testTty->conout_overlapped_event) && result == 0) {
		result = GetLastError();
	}
	if (!CloseHandle(testTty->conout_interrupt_event) && result == 0) {
		result = GetLastError();
	}

	atomic_flag_clear(&globalTestTty);
	free(testTty);
	return result;
}

#endif
