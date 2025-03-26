@file:JvmName("Main")

package example

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import com.jakewharton.mosaic.terminal.Terminal
import com.jakewharton.mosaic.tty.Tty
import com.jakewharton.mosaic.tty.terminal.asTerminalIn
import kotlin.io.path.createParentDirectories
import kotlin.io.path.writeText
import kotlin.reflect.KProperty
import kotlinx.coroutines.runBlocking

fun main(vararg args: String) {
	TerminalCapabilityCommand().main(args)
}

private class TerminalCapabilityCommand : CliktCommand("terminal-capability") {
	private val output by option(envvar = "MOSAIC_TERMINAL_CAPABILITY_OUTPUT")
		.help("Path into which the output will also be written")
		.path()

	override fun run() = runBlocking<Unit> {
		val tty = checkNotNull(Tty.tryBind()) { "No TTY" }

		val text = tty.asTerminalIn(this).use { terminal ->
			buildString {
				appendLine(terminal.name)
				appendLine()
				Terminal.Capabilities::class
					.members
					.filterIsInstance<KProperty<*>>()
					.sortedBy { it.name }
					.forEach { capabilityProperty ->
						val value = capabilityProperty.call(terminal.capabilities)
						appendLine("${capabilityProperty.name}: $value")
					}
			}
		}
		print(text)

		output?.let { output ->
			output.createParentDirectories()
			output.writeText(text)
		}
	}
}
