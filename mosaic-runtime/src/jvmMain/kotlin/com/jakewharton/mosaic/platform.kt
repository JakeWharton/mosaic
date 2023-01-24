package com.jakewharton.mosaic

import org.fusesource.jansi.AnsiConsole
import java.nio.CharBuffer
import java.nio.charset.StandardCharsets.UTF_8

private val out = AnsiConsole.out()!!
private val encoder = UTF_8.newEncoder()!!

internal actual fun platformRender(chars: CharSequence) {
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
