package com.jakewharton.mosaic.tty

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.hasMessage
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import assertk.assertions.isInstanceOf
import assertk.assertions.isZero
import kotlin.test.AfterTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest

class TtyTest {
	private val events = ArrayDeque<String>()
	private val testTty = TestTty.create()
	private val tty = testTty.tty

	@AfterTest fun after() {
		tty.close()
		testTty.close()
		assertThat(events, name = "events").isEmpty()
	}

	@Test fun bindTwiceFails() {
		Tty.bind().use {
			assertFailure {
				Tty.bind()
			}.isInstanceOf<IllegalStateException>()
				.hasMessage("Tty already bound")
		}
	}

	@Test fun readWhatWasWritten() {
		val buffer = ByteArray(10) { 'x'.code.toByte() }

		testTty.writeInput("hello".encodeToByteArray())
		val readA = tty.readInput(buffer, 0, 10)
		assertThat(readA, "readA").isEqualTo(5)
		assertThat(buffer.decodeToString()).isEqualTo("helloxxxxx")

		testTty.writeInput("world".encodeToByteArray())
		val readB = tty.readInput(buffer, 0, 10)
		assertThat(readB, "readB").isEqualTo(5)
		assertThat(buffer.decodeToString()).isEqualTo("worldxxxxx")
	}

	@Test fun readOnlyUpToCount() {
		val buffer = ByteArray(10) { 'x'.code.toByte() }

		testTty.writeInput("hello".encodeToByteArray())
		val read = tty.readInput(buffer, 0, 4)
		assertThat(read).isEqualTo(4)
		assertThat(buffer.decodeToString()).isEqualTo("hellxxxxxx")
	}

	@Test fun readUnderflow() {
		val buffer = ByteArray(10) { 'x'.code.toByte() }

		testTty.writeInput("hello".encodeToByteArray())
		val read = tty.readInput(buffer, 0, 10)
		assertThat(read).isEqualTo(5)
		assertThat(buffer.decodeToString()).isEqualTo("helloxxxxx")
	}

	@Test fun readAtOffset() {
		val buffer = ByteArray(10) { 'x'.code.toByte() }

		testTty.writeInput("hello".encodeToByteArray())
		val read = tty.readInput(buffer, 5, 5)
		assertThat(read).isEqualTo(5)
		assertThat(buffer.decodeToString()).isEqualTo("xxxxxhello")
	}

	@Test fun readCanBeInterrupted() = runTest {
		backgroundScope.launch(Dispatchers.Default) {
			delay(150.milliseconds)
			tty.interruptRead()
		}
		val readA = tty.readInput(ByteArray(10), 0, 10)
		assertThat(readA).isZero()

		backgroundScope.launch(Dispatchers.Default) {
			delay(150.milliseconds)
			tty.interruptRead()
		}
		val readB = tty.readInput(ByteArray(10), 0, 10)
		assertThat(readB).isZero()
	}

	@Test fun readWithTimeoutReturnsZeroOnTimeout() {
		// Windows appears to be happy to return a few milliseconds early, so we just validate a
		// conservative lower bound which indicates that there was at least _some_ waiting.

		val readA: Int
		val tookA = measureTime {
			readA = tty.readInputWithTimeout(ByteArray(10), 0, 10, 100)
		}
		assertThat(readA).isZero()
		assertThat(tookA).isGreaterThan(50.milliseconds)

		val readB: Int
		val tookB = measureTime {
			readB = tty.readInputWithTimeout(ByteArray(10), 0, 10, 100)
		}
		assertThat(readB).isZero()
		assertThat(tookB).isGreaterThan(50.milliseconds)
	}

	@Test fun focusEventNoCallback() {
		testTty.focusEvent(true)
	}

	@Test fun focusEventCallbackDeliveredOnWindows() {
		if (!isWindows()) return

		tty.setCallback(MyCallback())

		testTty.focusEvent(true)
		doWriteReadRoundtrip()

		assertThat(events.removeFirst()).isEqualTo("hey! onFocus true")
	}

	@Test fun focusEventCallbackIgnoredOnNonWindows() {
		if (isWindows()) return

		tty.setCallback(MyCallback())

		testTty.focusEvent(true)

		assertThat(events).isEmpty()
	}

	@Test fun keyEventNoCallback() {
		testTty.keyEvent()
	}

