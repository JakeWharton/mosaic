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
	actual override fun close() {
		val ptr = ptr
		if (ptr != 0L) {
			Jni.streamsFree(ptr)
			this.ptr = 0L
		}
	}
}
