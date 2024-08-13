package com.jakewharton.mosaic.term

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.set
import kotlinx.cinterop.usePinned
import kotlinx.io.buffered
import platform.posix.BRKINT
import platform.posix.CSIZE
import platform.posix.ECHO
import platform.posix.ECHONL
import platform.posix.ICANON
import platform.posix.ICRNL
import platform.posix.IEXTEN
import platform.posix.IGNBRK
import platform.posix.IGNCR
import platform.posix.INLCR
import platform.posix.ISIG
import platform.posix.ISTRIP
import platform.posix.IXON
import platform.posix.OPOST
import platform.posix.O_RDWR
import platform.posix.PARENB
import platform.posix.PARMRK
import platform.posix.TCSAFLUSH
import platform.posix.VMIN
import platform.posix.VTIME
import platform.posix.errno
import platform.posix.fileno
import platform.posix.open
import platform.posix.read
import platform.posix.size_t
import platform.posix.tcgetattr
import platform.posix.tcsetattr
import platform.posix.termios

@OptIn(ExperimentalForeignApi::class)
fun main() = memScoped {
	val stdoutFd = fileno(platform.posix.stdout).also {
		if (it == -1) error(errno)
	}
	val stdout = PosixFileDescriptorRawSink(stdoutFd).buffered()

	val ttyFd = open("/dev/tty", O_RDWR, 0).also {
		if (it == -1) error(errno)
	}
	val tty = PosixFileDescriptorRawSource(ttyFd)

	val initial = alloc<termios>()
	tcgetattr(ttyFd, initial.ptr)
	try {
		val state = alloc<termios>()
		tcgetattr(ttyFd, state.ptr)
		state.c_iflag = state.c_iflag and (IGNBRK or BRKINT or PARMRK or ISTRIP or INLCR or IGNCR or ICRNL or IXON).inv().toULong()
		state.c_oflag = state.c_oflag and (OPOST).inv().toULong()
		state.c_lflag = state.c_lflag and (ECHO or ECHONL or ICANON or ISIG or IEXTEN).inv().toULong()
		state.c_cflag = state.c_cflag and (CSIZE or PARENB).inv().toULong()
		state.c_cc[VMIN] = 1.toUByte()
		state.c_cc[VTIME] = 0.toUByte()
		tcsetattr(ttyFd, TCSAFLUSH, state.ptr)

		stdout.apply {
//			writeDecPrivateModeRequest(1004)
//			writeDecPrivateModeRequest(2048)
			writeDecPrivateModeSet(/*1002,*/ 1003, 1006)//, 1004, 1006)
//			writeNameAndVersionRequest()
			flush()
		}

//		print(
////			"\u001b[c" + // Primary device attrs
////			"\u001b[=c" + // Tertiary device attrs
////			"\u001b[5n" + // Device status report
////			"\u001b[>0q" +  // xterm version
////			"\u001b[?u" + // Kitty keyboard
////			"\u001b_Gi=1,a=q\u001b\\" + // Kitty graphics
////			"\u001b[?2;1;0S" + // Sixel geometry
////			"\u001b[?1004\$p" + // Focus
////			"\u001b[?1004h" + // Focus
////			"\u001b[14t" + // Kitty window size
////				"\u001b[?2048\$p" + // In-band resize query
////				"\u001b[?2048h" + // In-band resize enable
//			""
//		)

		val bufferSize = 1024 // TODO const
		val buffer = UByteArray(bufferSize) // TODO ByteArray is probably fine?
		buffer.usePinned { pin ->
			var offset = 0
			read@ while (true) {
				val ptr = pin.addressOf(offset)
				val limit = (bufferSize - offset).convert<size_t>()
				val end = read(ttyFd, ptr, limit).toInt()

				// Assume that we will successfully consume the newly-read data in its entirety.
				offset = 0

				var start = 0
				parse@ while (start < end) {
					print(buildString {
						append(">>> ")
						buffer.copyOf(end).joinTo(this, "") { it.toString(16).padStart(2, '0') }
						append("\r\n    ")
						repeat(start) {
							append("  ")
						}
						append("[")
						repeat(end - start - 1) {
							append("  ")
						}
						append("]\r\n")
					})

					val result = parseInputEvent(buffer, start, end)
					if (result != null) {
						val event = result.event
						print("<<< $event\r\n") // TODO write to channel

						if (event is CodepointEvent && event.ctrl && event.codepoint == 'c'.code) {
							break@read
						}

						start += result.bytesConsumed
						continue@parse
					}

					// Buffer underflow! Move remaining bytes to index 0 and trigger a read to fill more.
					buffer.copyInto(buffer, 0, start, end)
					offset = end - start + 1
				}
			}
		}
	} finally {
		stdout.writeDecPrivateModeReset(1002, 1003, 1004, 1006, 2048)
		stdout.flush()
		tcsetattr(ttyFd, TCSAFLUSH, initial.ptr)
	}
}
