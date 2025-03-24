package example

import kotlin.text.format as jvmFormat

actual fun String.Companion.format(formatString: String, formatArg: Float): String =
	String.jvmFormat(formatString, formatArg)

actual fun String.Companion.format(formatString: String, formatArg: Double): String =
	String.jvmFormat(formatString, formatArg)

actual fun String.Companion.format(formatString: String, formatArg1: Int, formatArg2: Long, formatArg3: Long): String =
	String.jvmFormat(formatString, formatArg1, formatArg2, formatArg3)

actual fun String.Companion.format(
	formatString: String,
	formatArg1: Long,
	formatArg2: Long,
	formatArg3: Long,
	formatArg4: Long,
): String =
	String.jvmFormat(formatString, formatArg1, formatArg2, formatArg3, formatArg4)
