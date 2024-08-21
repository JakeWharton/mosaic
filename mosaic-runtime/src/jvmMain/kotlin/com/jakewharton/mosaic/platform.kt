package com.jakewharton.mosaic

import java.util.concurrent.atomic.AtomicBoolean as JavaAtomicBoolean

internal actual class AtomicBoolean actual constructor(initialValue: Boolean) {
	private val delegate = JavaAtomicBoolean(initialValue)

	actual fun set(value: Boolean) {
		delegate.set(value)
	}

	actual fun compareAndSet(expect: Boolean, update: Boolean): Boolean {
		return delegate.compareAndSet(expect, update)
	}
}
