package example

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UnsafeNumber
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.posix.STDIN_FILENO
import platform.posix.read

@OptIn(ExperimentalForeignApi::class, UnsafeNumber::class)
internal actual fun stdinRead(bytes: ByteArray, offset: Int, length: Int): Int {
	return bytes.usePinned { pin ->
		read(STDIN_FILENO, pin.addressOf(offset), length.convert()).convert()
	}
}
