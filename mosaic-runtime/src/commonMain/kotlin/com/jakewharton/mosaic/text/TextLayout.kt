package com.jakewharton.mosaic.text

import de.cketti.codepoints.codePointAt

internal abstract class TextLayout<T : CharSequence>(initialValue: T) {

	var value: T = initialValue
		set(value) {
			if (value != field) {
				dirty = true
				field = value
			}
		}

	var width: Int = -1
		private set
		get() {
			check(!dirty) { "Missing call to measure()" }
			return field
		}

	var height: Int = -1
		private set
		get() {
			check(!dirty) { "Missing call to measure()" }
			return field
		}

	var lines: List<T> = emptyList()
		private set
		get() {
			check(!dirty) { "Missing call to measure()" }
			return field
		}

	private var dirty = true

	fun measure() {
		if (!dirty) return

		val lines = value.splitByLines()
		width = lines.maxOf { line ->
			var w = 0
			var i = 0
			while (i < line.length) {
				val cp = line.codePointAt(i)
				w += charWidth(cp)
				i += if (line[i].isHighSurrogate()) 2 else 1
			}
			w
		}
		height = lines.size
		this.lines = lines
		dirty = false
	}

	protected abstract fun T.splitByLines(): List<T>
}

internal class StringTextLayout : TextLayout<String>(initialValue = "") {

	override fun String.splitByLines(): List<String> {
		return this.split("\n")
	}
}

internal class AnnotatedStringTextLayout :
	TextLayout<AnnotatedString>(
		initialValue = emptyAnnotatedString(),
	) {

	override fun AnnotatedString.splitByLines(): List<AnnotatedString> {
		return this.split("\n")
	}
}
