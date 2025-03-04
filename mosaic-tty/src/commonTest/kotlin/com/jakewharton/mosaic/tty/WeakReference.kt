package com.jakewharton.mosaic.tty

internal expect class WeakReference<T : Any> {
	constructor(referred: T)
	fun get(): T?
}
