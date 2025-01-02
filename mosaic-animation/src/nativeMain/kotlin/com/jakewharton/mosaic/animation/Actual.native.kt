package com.jakewharton.mosaic.animation

@Suppress("ACTUAL_WITHOUT_EXPECT") // https://youtrack.jetbrains.com/issue/KT-37316
internal actual typealias AtomicReference<T> = kotlin.concurrent.AtomicReference<T>

@Suppress("NOTHING_TO_INLINE")
internal actual inline fun <T> AtomicReference<T>.get(): T {
	return value
}

@Suppress("NOTHING_TO_INLINE")
internal actual inline fun <T> AtomicReference<T>.set(value: T) {
	this.value = value
}

@Suppress("NOTHING_TO_INLINE", "EXTENSION_SHADOWED_BY_MEMBER")
internal actual inline fun <T> AtomicReference<T>.compareAndSet(expect: T, update: T): Boolean {
	return compareAndSet(expect, update)
}

@Suppress("NOTHING_TO_INLINE")
internal actual inline fun <T> atomicReferenceOf(initialValue: T): AtomicReference<T> {
	return AtomicReference(initialValue)
}
