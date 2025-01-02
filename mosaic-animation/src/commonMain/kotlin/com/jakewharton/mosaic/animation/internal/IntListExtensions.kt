package com.jakewharton.mosaic.animation.internal

import androidx.collection.IntList
import kotlin.jvm.JvmOverloads

/**
 * Searches this list the specified element in the range defined by [fromIndex] and [toIndex].
 * The list is expected to be sorted into ascending order according to the natural ordering of
 * its elements, otherwise the result is undefined.
 *
 * [fromIndex] must be >= 0 and < [toIndex], and [toIndex] must be <= [size], otherwise an an
 * [IndexOutOfBoundsException] will be thrown.
 *
 * @return the index of the element if it is contained in the list within the specified range.
 *   otherwise, the inverted insertion point `(-insertionPoint - 1)`. The insertion point is
 *   defined as the index at which the element should be inserted, so that the list remains
 *   sorted.
 */
@JvmOverloads
internal fun IntList.binarySearch(element: Int, fromIndex: Int = 0, toIndex: Int = size): Int {
	if (fromIndex < 0 || fromIndex >= toIndex || toIndex > size) {
		throw IndexOutOfBoundsException("")
	}

	var low = fromIndex
	var high = toIndex - 1

	while (low <= high) {
		val mid = low + high ushr 1
		val midVal = get(mid)
		if (midVal < element) {
			low = mid + 1
		} else if (midVal > element) {
			high = mid - 1
		} else {
			return mid // key found
		}
	}

	return -(low + 1) // key not found.
}
