@file:Suppress("NOTHING_TO_INLINE")

package example.models

import androidx.collection.MutableLongList
import example.utils.firstFromRawIntIntPair
import example.utils.secondFromRawIntIntPair

typealias Snake = MutableLongList

inline val Snake.rawHead: Long get() = first()

inline val Snake.head: Point
	get() {
		val headLong = first()
		return Point(headLong.firstFromRawIntIntPair, headLong.secondFromRawIntIntPair)
	}

inline fun Snake.copy(newSize: Int = this.size): Snake {
	val newSnake = Snake(newSize)
	newSnake.addAll(this)
	return newSnake
}
