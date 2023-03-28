package com.jakewharton.mosaic.text

import de.cketti.codepoints.codePointCount

internal class TextLayout {
	var value: String = ""
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

	var lines: List<String> = emptyList()
		private set
		get() {
			check(!dirty) { "Missing call to measure()" }
			return field
		}

	private var dirty = true

	fun measure() {
		if (!dirty) return

		val lines = value.split('\n')
		width = lines.maxOf { it.codePointCount(0, it.length) }
		height = lines.size
		this.lines = lines
		dirty = false
	}
}
