package com.jakewharton.mosaic.tty.terminal

import com.jakewharton.mosaic.terminal.Terminal
import com.jakewharton.mosaic.tty.TestTty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest

fun terminalTest(block: suspend TestTerminalEmulator.() -> Unit) {
	runTest {
		TestTty.create().use { testTty ->
			TestTerminalEmulator(testTty, this).block()
		}
	}
}

class TestTerminalEmulator(
	private val testTty: TestTty,
	private val scope: CoroutineScope,
) {
	init {
		scope.launch {
			// TODO read output
		}
	}

	fun expect(output: String, andReply: String? = null) {
		
	}

	suspend fun withTerminal(block: Terminal.() -> Unit) {
		testTty.tty.asTerminalIn(scope).use(block)
	}
}
