package example.utils

import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@ExperimentalAtomicApi
internal inline fun <T> AtomicReference<T>.updateAndGet(updater: (T) -> T): T {
	while (true) {
		val oldValue = load()
		val newValue = updater(oldValue)
		if (compareAndSet(oldValue, newValue)) {
			return newValue
		}
	}
}
