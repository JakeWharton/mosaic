package com.jakewharton.mosaic.animation

internal expect class AtomicReference<T>

internal expect inline fun <T> AtomicReference<T>.get(): T

internal expect inline fun <T> AtomicReference<T>.set(value: T)

internal expect inline fun <T> AtomicReference<T>.compareAndSet(expect: T, update: T): Boolean

internal expect inline fun <T> atomicReferenceOf(initialValue: T): AtomicReference<T>
