@file:Suppress("NOTHING_TO_INLINE")

package example.utils

import androidx.collection.IntIntPair

inline fun IntIntPair.asLong(): Long {
	return packInts(first, second)
}

inline fun packInts(val1: Int, val2: Int): Long {
	return (val1.toLong() shl 32) or (val2.toLong() and 0xFFFFFFFF)
}

inline val Long.firstFromRawIntIntPair: Int get() = (this shr 32).toInt()

inline val Long.secondFromRawIntIntPair: Int get() = (this and 0xFFFFFFFF).toInt()
