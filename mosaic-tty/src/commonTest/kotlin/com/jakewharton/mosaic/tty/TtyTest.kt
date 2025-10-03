package com.jakewharton.mosaic.tty

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
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
		val events = buildList {
			while (true) {
				val event = events.tryReceive()
				if (event.isSuccess) {
					add(event.getOrThrow())
				} else {
					break
				}
			}
		}
		assertThat(events).isEmpty()
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
		val outgoing = "roundtrip".encodeToByteArray()
		outgoing.writeFullyTo(testTty::writeTty)

		val incoming = ByteArray(outgoing.size).readFully(tty::read)
		assertThat(incoming.decodeToString()).isEqualTo("roundtrip")
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
		private var lastSize: Size? = null
		override fun onResize(columns: Int, rows: Int, width: Int, height: Int) {
			// Newer Windows Terminal will automatically send two resize records after the resize. We also
			// unconditionally send our own record for older terminals which have no automatic sending.
			val size = Size(columns, rows, width, height)
			if (size != lastSize) {
				lastSize = size
				events.trySend("$prefix onResize $columns $rows $width $height")
			}
		}
	}

	private data class Size(
		val columns: Int,
		val rows: Int,
		val width: Int,
		val height: Int,
	)
}
