package com.jakewharton.mosaic.tty

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import assertk.assertions.isTrue
import assertk.assertions.isZero
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class TtyTest {
	private val events = Channel<String>(UNLIMITED)
	private val testTty = TestTty.bind()
	private val tty = testTty.tty

	@BeforeTest fun before() {
		tty.enableRawMode()
	}

	@AfterTest fun after() = runTest {
		// TestTty.close() will call Tty.close(), but we get a free idempotency test here.
		tty.close()
		testTty.close()

		assertEventsEmpty()
	}

	private fun assertEventsEmpty() {
		assertThat(events.isEmpty, name = "events empty").isTrue()
	}

	@Test fun readWhatWasWritten() {
		val buffer = ByteArray(10) { 'x'.code.toByte() }

		testTty.write("hello")
		val readA = tty.read(buffer, 0, 10)
		assertThat(readA, "readA").isEqualTo(5)
		assertThat(buffer.decodeToString()).isEqualTo("helloxxxxx")

		testTty.write("world")
		val readB = tty.read(buffer, 0, 10)
		assertThat(readB, "readB").isEqualTo(5)
		assertThat(buffer.decodeToString()).isEqualTo("worldxxxxx")
	}

	@Test fun readOnlyUpToCount() {
		val buffer = ByteArray(10) { 'x'.code.toByte() }

		testTty.write("hello")
		val read = tty.read(buffer, 0, 4)
		assertThat(read).isEqualTo(4)
		assertThat(buffer.decodeToString()).isEqualTo("hellxxxxxx")
	}

	@Test fun readUnderflow() {
		val buffer = ByteArray(10) { 'x'.code.toByte() }

		testTty.write("hello")
		val read = tty.read(buffer, 0, 10)
		assertThat(read).isEqualTo(5)
		assertThat(buffer.decodeToString()).isEqualTo("helloxxxxx")
	}

	@Test fun readAtOffset() {
		val buffer = ByteArray(10) { 'x'.code.toByte() }

		testTty.write("hello")
		val read = tty.read(buffer, 5, 5)
		assertThat(read).isEqualTo(5)
		assertThat(buffer.decodeToString()).isEqualTo("xxxxxhello")
	}

	@Test fun readCanBeInterrupted() = runTest {
		backgroundScope.launch(Dispatchers.Default) {
			delay(150.milliseconds)
			tty.interruptRead()
		}
		val readA = tty.read(ByteArray(10), 0, 10)
		assertThat(readA).isZero()

		backgroundScope.launch(Dispatchers.Default) {
			delay(150.milliseconds)
			tty.interruptRead()
		}
		val readB = tty.read(ByteArray(10), 0, 10)
		assertThat(readB).isZero()
	}

	@Test fun readWithTimeoutReturnsZeroOnTimeout() {
		// Windows appears to be happy to return a few milliseconds early, so we just validate a
		// conservative lower bound which indicates that there was at least _some_ waiting.

		val readA: Int
		val tookA = measureTime {
			readA = tty.readWithTimeout(ByteArray(10), 0, 10, 100)
		}
		assertThat(readA).isZero()
		assertThat(tookA).isGreaterThan(50.milliseconds)

		val readB: Int
		val tookB = measureTime {
			readB = tty.readWithTimeout(ByteArray(10), 0, 10, 100)
		}
		assertThat(readB).isZero()
		assertThat(tookB).isGreaterThan(50.milliseconds)
	}

	@Test fun focusEventNoCallback() {
		testTty.focusEvent(true)
	}

	@Test fun focusEventCallbackDeliveredOnWindows() = runTest {
		if (!isWindows()) return@runTest

		tty.setCallback(MyCallback())

		testTty.focusEvent(true)
		doWriteReadRoundtrip()

		assertThat(events.receive()).isEqualTo("hey! onFocus true")
	}

	@Test fun focusEventCallbackIgnoredOnNonWindows() = runTest {
		if (isWindows()) return@runTest

		tty.setCallback(MyCallback())

		testTty.focusEvent(true)

		assertEventsEmpty()
	}

	@Test fun keyEventNoCallback() {
		testTty.keyEvent()
	}

	@Ignore // Event not delivered yet.
	@Test fun keyEventCallback() = runTest {
		if (!isWindows()) return@runTest

		tty.setCallback(MyCallback())

		testTty.keyEvent()
		doWriteReadRoundtrip()

		assertThat(events.receive()).isEqualTo("hey! onKey")
	}

	@Test fun keyEventCallbackIgnoredOnNonWindows() = runTest {
		if (isWindows()) return@runTest

		tty.setCallback(MyCallback())

		testTty.keyEvent()

		assertEventsEmpty()
	}

	@Test fun mouseEventNoCallback() {
		testTty.mouseEvent()
	}

	@Ignore // Event not delivered yet.
	@Test fun mouseEventCallback() = runTest {
		if (!isWindows()) return@runTest

		tty.setCallback(MyCallback())

		testTty.mouseEvent()
		doWriteReadRoundtrip()

		assertThat(events.receive()).isEqualTo("hey! onMouse")
	}

	@Test fun mouseEventCallbackIgnoredOnNonWindows() = runTest {
		if (isWindows()) return@runTest

		tty.setCallback(MyCallback())

		testTty.mouseEvent()

		assertEventsEmpty()
	}

	@Test fun resizeEventNoCallback() {
		testTty.resizeEvent(1, 2, 3, 4)
	}

	@Test fun resizeEventCallback() = runTest {
		tty.enableWindowResizeEvents()
		tty.setCallback(MyCallback())

		testTty.resizeEvent(1, 2, 3, 4)
		doWriteReadRoundtrip()

		val expected = if (isWindows()) {
			"hey! onResize 1 2 0 0"
		} else {
			"hey! onResize 1 2 3 4"
		}
		assertThat(events.receive()).isEqualTo(expected)
	}

	@Test fun callbackClear() = runTest {
		tty.setCallback(MyCallback())
		tty.setCallback(null)

		testTty.resizeEvent(1, 2, 3, 4)
		doWriteReadRoundtrip()

		assertEventsEmpty()
	}

	@Test fun callbackReplacementUsesNewInstance() = runTest {
		tty.enableWindowResizeEvents()
		tty.setCallback(MyCallback())
		tty.setCallback(MyCallback("hello!"))

		testTty.resizeEvent(1, 2, 0, 0)
		doWriteReadRoundtrip()

		assertThat(events.receive()).isEqualTo("hello! onResize 1 2 0 0")
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

	@Test fun sizeAndResize() {
		assertThat(tty.currentSize()).isEqualTo(intArrayOf(80, 24, 0, 0))

		testTty.resizeEvent(90, 30, 0, 0)
		assertThat(tty.currentSize()).isEqualTo(intArrayOf(90, 30, 0, 0))
	}

	/**
	 * On Windows events are only delivered during reads. Call this after an event to perform a
	 * write-read round-trip to ensure all events were processed.
	 */
	private fun doWriteReadRoundtrip() {
		val data = "roundtrip"
		testTty.write(data)
		assertThat(tty.read(data.length)).isEqualTo(data)
	}

	inner class MyCallback(
		private val prefix: String = "hey!",
	) : Tty.Callback {
		override fun onFocus(focused: Boolean) {
			events.trySend("$prefix onFocus $focused")
		}
		override fun onKey() {
			events.trySend("$prefix onKey")
		}
		override fun onMouse() {
			events.trySend("$prefix onMouse")
		}
		override fun onResize(columns: Int, rows: Int, width: Int, height: Int) {
			events.trySend("$prefix onResize $columns $rows $width $height")
		}
	}
}
