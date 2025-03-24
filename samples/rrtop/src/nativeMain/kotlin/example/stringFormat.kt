@file:OptIn(ExperimentalForeignApi::class)

package example

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString
import platform.posix.sprintf

actual fun String.Companion.format(formatString: String, formatArg: Float): String {
	return memScoped {
		val buffer = allocArray<ByteVar>(64)
		sprintf(buffer, formatString, formatArg)
		buffer.toKString()
	}
}

actual fun String.Companion.format(formatString: String, formatArg: Double): String {
	return memScoped {
		val buffer = allocArray<ByteVar>(64)
		sprintf(buffer, formatString, formatArg)
		buffer.toKString()
	}
}

actual fun String.Companion.format(formatString: String, formatArg1: Int, formatArg2: Long, formatArg3: Long): String {
	return memScoped {
		val buffer = allocArray<ByteVar>(64)
		sprintf(buffer, formatString, formatArg1, formatArg2, formatArg3)
		buffer.toKString()
	}
}

actual fun String.Companion.format(
	formatString: String,
	formatArg1: Long,
	formatArg2: Long,
	formatArg3: Long,
	formatArg4: Long,
): String {
	return memScoped {
		val buffer = allocArray<ByteVar>(64)
		sprintf(buffer, formatString, formatArg1, formatArg2, formatArg3, formatArg4)
		buffer.toKString()
	}
}
