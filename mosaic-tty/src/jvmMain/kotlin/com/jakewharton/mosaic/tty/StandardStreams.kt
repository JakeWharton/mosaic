package com.jakewharton.mosaic.tty

public actual class StandardStreams internal constructor(
	private var ptr: Long,
) : AutoCloseable {
	public actual companion object {
		@JvmStatic
		public actual fun bind(): StandardStreams {
			return StandardStreams(Jni.streamsInit())
		}
	}

	@Throws(IOException::class)
	public actual fun isInputTty(): Boolean {
		return Jni.streamsInputIsTty(ptr)
	}

	@Throws(IOException::class)
	public actual fun isOutputTty(): Boolean {
		return Jni.streamsOutputIsTty(ptr)
	}

	@Throws(IOException::class)
	public actual fun isErrorTty(): Boolean {
		return Jni.streamsErrorIsTty(ptr)
	}

	@Throws(IOException::class)
	public actual fun readInput(buffer: ByteArray, offset: Int, count: Int): Int {
		return Jni.streamsReadInput(ptr, buffer, offset, count)
	}

	@Throws(IOException::class)
	public actual fun readInputWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
		return Jni.streamsReadInputWithTimeout(ptr, buffer, offset, count, timeoutMillis)
	}

	@Throws(IOException::class)
	public actual fun interruptInputRead() {
		return Jni.streamsInterruptInputRead(ptr)
	}

	@Throws(IOException::class)
	public actual fun writeOutput(buffer: ByteArray, offset: Int, count: Int): Int {
		return Jni.streamsWriteOutput(ptr, buffer, offset, count)
	}

	@Throws(IOException::class)
	public actual fun writeError(buffer: ByteArray, offset: Int, count: Int): Int {
		return Jni.streamsWriteError(ptr, buffer, offset, count)
	}

	@Throws(IOException::class)
	actual override fun close() {
		val ptr = ptr
		if (ptr != 0L) {
			Jni.streamsFree(ptr)
			this.ptr = 0L
		}
	}
}
