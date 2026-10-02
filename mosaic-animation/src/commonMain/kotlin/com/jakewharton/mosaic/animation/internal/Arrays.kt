package com.jakewharton.mosaic.animation.internal

// copy of List.kotlin.collections.binarySearch adapted for Float
internal fun FloatArray.binarySearch(element: Float, fromIndex: Int = 0, toIndex: Int = size): Int {
	rangeCheck(size, fromIndex, toIndex)

	var low = fromIndex
	var high = toIndex - 1

	while (low <= high) {
		val mid = (low + high).ushr(1) // safe from overflows
		val midVal = get(mid)
		val cmp = midVal.compareTo(element)

		if (cmp < 0) {
			low = mid + 1
		} else if (cmp > 0) {
			high = mid - 1
		} else {
			return mid // key found
		}
	}
	return -(low + 1) // key not found
}

private fun rangeCheck(size: Int, fromIndex: Int, toIndex: Int) {
	when {
		fromIndex > toIndex -> throw IllegalArgumentException("fromIndex ($fromIndex) is greater than toIndex ($toIndex).")
		fromIndex < 0 -> throw IndexOutOfBoundsException("fromIndex ($fromIndex) is less than zero.")
		toIndex > size -> throw IndexOutOfBoundsException("toIndex ($toIndex) is greater than size ($size).")
	}
}
