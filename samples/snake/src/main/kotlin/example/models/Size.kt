@file:Suppress("NOTHING_TO_INLINE")

package example.models

import androidx.collection.IntIntPair

typealias Size = IntIntPair

inline val Size.width: Int get() = first

inline val Size.height: Int get() = second
