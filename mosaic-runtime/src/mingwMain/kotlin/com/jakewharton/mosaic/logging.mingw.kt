package com.jakewharton.mosaic

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cstr
import platform.posix.write

@OptIn(ExperimentalForeignApi::class)
internal actual inline fun write(fileDescriptor: Int, str: String) {
	write(fileDescriptor, str.cstr, str.length.toUInt())
}
