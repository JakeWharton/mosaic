package com.jakewharton.mosaic.tty

/** The standard input, output, and error streams. */
public expect class StandardStreams : AutoCloseable {
	public companion object {
		/**
		 * Initialize a [StandardStreams] instance bound to the standard input, output, and error
		 * streams for this application.
		 *
		 * @throws IOException If an error occurred binding to the streams.
		 */
		public fun bind(): StandardStreams
	}

	/**
	 * True if the input is connected to the TTY.
	 *
	 * If the program is running interactively this will (usually) be true, and it means [readInput]
	 * will come from the terminal. If the user has piped the output of another program into this one,
	 * explicitly redirected data to the input stream, or is simply running this program in a
	 * non-interactive context (such as within an IDE, build tool, or headlessly), this will be false.
	 */
	public fun isInputTty(): Boolean

	/**
	 * True if the output is connected to the TTY.
	 *
	 * If the program is running interactively this will (usually) be true, and it means [writeOutput]
	 * will go to the terminal. If the user has piped the output of this program into another,
	 * explicitly redirected the output stream somewhere, or is simply running this program in a
	 * non-interactive context (such as within an IDE, build tool, or headlessly), this will be false.
	 */
	public fun isOutputTty(): Boolean

	/**
	 * True if the error is connected to the TTY.
	 *
	 * If the program is running interactively this will (usually) be true, and it means [writeOutput]
	 * will go to the terminal. If the user has piped the output of this program into another,
	 * explicitly redirected the output stream somewhere, or is simply running this program in a
	 * non-interactive context (such as within an IDE, build tool, or headlessly), this will be false.
	 */
	public fun isErrorTty(): Boolean

	/**
	 * Read up to [count] bytes into [buffer] at [offset] from the input stream.
	 * The number of bytes read will be returned. 0 will be returned if [interruptInputRead] is called
	 * while waiting for data.
	 *
	 * @see readInputWithTimeout
	 * @see interruptInputRead
	 */
	public fun readInput(buffer: ByteArray, offset: Int, count: Int): Int

	/**
	 * Read up to [count] bytes into [buffer] at [offset] from the input stream.
	 * The number of bytes read will be returned. 0 will be returned if [interruptInputRead] is called
	 * while waiting for data, or if at least [timeoutMillis] have passed without data.
	 *
	 * @param timeoutMillis A value of 0 will perform a non-blocking read. Otherwise, valid values
	 * are 1 to 999 which represent a maximum time (in milliseconds) to wait for data. Note: This
	 * value is not validated.
	 * @see readInput
	 * @see interruptInputRead
	 */
	public fun readInputWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int

	/** Signal blocking calls to [readInput] or [readInputWithTimeout] to wake up and return 0. */
	public fun interruptInputRead()

	/**
	 * Write up to [count] bytes from [buffer] at [offset] to the output stream.
	 * The number of bytes written will be returned.
	 */
	public fun writeOutput(buffer: ByteArray, offset: Int, count: Int): Int

	/**
	 * Write up to [count] bytes from [buffer] at [offset] to the error stream.
	 * The number of bytes written will be returned.
	 */
	public fun writeError(buffer: ByteArray, offset: Int, count: Int): Int

	public fun interceptOtherWrites(): InterceptedStreams

	override fun close()

	public class InterceptedStreams : AutoCloseable {
		public fun readOutput(buffer: ByteArray, offset: Int, count: Int): Int
		public fun readOutputWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int
		public fun interruptOutputRead()

		public fun readError(buffer: ByteArray, offset: Int, count: Int): Int
		public fun readErrorWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int
		public fun interruptErrorRead()

		override fun close()
	}
}
