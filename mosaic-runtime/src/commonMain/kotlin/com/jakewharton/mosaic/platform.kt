package com.jakewharton.mosaic

internal expect fun platformDisplay(chars: CharSequence)

internal expect class AtomicBoolean(initialValue: Boolean) {

	fun set(value: Boolean)

	fun compareAndSet(expect: Boolean, update: Boolean): Boolean
}
