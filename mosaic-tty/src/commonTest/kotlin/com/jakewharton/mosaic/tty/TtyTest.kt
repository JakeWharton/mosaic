package com.jakewharton.mosaic.tty

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
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

	@Test fun focusEventNoCallback() {
		testTty.sendFocusEvent(true)
	}

	@Test fun focusEventCallbackDeliveredOnWindows() = runTest {
		if (!isWindows()) return@runTest

		tty.setCallback(MyCallback())

		testTty.sendFocusEvent(true)
		doWriteReadRoundtrip()

		assertThat(events.receive()).isEqualTo("hey! onFocus true")
	}

	@Test fun focusEventCallbackIgnoredOnNonWindows() = runTest {
		if (isWindows()) return@runTest

		tty.setCallback(MyCallback())

		testTty.sendFocusEvent(true)

		assertEventsEmpty()
	}

	@Test fun keyEventNoCallback() {
		testTty.sendKeyEvent()
	}

	@Ignore // Event not delivered yet.
	@Test fun keyEventCallback() = runTest {
		if (!isWindows()) return@runTest

		tty.setCallback(MyCallback())

		testTty.sendKeyEvent()
		doWriteReadRoundtrip()

		assertThat(events.receive()).isEqualTo("hey! onKey")
	}

	@Test fun keyEventCallbackIgnoredOnNonWindows() = runTest {
		if (isWindows()) return@runTest

		tty.setCallback(MyCallback())

		testTty.sendKeyEvent()

		assertEventsEmpty()
	}

	@Test fun mouseEventNoCallback() {
		testTty.sendMouseEvent()
	}

	@Ignore // Event not delivered yet.
	@Test fun mouseEventCallback() = runTest {
		if (!isWindows()) return@runTest

		tty.setCallback(MyCallback())

		testTty.sendMouseEvent()
		doWriteReadRoundtrip()

		assertThat(events.receive()).isEqualTo("hey! onMouse")
	}

	@Test fun mouseEventCallbackIgnoredOnNonWindows() = runTest {
		if (isWindows()) return@runTest

		tty.setCallback(MyCallback())

		testTty.sendMouseEvent()

		assertEventsEmpty()
	}

	@Test fun resizeNoCallback() {
		testTty.resize(1, 2, 3, 4)
	}

	@Test fun resizeCallback() = runTest {
		tty.enableWindowResizeEvents()
		tty.setCallback(MyCallback())

		testTty.resize(1, 2, 3, 4)
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

		testTty.resize(1, 2, 3, 4)
		doWriteReadRoundtrip()

		assertEventsEmpty()
	}

	@Test fun callbackReplacementUsesNewInstance() = runTest {
		tty.enableWindowResizeEvents()
		tty.setCallback(MyCallback())
		tty.setCallback(MyCallback("hello!"))

		testTty.resize(1, 2, 0, 0)
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

	@Test fun defaultSize() {
		assertThat(tty.currentSize()).isEqualTo(intArrayOf(80, 24, 0, 0))
	}

	@Test fun resizeAffectsSize() {
		testTty.resize(90, 30, 0, 0)
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
