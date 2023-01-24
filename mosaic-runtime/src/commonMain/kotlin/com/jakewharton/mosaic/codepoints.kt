@file:Suppress(
	"EXTENSION_SHADOWED_BY_MEMBER",
	"KotlinRedundantDiagnosticSuppress",
)

package com.jakewharton.mosaic

import de.cketti.codepoints.CodePoints

// TODO Switch to https://github.com/cketti/kotlin-codepoints/issues/8 once released.
fun StringBuilder.appendCodePoint(codePoint: Int): StringBuilder = apply {
	if (CodePoints.charCount(codePoint) > 1) {
		append(CodePoints.highSurrogate(codePoint))
		append(CodePoints.lowSurrogate(codePoint))
	} else {
		append(codePoint.toChar())
	}
}
