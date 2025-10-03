package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEmpty
import com.jakewharton.mosaic.terminal.Terminal
import com.jakewharton.mosaic.tty.TestTty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.channels.toList
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.io.Buffer
import kotlinx.io.UnsafeIoApi
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.encodeToByteString
import kotlinx.io.indexOf
import kotlinx.io.readByteString
import kotlinx.io.unsafe.UnsafeBufferOperations

fun terminalTest(block: suspend TerminalTester.() -> Unit) {
	runBlocking {
		TestTty.bind().use { testTty ->
			TerminalTester(testTty).block()
		}
	}
}

class TerminalTester(
	val testTty: TestTty,
) {
	private data class Expect(val output: ByteString, val reply: ByteString)
	private val expects = Channel<Expect>(UNLIMITED)

	suspend fun expect(output: String, reply: String) {
		expects.send(Expect(output.encodeToByteString(), reply.encodeToByteString()))
	}

	fun ptyWrite(s: String) = testTty.write(s)

	suspend fun withTerminal(block: suspend Terminal.(setup: ByteString) -> Unit): ByteString {
		expects.close()
		val expects = ArrayDeque(expects.toList())

		val buffer = Buffer()
		fun readUntilInterrupted() {
			var expectStartIndex = 0L
			while (true) {
				expects.firstOrNull()?.let { expect ->
					val offset = buffer.indexOf(expect.output, expectStartIndex)
					if (offset != -1L) {
						expects.removeFirst()
						expectStartIndex = offset + expect.output.size

						val replyBytes = expect.reply.toByteArray()
						testTty.writeTty(replyBytes, 0, replyBytes.size)

						continue // Look for additional expects before blocking on read.
					}
				}

				@OptIn(UnsafeIoApi::class)
				val read = UnsafeBufferOperations.writeToTail(
					buffer = buffer,
					minimumCapacity = UnsafeBufferOperations.maxSafeWriteCapacity,
				) { bytes, startIndexInclusive, endIndexExclusive ->
					testTty.readTty(bytes, startIndexInclusive, endIndexExclusive - startIndexInclusive)
				}
				if (read == 0) break
			}
		}

		coroutineScope {
			var readJob = launch(Dispatchers.IO) { readUntilInterrupted() }
			try {
				testTty.tty.asTerminalIn(this).use { terminal ->
					testTty.interruptTtyRead()
					readJob.cancelAndJoin()

					try {
						// Print the events parsed during setup.
						while (true) {
							println(terminal.events.tryReceive().getOrNull() ?: break)
						}
						val setup = buffer.readByteString()

						assertThat(expects).isEmpty()

						terminal.block(setup)
					} finally {
						readJob = launch(Dispatchers.IO) { readUntilInterrupted() }
					}
				}
			} finally {
				testTty.interruptTtyRead()
				readJob.cancelAndJoin()
			}
		}

		return buffer.readByteString()
	}
}
