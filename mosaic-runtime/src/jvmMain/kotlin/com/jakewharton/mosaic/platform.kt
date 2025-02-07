package com.jakewharton.mosaic

import java.nio.CharBuffer
import java.nio.charset.StandardCharsets.UTF_8
import org.fusesource.jansi.AnsiConsole

internal actual fun env(name: String): String? {
	return System.getenv(name)
}

private val out = AnsiConsole.out()!!.also { AnsiConsole.systemInstall() }
private val encoder = UTF_8.newEncoder()!!

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

internal actual typealias AtomicBoolean = java.util.concurrent.atomic.AtomicBoolean

@Suppress("NOTHING_TO_INLINE", "EXTENSION_SHADOWED_BY_MEMBER")
internal actual inline fun AtomicBoolean.set(value: Boolean) {
	set(value)
}

@Suppress("NOTHING_TO_INLINE", "EXTENSION_SHADOWED_BY_MEMBER")
internal actual inline fun AtomicBoolean.compareAndSet(expect: Boolean, update: Boolean): Boolean {
	return compareAndSet(expect, update)
}

@Suppress("NOTHING_TO_INLINE")
internal actual inline fun atomicBooleanOf(initialValue: Boolean): AtomicBoolean {
	return AtomicBoolean(initialValue)
}

@Suppress("NOTHING_TO_INLINE")
internal actual inline fun nanoTime(): Long = System.nanoTime()
