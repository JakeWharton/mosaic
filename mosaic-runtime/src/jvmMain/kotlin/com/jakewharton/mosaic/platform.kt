package com.jakewharton.mosaic

import com.jakewharton.mosaic.ui.unit.IntSize
import java.lang.management.ManagementFactory
import java.nio.CharBuffer
import java.nio.charset.StandardCharsets.UTF_8
import org.jline.nativ.CLibrary
import org.jline.terminal.TerminalBuilder

private const val STDOUT_FILENO = 1

/* todo: invoking enterRawMode - it is a hack to use key events in samples */
private val terminal = TerminalBuilder.terminal().also { it.enterRawMode() }
private val out = terminal.output()
private val encoder = UTF_8.newEncoder()

internal actual fun platformDisplay(chars: CharSequence) {
	// Write a single byte array to stdout to create an atomic visual change. If you instead write
	// the string, it will be UTF-8 encoded using an intermediate buffer that appears to be
	// periodically flushed to the underlying byte stream. This will cause fraction-of-a-second
	// flickers of broken content. Note that this only occurs with the AnsiConsole stream, but
	// there's no harm in doing it unconditionally.
	val bytes = encoder.encode(CharBuffer.wrap(chars))
	out.write(bytes.array(), 0, bytes.limit())

	// Explicitly flush to ensure the trailing line clear is sent. Empirically, this appears to be
	// buffered and not processed until the next frame, or not at all on the final frame.
	out.flush()
}

internal actual fun getPlatformTerminalSize(): IntSize {
	return IntSize(terminal.width, terminal.height)
}

internal actual fun getEnv(key: String): String? = System.getenv(key)

// Depending on how IntelliJ is configured, it might use its own Java agent
internal actual fun runningInIdeaJavaAgent(): Boolean {
	return try {
		val bean = ManagementFactory.getRuntimeMXBean()
		val jvmArgs = bean.inputArguments
		jvmArgs.any { it.startsWith("-javaagent") && "idea_rt.jar" in it }
	} catch (e: SecurityException) {
		false
	}
}

internal actual fun stdoutInteractive(): Boolean = CLibrary.isatty(STDOUT_FILENO) != 0
