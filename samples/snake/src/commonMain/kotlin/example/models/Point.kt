@file:Suppress("NOTHING_TO_INLINE")

package example.models

import androidx.collection.IntIntPair
import example.utils.firstFromRawIntIntPair
import example.utils.secondFromRawIntIntPair

typealias Point = IntIntPair

inline val Point.x: Int get() = first

inline val Point.y: Int get() = second

inline fun Long.asPoint(): Point {
	return Point(firstFromRawIntIntPair, secondFromRawIntIntPair)
}
