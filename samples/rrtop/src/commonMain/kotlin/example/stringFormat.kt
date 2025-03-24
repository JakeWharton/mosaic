package example

expect fun String.Companion.format(formatString: String, formatArg: Float): String
expect fun String.Companion.format(formatString: String, formatArg: Double): String
expect fun String.Companion.format(formatString: String, formatArg1: Int, formatArg2: Long, formatArg3: Long): String
expect fun String.Companion.format(
	formatString: String,
	formatArg1: Long,
	formatArg2: Long,
	formatArg3: Long,
	formatArg4: Long,
): String
