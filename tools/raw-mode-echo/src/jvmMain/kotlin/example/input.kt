package example

internal actual fun stdinRead(bytes: ByteArray, offset: Int, length: Int): Int {
	return System.`in`.read(bytes, offset, length)
}
