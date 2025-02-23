@file:JvmName("Main")

package example

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import com.jakewharton.finalization.withFinalizationHook
import com.jakewharton.mosaic.terminal.Tty
import com.jakewharton.mosaic.terminal.event.DebugEvent
import com.jakewharton.mosaic.terminal.event.KeyboardEvent
import com.jakewharton.mosaic.terminal.event.KeyboardEvent.Companion.ModifierCtrl
import kotlin.jvm.JvmName
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main(vararg args: String) = RawModeEchoCommand().main(args)

@OptIn(ExperimentalStdlibApi::class)
private class RawModeEchoCommand : CliktCommand("raw-mode-echo") {
	private enum class Mode { Hex, Event, Both }
	private val mode by option()
		.enum<Mode> { it.name.lowercase() }
		.default(Mode.Both)

	private val all by option().flag()
	private val focusEvents by option().flag()
	private val kittyKeyEvents by option().flag()
	private val mouseEvents by option().flag()
	private val inBandResize by option().flag()
	private val bracketedPaste by option().flag()
	private val systemThemeQuery by option().flag()
	private val colorsQuery by option().flag()

	private val windowResize by option().flag()

	override fun run() = runBlocking {
		val reader = Tty.terminalReader(emitDebugEvents = mode != Mode.Event)
		reader.enableRawMode()
		withFinalizationHook(
			hook = {
				print("\u001b[?1003l") // Any-event disable
				print("\u001b[?1004l") // Focus disable
				print("\u001b[?2004l") // Bracketed paste disable
				print("\u001b[?2048l") // In-band resize disable
				print("\u001b[?25h") // Cursor enable
				reader.close()
			},
			block = {
				print("\u001b[?25l") // Cursor disable
				print("\u001b[c") // Primary device attrs
				print("\u001b[=c") // Tertiary device attrs
				print("\u001b[5n") // Device status report
				print("\u001b[>0q") // xterm version
				if (all || focusEvents) {
					print("\u001b[?1004\$p") // Focus query
					print("\u001b[?1004h") // Focus enable
				}
				if (all || kittyKeyEvents) {
					print("\u001b[?u") // Kitty keyboard enable
				}
				if (all || mouseEvents) {
					print("\u001b[?1003h") // Any-event enable
					print("\u001b[?1006h") // SGR extended coordinates enable
				}
				if (all || inBandResize) {
					print("\u001b[?2048\$p") // In-band resize query
					print("\u001b[?2048h") // In-band resize enable
				}
				if (all || bracketedPaste) {
					print("\u001b[?2004h") // Bracketed paste enable
				}
				if (all || systemThemeQuery) {
					print("\u001b[?996n") // Color scheme request
					print("\u001b[?2031h") // Color scheme enable
				}
				if (all || colorsQuery) {
					print("\u001b]10;?\u001b\\")
					print("\u001b]11;?\u001b\\")
					print("\u001b]12;?\u001b\\")
					for (i in 0 until 256) {
						print("\u001b]4;$i;?\u001b\\")
					}
				}

				print("\u001b[5n") // Device status report

				if (windowResize) {
					reader.enableWindowResizeEvents()
				}
				print("Initial size: ${reader.currentSize()}\r\n")

				// Upon receiving a signal, this block's job will be canceled. Use that to wake up the
				// blocking stdin read so it loops and checks if its job is still active or not.
				val readerInterruptJob = launch(start = UNDISPATCHED) {
					try {
						awaitCancellation()
					} finally {
						reader.interrupt()
					}
				}

				launch(Dispatchers.IO) {
					reader.runParseLoop()
				}

				var first = true
				fun printNewline() {
					if (!first) print("\r\n")
					first = false
				}

				val events = reader.events
				while (true) {
					val event = events.receive()

					fun printDebug() {
						printNewline()
						print(event.toString())
					}
					suspend fun printHex() {
						val string = (events.receive() as DebugEvent).bytes.toHexString()
						if (string.isNotEmpty()) {
							printNewline()
							print(string)
						}
					}

					when (mode) {
						Mode.Hex -> printHex()
						Mode.Event -> printDebug()
						Mode.Both -> {
							printNewline()
							printHex()
							printDebug()
						}
					}

					if (event is KeyboardEvent &&
						event.codepoint == 0x63 &&
						event.modifiers == ModifierCtrl
					) {
						break
					}
				}

				print("\r\n")
				readerInterruptJob.cancel()
			},
		)
	}
}
