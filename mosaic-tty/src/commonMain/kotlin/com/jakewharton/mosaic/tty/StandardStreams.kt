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
	 * This function will continue to write to this process' output stream even if
	 * [interceptOtherWrites] is called.
	 */
	public fun writeOutput(buffer: ByteArray, offset: Int, count: Int): Int

	/**
	 * Write up to [count] bytes from [buffer] at [offset] to the error stream.
	 * The number of bytes written will be returned.
	 * This function will continue to write to this process' error stream even if
	 * [interceptOtherWrites] is called.
	 */
	public fun writeError(buffer: ByteArray, offset: Int, count: Int): Int

	/**
	 * Begin intercepting writes to the output and errors streams which are NOT performed through this
	 * type's [writeOutput] and [writeError] functions.
	 *
	 * Writes to the "normal" output and error streams (such as through functions like [println]) can
	 * be read using the functions on the returned [InterceptedStreams] instance. Call
	 * [InterceptedStreams.close] to restore sending writes to the original output or error stream.
	 *
	 * Since intercepting writes changes global process state, subsequent calls to this function
	 * will throw an exception until [InterceptedStreams.close] is called.
	 */
	public fun interceptOtherWrites(): InterceptedStreams

	override fun close()

	/**
	 * The intercepted process output and error streams which can be read to capture
	 * inadvertent writes.
	 * */
	public class InterceptedStreams : AutoCloseable {
		/**
		 * Read up to [count] bytes into [buffer] at [offset] from the intercepted output stream.
		 * The number of bytes read will be returned. 0 will be returned if [interruptOutputRead] is
		 * called while waiting for data.
		 *
		 * @see readOutputWithTimeout
		 * @see interruptOutputRead
		 */
		public fun readOutput(buffer: ByteArray, offset: Int, count: Int): Int

		/**
		 * Read up to [count] bytes into [buffer] at [offset] from the intercepted output stream.
		 * The number of bytes read will be returned. 0 will be returned if [interruptOutputRead] is
		 * called while waiting for data, or if at least [timeoutMillis] have passed without data.
		 *
		 * @param timeoutMillis A value of 0 will perform a non-blocking read. Otherwise, valid values
		 * are 1 to 999 which represent a maximum time (in milliseconds) to wait for data. Note: This
		 * value is not validated.
		 * @see readOutput
		 * @see interruptOutputRead
		 */
		public fun readOutputWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int

		/** Signal blocking calls to [readOutput] or [readOutputWithTimeout] to wake up and return 0. */
		public fun interruptOutputRead()

		/**
		 * Read up to [count] bytes into [buffer] at [offset] from the intercepted error stream.
		 * The number of bytes read will be returned. 0 will be returned if [interruptErrorRead] is
		 * called while waiting for data.
		 *
		 * @see readErrorWithTimeout
		 * @see interruptErrorRead
		 */
		public fun readError(buffer: ByteArray, offset: Int, count: Int): Int

		/**
		 * Read up to [count] bytes into [buffer] at [offset] from the intercepted error stream.
		 * The number of bytes read will be returned. 0 will be returned if [interruptErrorRead] is
		 * called while waiting for data, or if at least [timeoutMillis] have passed without data.
		 *
		 * @param timeoutMillis A value of 0 will perform a non-blocking read. Otherwise, valid values
		 * are 1 to 999 which represent a maximum time (in milliseconds) to wait for data. Note: This
		 * value is not validated.
		 * @see readError
		 * @see interruptErrorRead
		 */
		public fun readErrorWithTimeout(buffer: ByteArray, offset: Int, count: Int, timeoutMillis: Int): Int

		/** Signal blocking calls to [readError] or [readErrorWithTimeout] to wake up and return 0. */
		public fun interruptErrorRead()

		/**
		 * Restore sending writes to the original output or error stream. Discards any unread data in
		 * the intercepted output and error streams.
		 */
		override fun close()
	}
}
