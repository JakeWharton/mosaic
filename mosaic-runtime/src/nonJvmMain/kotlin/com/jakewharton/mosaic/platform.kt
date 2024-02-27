package com.jakewharton.mosaic

internal actual fun platformDisplay(chars: CharSequence) {
	print(chars.toString())
}

internal actual class AtomicBoolean actual constructor(initialValue: Boolean) {
	private var value: Boolean = initialValue

	actual fun set(value: Boolean) {
		this.value = value
	}

	actual fun compareAndSet(expect: Boolean, update: Boolean): Boolean {
		if (value != expect) return false
		value = update
		return true
	}
}
