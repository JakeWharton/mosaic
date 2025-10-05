#if defined(_WIN32)

#include "mosaic-streams-windows.h"
#include "mosaic-test.h"
#include "mosaic-tty-windows.h"
#include "mosaic-utils-windows.h"

#include <stdatomic.h>
#include <windows.h>

typedef struct MosaicTestTtyImpl {
	MosaicStreams *streams;
	MosaicTty *tty;
	HANDLE conout_pipe_read;
	HANDLE conout_pipe_write;
	HANDLE conout_overlapped_event;
	HANDLE conout_interrupt_event;
	HANDLE stdin_pipe_read;
	HANDLE stdin_pipe_write;
	HANDLE stdout_pipe_read;
	HANDLE stdout_pipe_write;
	HANDLE stdout_overlapped_event;
	HANDLE stdout_interrupt_event;
	HANDLE stderr_pipe_read;
	HANDLE stderr_pipe_write;
	HANDLE stderr_overlapped_event;
	HANDLE stderr_interrupt_event;
} MosaicTestTtyImpl;

static atomic_flag globalTestTty = ATOMIC_FLAG_INIT;

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

	HANDLE conoutPipeRead;
	HANDLE conoutPipeWrite;
	uint32_t conoutPipeResult = mosaic_utils_create_pipe(&conoutPipeRead, &conoutPipeWrite);
	if (unlikely(conoutPipeResult)) {
		result.error = conoutPipeResult;
		goto err_conout;
	}

	HANDLE conoutOverlappedEvent;
	HANDLE conoutInterruptEvent;
	uint32_t conoutEventResult = mosaic_utils_create_events(&conoutOverlappedEvent, &conoutInterruptEvent);
	if (unlikely(conoutEventResult)) {
		result.error = conoutEventResult;
		goto err_conout_pipe;
	}

	HANDLE stdinPipeRead;
	HANDLE stdinPipeWrite;
	if (stdinIsTty) {
		stdinPipeRead = conin;
		stdinPipeWrite = conin;
	} else {
		uint32_t stdinPipeResult = mosaic_utils_create_pipe(&stdinPipeRead, &stdinPipeWrite);
		if (unlikely(stdinPipeResult)) {
			result.error = stdinPipeResult;
			goto err_conout_events;
		}
	}

	HANDLE stdoutPipeRead;
	HANDLE stdoutPipeWrite;
	if (stdoutIsTty) {
		stdoutPipeRead = conoutPipeRead;
		stdoutPipeWrite = conoutPipeWrite;
	} else {
		uint32_t stdoutPipeResult = mosaic_utils_create_pipe(&stdoutPipeRead, &stdoutPipeWrite);
		if (unlikely(stdoutPipeResult)) {
			result.error = stdoutPipeResult;
			goto err_stdin_pipe;
		}
	}

	HANDLE stdoutOverlappedEvent;
	HANDLE stdoutInterruptEvent;
	uint32_t stdoutEventResult = mosaic_utils_create_events(&stdoutOverlappedEvent, &stdoutInterruptEvent);
	if (unlikely(stdoutEventResult)) {
		result.error = stdoutEventResult;
		goto err_stdout_pipe;
	}

	HANDLE stderrPipeRead;
	HANDLE stderrPipeWrite;
	if (stderrIsTty) {
		stderrPipeRead = conoutPipeRead;
		stderrPipeWrite = conoutPipeWrite;
	} else {
		uint32_t stderrPipeResult = mosaic_utils_create_pipe(&stderrPipeRead, &stderrPipeWrite);
		if (unlikely(stderrPipeResult)) {
			result.error = stderrPipeResult;
			goto err_stdout_events;
		}
	}

	HANDLE stderrOverlappedEvent;
	HANDLE stderrInterruptEvent;
	uint32_t stderrEventResult = mosaic_utils_create_events(&stderrOverlappedEvent, &stderrInterruptEvent);
	if (unlikely(stderrEventResult)) {
		result.error = stderrEventResult;
		goto err_stderr_pipe;
	}

	HANDLE stdinRead = stdinIsTty ? conin : stdinPipeRead;
	HANDLE stdoutWrite = stdoutIsTty ? conout : stdoutPipeWrite;
	HANDLE stderrWrite = stderrIsTty ? conout : stderrPipeWrite;

	MosaicTtyInitResult ttyInitResult = mosaic_tty_init_with_handles(conin, conout, conoutPipeWrite, true);
	if (unlikely(!ttyInitResult.tty)) {
		result.error = ttyInitResult.error;
		result.already_bound = ttyInitResult.already_bound;
		goto err_stderr_events;
	}

	MosaicStreamsInitResult streamsInitResult = mosaic_streams_init_internal(stdinRead, stdoutWrite, stderrWrite, true);
	if (unlikely(!streamsInitResult.streams)) {
		result.error = streamsInitResult.error;
		goto err_tty;
	}

	testTty->streams = streamsInitResult.streams;
	testTty->tty = ttyInitResult.tty;
	testTty->conout_pipe_read = conoutPipeRead;
	testTty->conout_pipe_write = conoutPipeWrite;
	testTty->conout_overlapped_event = conoutOverlappedEvent;
	testTty->conout_interrupt_event = conoutInterruptEvent;
	testTty->stdin_pipe_read = stdinPipeRead;
	testTty->stdin_pipe_write = stdinPipeWrite;
	testTty->stdout_pipe_read = stdoutPipeRead;
	testTty->stdout_pipe_write = stdoutPipeWrite;
	testTty->stdout_overlapped_event = stdoutOverlappedEvent;
	testTty->stdout_interrupt_event = stdoutInterruptEvent;
	testTty->stderr_pipe_read = stderrPipeRead;
	testTty->stderr_pipe_write = stderrPipeWrite;
	testTty->stderr_overlapped_event = stderrOverlappedEvent;
	testTty->stderr_interrupt_event = stderrInterruptEvent;

	result.testTty = testTty;

	if (unlikely(atomic_flag_test_and_set(&globalTestTty))) {
		// We initialized an instance but there already was a global instance.
		result.testTty = NULL;
		result.already_bound = true;
		goto err_streams;
	}

	ret:
	return result;

	err_streams:
	mosaic_streams_free(streamsInitResult.streams);

	err_tty:
	mosaic_tty_free(ttyInitResult.tty);

	err_stderr_events:
	CloseHandle(stderrInterruptEvent);
	CloseHandle(stderrOverlappedEvent);

	err_stderr_pipe:
	if (!stderrIsTty) {
		CloseHandle(stderrPipeWrite);
		CloseHandle(stderrPipeRead);
	}

	err_stdout_events:
	CloseHandle(stdoutInterruptEvent);
	CloseHandle(stdoutOverlappedEvent);

	err_stdout_pipe:
	if (!stdoutIsTty) {
		CloseHandle(stdoutPipeWrite);
		CloseHandle(stdoutPipeRead);
	}

	err_stdin_pipe:
	if (!stdinIsTty) {
		CloseHandle(stdinPipeWrite);
		CloseHandle(stdinPipeRead);
	}

	err_conout_events:
	CloseHandle(conoutInterruptEvent);
	CloseHandle(conoutOverlappedEvent);

	err_conout_pipe:
	CloseHandle(conoutPipeWrite);
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

