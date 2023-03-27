package com.jakewharton.mosaic

import androidx.compose.runtime.snapshots.SnapshotStateList

const val s = " "

fun <T> snapshotStateListOf(vararg values: T): SnapshotStateList<T> {
	return SnapshotStateList<T>().apply { addAll(values) }
}
