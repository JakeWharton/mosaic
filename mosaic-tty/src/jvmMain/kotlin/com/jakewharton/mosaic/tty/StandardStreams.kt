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

	public actual fun interceptOtherWrites(): InterceptedStreams {
		Jni.streamsInterceptStart(ptr)
		return InterceptedStreams(ptr)
	}

	@Throws(IOException::class)
	actual override fun close() {
		val ptr = ptr
		if (ptr != 0L) {
			Jni.streamsFree(ptr)
			this.ptr = 0L
		}
	}

	public actual class InterceptedStreams internal constructor(
		private val ptr: Long,
	) : AutoCloseable {
		public actual fun readOutput(buffer: ByteArray, offset: Int, count: Int): Int {
			return Jni.streamsReadInterceptedOutput(ptr, buffer, offset, count)
		}

		public actual fun readOutputWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
			return Jni.streamsReadInterceptedOutputWithTimeout(ptr, buffer, offset, count, timeoutMillis)
		}

		public actual fun interruptOutputRead() {
			Jni.streamsInterruptInterceptedOutputRead(ptr)
		}

		public actual fun readError(buffer: ByteArray, offset: Int, count: Int): Int {
			return Jni.streamsReadInterceptedError(ptr, buffer, offset, count)
		}

		public actual fun readErrorWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int {
			return Jni.streamsReadInterceptedErrorWithTimeout(ptr, buffer, offset, count, timeoutMillis)
		}

		public actual fun interruptErrorRead() {
			Jni.streamsInterruptInterceptedErrorRead(ptr)
		}

		actual override fun close() {
			Jni.streamsInterceptStop(ptr)
		}
	}
}