	@Ignore // Event not delivered yet.
	@Test fun keyEventCallback() {
		if (!isWindows()) return

		tty.setCallback(MyCallback())

		testTty.keyEvent()
		doWriteReadRoundtrip()

		assertThat(events.removeFirst()).isEqualTo("hey! onKey")
	}

	@Test fun keyEventCallbackIgnoredOnNonWindows() {
		if (isWindows()) return

		tty.setCallback(MyCallback())

		testTty.keyEvent()

		assertThat(events).isEmpty()
	}

	@Test fun mouseEventNoCallback() {
		testTty.mouseEvent()
	}

	@Ignore // Event not delivered yet.
	@Test fun mouseEventCallback() {
		if (!isWindows()) return

		tty.setCallback(MyCallback())

		testTty.mouseEvent()
		doWriteReadRoundtrip()

		assertThat(events.removeFirst()).isEqualTo("hey! onMouse")
	}

	@Test fun mouseEventCallbackIgnoredOnNonWindows() {
		if (isWindows()) return

		tty.setCallback(MyCallback())

		testTty.mouseEvent()

		assertThat(events).isEmpty()
	}

	@Test fun resizeEventNoCallback() {
		testTty.resizeEvent(1, 2, 3, 4)
	}

	@Test fun resizeEventCallback() {
		if (isWindows()) {
			// Resize events are not delivered unless we disable their filtering.
			tty.enableWindowResizeEvents()
		}
		tty.setCallback(MyCallback())

		testTty.resizeEvent(1, 2, 3, 4)
		doWriteReadRoundtrip()

		val expected = if (isWindows()) {
			"hey! onResize 1 2 0 0"
		} else {
			"hey! onResize 1 2 3 4"
		}
		assertThat(events.removeFirst()).isEqualTo(expected)
	}

	@Test fun callbackClear() {
		tty.setCallback(MyCallback())
		tty.setCallback(null)

		testTty.resizeEvent(1, 2, 3, 4)
		doWriteReadRoundtrip()

		assertThat(events).isEmpty()
	}

	@Test fun callbackReplacementUsesNewInstance() {
		if (isWindows()) {
			tty.enableWindowResizeEvents()
		}

		tty.setCallback(MyCallback())
		tty.setCallback(MyCallback("hello!"))

		testTty.resizeEvent(1, 2, 0, 0)
		doWriteReadRoundtrip()

		assertThat(events.removeFirst()).isEqualTo("hello! onResize 1 2 0 0")
	}

	@Keep // Ensure reference doesn't leak to a local.
	private fun createAndSetCallback(): WeakReference<MyCallback> {
		val callback = MyCallback()
		tty.setCallback(callback)
		return WeakReference(callback)
	}

	@Test fun callbackGarbageCollectedOnClear() {
		val callbackRef = createAndSetCallback()
		tty.setCallback(null)
		callbackRef.assertGc()
	}

	@Test fun callbackGarbageCollectedOnReplacement() {
		val callbackRef = createAndSetCallback()
		tty.setCallback(MyCallback())
		callbackRef.assertGc()
	}

	@Test fun callbackGarbageCollectedOnClose() {
		val callbackRef = createAndSetCallback()
		tty.close()
		callbackRef.assertGc()
	}

	/**
	 * On Windows events are only delivered during reads. Call this after an event to perform a
	 * write-read round-trip to ensure all events were processed.
	 */
	private fun doWriteReadRoundtrip() {
		val outgoing = "roundtrip".encodeToByteArray()
		testTty.writeInput(outgoing)
		var offset = 0
		val incoming = ByteArray(1024)
		while (offset < outgoing.size) {
			val read = tty.readInput(incoming, offset, outgoing.size)
			if (read == -1) {
				throw RuntimeException("eof")
			}
			offset += read
		}
		assertThat(incoming.decodeToString(endIndex = offset)).isEqualTo("roundtrip")
	}

	inner class MyCallback(
		private val prefix: String = "hey!",
	) : Tty.Callback {
		override fun onFocus(focused: Boolean) {
			events += "$prefix onFocus $focused"
		}
		override fun onKey() {
			events += "$prefix onKey"
		}
		override fun onMouse() {
			events += "$prefix onMouse"
		}
		override fun onResize(columns: Int, rows: Int, width: Int, height: Int) {
			events += "$prefix onResize $columns $rows $width $height"
		}
	}

	private fun TestTty.writeInput(buffer: ByteArray) {
		// TODO Figure out public API to fully write a ByteArray.
		val written = writeInput(buffer, 0, buffer.size)
		assertThat(written).isEqualTo(buffer.size)
	}
}