MOSAIC_EXPORT MosaicIoResult mosaic_test_write_tty(MosaicTestTty *testTty, uint8_t *buffer, int count) {
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

MOSAIC_EXPORT MosaicIoResult mosaic_test_read_tty(MosaicTestTty *testTty, uint8_t *buffer, int count) {
	return mosaic_test_read_tty_with_timeout(testTty, buffer, count, INFINITE);
}

MOSAIC_EXPORT MosaicIoResult mosaic_test_read_tty_with_timeout(MosaicTestTty *testTty, uint8_t *buffer, int count, int timeoutMillis) {
	return mosaic_utils_read_overlapped(testTty->conout_pipe_read, testTty->conout_overlapped_event, testTty->conout_interrupt_event, buffer, count, timeoutMillis);
}

MOSAIC_EXPORT uint32_t mosaic_test_interrupt_tty_read(MosaicTestTty *testTty) {
	return likely(SetEvent(testTty->conout_interrupt_event) != 0)
		? 0
		: GetLastError();
}

MOSAIC_EXPORT MosaicIoResult mosaic_test_write_input(MosaicTestTty *testTty, uint8_t *buffer, int count) {
	return mosaic_utils_write(testTty->stdin_pipe_write, buffer, count);
}

MOSAIC_EXPORT MosaicIoResult mosaic_test_read_output(MosaicTestTty *testTty, uint8_t *buffer, int count) {
	return mosaic_test_read_output_with_timeout(testTty, buffer, count, INFINITE);
}

MOSAIC_EXPORT MosaicIoResult mosaic_test_read_output_with_timeout(MosaicTestTty *testTty, uint8_t *buffer, int count, int timeoutMillis) {
	return mosaic_utils_read_overlapped(testTty->stdout_pipe_read, testTty->stdout_overlapped_event, testTty->stdout_interrupt_event, buffer, count, timeoutMillis);
}

MOSAIC_EXPORT uint32_t mosaic_test_interrupt_output_read(MosaicTestTty *testTty) {
	return likely(SetEvent(testTty->stdout_interrupt_event) != 0)
  		? 0
  		: GetLastError();
}

MOSAIC_EXPORT MosaicIoResult mosaic_test_read_error(MosaicTestTty *testTty, uint8_t *buffer, int count) {
	return mosaic_test_read_error_with_timeout(testTty, buffer, count, INFINITE);
}

MOSAIC_EXPORT MosaicIoResult mosaic_test_read_error_with_timeout(MosaicTestTty *testTty, uint8_t *buffer, int count, int timeoutMillis) {
	return mosaic_utils_read_overlapped(testTty->stderr_pipe_read, testTty->stderr_overlapped_event, testTty->stderr_interrupt_event, buffer, count, timeoutMillis);
}

MOSAIC_EXPORT uint32_t mosaic_test_interrupt_error_read(MosaicTestTty *testTty) {
	return likely(SetEvent(testTty->stderr_interrupt_event) != 0)
  		? 0
  		: GetLastError();
}

static uint32_t mosaic_test_write_tty_record(HANDLE h, INPUT_RECORD *record) {
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
	return mosaic_test_write_tty_record(testTty->tty->conin, &record);
}

MOSAIC_EXPORT uint32_t mosaic_test_send_focus_event(MosaicTestTty *testTty, bool focused) {
	INPUT_RECORD record;
	record.EventType = FOCUS_EVENT;
	record.Event.FocusEvent.bSetFocus = focused;
	return mosaic_test_write_tty_record(testTty->tty->conin, &record);
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

	bool stdinIsTty = testTty->stdin_pipe_read == testTty->stdin_pipe_write;
	bool stdoutIsTty = testTty->stdout_pipe_read == testTty->conout_pipe_read;
	bool stderrIsTty = testTty->stderr_pipe_read == testTty->conout_pipe_read;

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
	if (!stdinIsTty) {
		if (!CloseHandle(testTty->stdin_pipe_read) && result == 0) {
			result = GetLastError();
		}
		if (!CloseHandle(testTty->stdin_pipe_write) && result == 0) {
			result = GetLastError();
		}
	}
	if (!stdoutIsTty) {
		if (!CloseHandle(testTty->stdout_pipe_read) && result == 0) {
			result = GetLastError();
		}
		if (!CloseHandle(testTty->stdout_pipe_write) && result == 0) {
			result = GetLastError();
		}
	}
	if (!CloseHandle(testTty->stdout_overlapped_event) && result == 0) {
		result = GetLastError();
	}
	if (!CloseHandle(testTty->stdout_interrupt_event) && result == 0) {
		result = GetLastError();
	}
	if (!stderrIsTty) {
		if (!CloseHandle(testTty->stderr_pipe_read) && result == 0) {
			result = GetLastError();
		}
		if (!CloseHandle(testTty->stderr_pipe_write) && result == 0) {
			result = GetLastError();
		}
	}
	if (!CloseHandle(testTty->stderr_overlapped_event) && result == 0) {
		result = GetLastError();
	}
	if (!CloseHandle(testTty->stderr_interrupt_event) && result == 0) {
		result = GetLastError();
	}

	atomic_flag_clear(&globalTestTty);
	free(testTty);
	return result;
}

#endif
