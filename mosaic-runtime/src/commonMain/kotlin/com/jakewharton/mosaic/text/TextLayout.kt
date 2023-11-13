package com.jakewharton.mosaic.text

import de.cketti.codepoints.codePointCount

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
		width = lines.maxOf { it.codePointCount(0, it.length) }
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

internal class AnnotatedStringTextLayout : TextLayout<AnnotatedString>(
	initialValue = emptyAnnotatedString(),
) {

	override fun AnnotatedString.splitByLines(): List<AnnotatedString> {
		return this.split("\n")
	}
}
