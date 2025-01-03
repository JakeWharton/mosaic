package com.jakewharton.mosaic

import assertk.Assert
import assertk.assertFailure as originalAssertFailure
import assertk.assertions.isInstanceOf

inline fun <reified T : Throwable> assertFailure(block: () -> Unit): Assert<T> {
	return originalAssertFailure(block).isInstanceOf<T>()
}
