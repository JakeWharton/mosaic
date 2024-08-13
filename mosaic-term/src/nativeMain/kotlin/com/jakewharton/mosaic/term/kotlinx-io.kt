package com.jakewharton.mosaic.term

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.io.Buffer
import kotlinx.io.IOException
import kotlinx.io.RawSink
import kotlinx.io.RawSource
import kotlinx.io.Sink
import kotlinx.io.UnsafeIoApi
import kotlinx.io.unsafe.UnsafeBufferOperations.maxSafeWriteCapacity
import kotlinx.io.unsafe.UnsafeBufferOperations.readFromHead
import kotlinx.io.unsafe.UnsafeBufferOperations.writeToTail
import kotlinx.io.writeDecimalLong
import platform.posix.close
import platform.posix.errno
import platform.posix.fsync
import platform.posix.read
import platform.posix.write

@UnsafeIoApi
@ExperimentalForeignApi
internal class PosixFileDescriptorRawSink(
	private val fd: Int,
) : RawSink {
	override fun write(source: Buffer, byteCount: Long) {
		var bytesRemaining = byteCount
		while (bytesRemaining > 0) {
			readFromHead(source) { bytes, startIndexInclusive, endIndexExclusive ->
				val bytesAvailable = (endIndexExclusive - startIndexInclusive).toLong()
				val toWrite = bytesRemaining.coerceAtMost(bytesAvailable)
				val written = bytes.usePinned { pin ->
					write(fd, pin.addressOf(startIndexInclusive), toWrite.toULong())
				}

				bytesRemaining -= written

				written.toInt().also {
					if (it == -1) {
						throwErrno()
					}
				}
			}
		}
	}

	override fun flush() {
		fsync(fd).also {
			if (it == -1) {
				throwErrno()
			}
		}
	}

	override fun close() {
		close(fd).also {
			if (it == -1) {
				throwErrno()
			}
		}
	}
}

@UnsafeIoApi
@ExperimentalForeignApi
internal class PosixFileDescriptorRawSource(
	private val fd: Int,
) : RawSource {
	override fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
		// This local does double-duty:
		// - Holds the computed maximum bytes we expect to read (byteCount or Segment.SIZE).
		// - Stores the actual read bytes until https://github.com/Kotlin/kotlinx-io/issues/360.
		var temp: Long = byteCount.coerceAtMost(maxSafeWriteCapacity.toLong())

		writeToTail(sink, temp.toInt()) { bytes, startIndexInclusive, endIndexExclusive ->
			val toRead = (endIndexExclusive - startIndexInclusive).toULong()
			val read = bytes.usePinned { pin ->
				read(fd, pin.addressOf(startIndexInclusive), toRead)
			}

			temp = read

			read.toInt().also {
				if (it == -1) {
					throwErrno()
				}
			}
		}
		return temp
	}

	override fun close() {
		close(fd).also {
			if (it == -1) {
				throwErrno()
			}
		}
	}
}

private fun throwErrno(): Nothing {
	throw IOException("Operation failed: $errno")
}
