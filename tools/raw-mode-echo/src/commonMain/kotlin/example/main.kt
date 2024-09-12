@file:JvmName("Main")

package example

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.jakewharton.finalization.withFinalizationHook
import com.jakewharton.mosaic.terminal.Tty
import kotlin.jvm.JvmName
import kotlinx.coroutines.CoroutineStart.UNDISPATCHED
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main(vararg args: String) = RawModeEchoCommand().main(args)

@OptIn(ExperimentalStdlibApi::class)
private class RawModeEchoCommand : CliktCommand("raw-mode-echo") {
	private val all by option().flag()
	private val focusEvents by option().flag()
	private val kittyKeyEvents by option().flag()
	private val mouseEvents by option().flag()
	private val inBandResize by option().flag()
	private val bracketedPaste by option().flag()
	private val colorQuery by option().flag()

	override fun run() = runBlocking {
		val rawMode = Tty.enableRawMode()
		withFinalizationHook(
			hook = {
				rawMode.close()
				print("\u001b[?1003l") // Any-event disable
				print("\u001b[?1004l") // Focus disable
				print("\u001b[?2004l") // Bracketed paste disable
				print("\u001b[?2048l") // In-band resize disable
				print("\u001b[?25h") // Cursor enable
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
				}
				if (all || inBandResize) {
					print("\u001b[?2048\$p") // In-band resize query
					print("\u001b[?2048h") // In-band resize enable
				}
				if (all || bracketedPaste) {
					print("\u001b[?2004h") // Bracketed paste enable
				}
				if (all || colorQuery) {
					print("\u001b[?996n") // Color scheme request
				}

				val reader = Tty.stdinReader()

				// Upon receiving a signal, this block's job will be canceled. Use that to wake up the
				// blocking stdin read so it loops and checks if its job is still active or not.
				val readerInterruptJob = launch(start = UNDISPATCHED) {
					try {
						awaitCancellation()
					} finally {
						reader.interrupt()
					}
				}

				val inputs = Channel<String>(UNLIMITED)
				launch(Dispatchers.IO) {
					val buffer = ByteArray(1024)
					val job = coroutineContext.job
					while (job.isActive) {
						val read = reader.read(buffer, 0, 1024)
						if (read > 0) {
							val hex = buffer.toHexString(endIndex = read)
							inputs.trySend(hex)
							if (hex == "03") {
								inputs.close()
								break
							}
						}
					}
				}

				for (input in inputs) {
					print(input)
					print("\r\n")
				}
				readerInterruptJob.cancel()
			},
		)
	}
}
