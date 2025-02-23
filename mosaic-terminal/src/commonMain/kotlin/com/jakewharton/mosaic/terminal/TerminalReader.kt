package com.jakewharton.mosaic.terminal

import com.jakewharton.mosaic.terminal.Tty.terminalReader
import com.jakewharton.mosaic.terminal.event.BracketedPasteEvent
import com.jakewharton.mosaic.terminal.event.CapabilityQueryEvent
import com.jakewharton.mosaic.terminal.event.DebugEvent
import com.jakewharton.mosaic.terminal.event.DecModeReportEvent
import com.jakewharton.mosaic.terminal.event.Event
import com.jakewharton.mosaic.terminal.event.FocusEvent
import com.jakewharton.mosaic.terminal.event.KeyboardEvent
import com.jakewharton.mosaic.terminal.event.KittyGraphicsEvent
import com.jakewharton.mosaic.terminal.event.KittyKeyboardQueryEvent
import com.jakewharton.mosaic.terminal.event.KittyNotificationEvent
import com.jakewharton.mosaic.terminal.event.KittyPointerQueryNameEvent
import com.jakewharton.mosaic.terminal.event.KittyPointerQuerySupportEvent
import com.jakewharton.mosaic.terminal.event.MouseEvent
import com.jakewharton.mosaic.terminal.event.OperatingStatusResponseEvent
import com.jakewharton.mosaic.terminal.event.PaletteColorEvent
import com.jakewharton.mosaic.terminal.event.PrimaryDeviceAttributesEvent
import com.jakewharton.mosaic.terminal.event.ResizeEvent
import com.jakewharton.mosaic.terminal.event.SystemThemeEvent
import com.jakewharton.mosaic.terminal.event.TerminalColorEvent
import com.jakewharton.mosaic.terminal.event.TerminalVersionEvent
import com.jakewharton.mosaic.terminal.event.TertiaryDeviceAttributesEvent
import com.jakewharton.mosaic.terminal.event.UnknownEvent
import com.jakewharton.mosaic.terminal.event.XtermCharacterSizeEvent
import com.jakewharton.mosaic.terminal.event.XtermPixelSizeEvent
import kotlin.concurrent.Volatile
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel

private const val BufferSize = 8 * 1024
private const val BareEscapeDisambiguationReadTimeoutMillis = 100

public class TerminalReader internal constructor(
	private val platformInput: PlatformInput,
	events: Channel<Event>,
	private val emitDebugEvents: Boolean,
) : AutoCloseable {
	private val buffer = ByteArray(BufferSize)
	private var offset = 0
	private var limit = 0

	@TestApi
	internal fun copyBuffer() = buffer.copyOfRange(offset, limit)

	private val _events = events

	/** Events read as a result of calls to [tryReadEvents]. */
	public val events: ReceiveChannel<Event> get() = _events

	/**
	 * Indicate whether Kitty's
	 * [escape code disambiguation](https://sw.kovidgoyal.net/kitty/keyboard-protocol/#disambiguate-escape-codes)
	 * progressive-enhancement is enabled.
	 *
	 * Normally, when a bare escape (`0x1b`) is encountered as the final byte read from the input,
	 * it is not possible to disambiguate this as the start of an escape sequence or as a bare
	 * <kbd>Esc</kbd> key press. The parser will perform a fast disambiguation read to look for
	 * additional bytes to try and guess. Setting this property to true eliminates the disambiguation
	 * read under the assumption that any <kbd>Esc</kbd> key press will be encoded using the Kitty
	 * keyboard protocol.
	 */
	@Volatile
	public var kittyDisambiguateEscapeCodes: Boolean = false

	/**
	 * Indicate whether XTerm's
	 * [UTF-8 extended mouse](https://invisible-island.net/xterm/ctlseqs/ctlseqs.html#h3-Extended-coordinates)
	 * is enabled (mode 1005).
	 *
	 * Normally, mouse events use three bytes for the flags, x, and y coordinate values. In UTF-8
	 * mode, the number of bytes switches to be variable-length as the values are now UTF-8 encoded,
	 * and setting this property to true correctly changes the parser to consume the values as UTF-8.
	 */
	@Volatile
	public var xtermExtendedUtf8Mouse: Boolean = false

	/**
	 * Save the current terminal settings and enter "raw" mode.
	 *
	 * Raw mode is described as "input is available character by character, echoing is disabled,
	 * and all special processing of terminal input and output characters is disabled."
	 *
	 * The saved settings can be restored by calling [close][AutoCloseable.close] on
	 * the returned instance.
	 *
	 * See [`termios(3)`](https://linux.die.net/man/3/termios) for more information.
	 *
	 * In addition to the flags required for entering "raw" mode, on POSIX-compliant platforms,
	 * this function will change the standard input stream to block indefinitely until a minimum
	 * of 1 byte is available to read. This allows the reader thread to fully be suspended rather
	 * than consuming CPU. Use [terminalReader] to read in a manner that can still be interrupted.
	 */
	public fun enableRawMode() {
		platformInput.enableRawMode()
	}

	/**
	 * Write [ResizeEvent]s into [events] using platform-specific window monitoring.
	 *
	 * Note: Before enabling this, consider querying the terminal for support of
	 * [mode 2048 in-band resize events](https://gist.github.com/rockorager/e695fb2924d36b2bcf1fff4a3704bd83)
	 * which are more reliable. Mode 2048 events are also parsed and sent as [ResizeEvent]s.
	 *
	 * On Windows this enables receiving
	 * [`WINDOW_BUFFER_SIZE_RECORD`](https://learn.microsoft.com/en-us/windows/console/window-buffer-size-record-str)
	 * records from the console. Only the row and column values of the [ResizeEvent] will be present.
	 * The width and height will always be 0.
	 *
	 * On Linux and macOS this installs a `SIGWINCH` signal handler which then queries `TIOCGWINSZ`
	 * using `ioctl`.
	 *
	 * Note: You can also respond to [ResizeEvent]s which lack necessary data by sending `XTWINOPS`
	 * to query row/col counts and/or window or cell size in pixels. More details
	 * [here](https://invisible-island.net/xterm/ctlseqs/ctlseqs.html#h4-Functions-using-CSI-_-ordered-by-the-final-character-lparen-s-rparen:CSI-Ps;Ps;Ps-t:Ps-=-1-4.2064).
	 */
	public fun enableWindowResizeEvents() {
		platformInput.enableWindowResizeEvents()
	}

	/** Synchronously query for the current terminal size. */
	public fun currentSize(): ResizeEvent {
		val (columns, rows, width, height) = platformInput.currentSize()
		return ResizeEvent(
			columns = columns,
			rows = rows,
			width = width,
			height = height,
		)
	}

	/**
	 * Perform a blocking read from stdin to try and parse events. Calls to this function are not
	 * guaranteed to read an event, nor are they guaranteed to read only one event. Events
	 * which are read will be placed into [events].
	 *
	 * It is expected that this function will be called repeatedly in a loop.
	 *
	 * @return False if returning due to [interrupt] being called. True means some data was read,
	 * but not necessarily that any events were put into the [events] channel. This could be because
	 * not enough bytes were available to parse the entire event, for example.
	 */
	public fun runParseLoop() {
		val buffer = buffer

		while (true) {
			if (offset < limit) {
				val event = tryParse(buffer, offset, limit)
				if (event != null) {
					_events.trySend(event)
					if (!emitDebugEvents) continue

					// In debug event mode, parsing starts at index 0 of the buffer (see below). Leverage
					// this to capture the consumed bytes by looking at where the next parse would start.
					val eventBytes = buffer.copyOfRange(0, offset)
					_events.trySend(DebugEvent(event, eventBytes))

					// Move remaining data to the start of the buffer to maintain the parse from 0 invariant.
					buffer.copyInto(buffer, 0, startIndex = offset, endIndex = limit)
					limit -= offset
					offset = 0

					continue
				}
			}

			// Underflow! Copy any data to start of buffer in preparation for a read.
			buffer.copyInto(buffer, 0, startIndex = offset, endIndex = limit)
			limit -= offset
			offset = 0

			if (kittyDisambiguateEscapeCodes || limit != 1 || buffer[0] != 0x1B.toByte()) {
				// Common case: we are using the Kitty keyboard protocol to disambiguate escape keys, or
				// the buffer contains anything other than a bare escape. Do a normal read for more data.
				val read = platformInput.read(buffer, limit, BufferSize - limit)
				if (read == -1) break // EOF
				if (read == 0) return // Interrupt

				limit += read
				continue
			}

			// Otherwise, perform a quick read to see if we have any more bytes. This will allow us to
			// determine whether the bare escape was truly a legacy keyboard escape event, or just the
			// start of some other escape sequence.
			val read = platformInput.readWithTimeout(
				buffer,
				1,
				BufferSize - 1,
				BareEscapeDisambiguationReadTimeoutMillis,
			)
			if (read == -1) break

			limit = if (read == 0) {
				_events.trySend(KeyboardEvent(0x1B))
				// We know the offset is 0, so resetting the limit effectively consumes the byte.
				0
			} else {
				read + 1
			}
		}

		if (limit > 0) {
			_events.trySend(UnknownEvent(buffer.copyOfRange(0, limit)))
		}
		_events.close()
	}

	private fun tryParse(buffer: ByteArray, start: Int, limit: Int): Event? {
		val b1 = buffer[start].toInt() and 0xff
		if (b1 == 0x1B) {
			val b2Index = start + 1
			// If this escape is at the end of the buffer, request another read to ensure we can
			// differentiate between a bare escape and one starting a sequence. Note: The caller is
			// expected to handle the case of a bare escape, as we will otherwise endlessly return null.
			if (b2Index == limit) return null

			when (val b2 = buffer[b2Index].toInt()) {
				0x4F -> return parseSs3(buffer, start, limit)
				0x50 -> return parseDcs(buffer, start, limit)
				0x58 -> return parseSos(buffer, start, limit)
				0x5B -> return parseCsi(buffer, start, limit)
				0x5D -> return parseOsc(buffer, start, limit)
				0x5E -> return parsePm(buffer, start, limit)
				0x5F -> return parseApc(buffer, start, limit)
				else -> {
					offset = start + 2
					return KeyboardEvent(b2, modifiers = KeyboardEvent.ModifierAlt)
				}
			}
		} else {
			return parseGround(buffer, start, limit, b1)
		}
	}

	private fun parseGround(buffer: ByteArray, start: Int, limit: Int, b1: Int): Event? {
		if (b1 <= 0x1a) {
			offset = start + 1
			return when (b1) {
				0x00 -> KeyboardEvent('@'.code, modifiers = KeyboardEvent.ModifierCtrl)
				0x08 -> KeyboardEvent(0x7F)
				0x09 -> KeyboardEvent(0x09)
				0x0A -> KeyboardEvent(0x0D)
				0x0D -> KeyboardEvent(0x0D)
				else -> KeyboardEvent(b1 + 0x60, modifiers = KeyboardEvent.ModifierCtrl)
			}
		}

		// TODO Non-UTF-8 support?
		// TODO multi-codepoint grapheme support
		val codepoint = buffer.parseUtf8(
			start,
			limit,
			onUnderflow = { return null },
			onSuccess = { offset = it },
			onError = {
				val nextStart = start + 1
				offset = nextStart
				return UnknownEvent(buffer.copyOfRange(start, nextStart))
			},
		)
		return KeyboardEvent(codepoint)
	}

	private fun parseApc(buffer: ByteArray, start: Int, limit: Int): Event? {
		// TODO https://stackoverflow.com/a/71632523/132047
		return parseUntilStringTerminator(buffer, start, limit) { b3Index, stIndex ->
			if (stIndex > b3Index && buffer[b3Index].toInt() == 'G'.code) {
				val delimiter = buffer.indexOf(';'.code.toByte(), b3Index, stIndex)
				val b5Index = start + 4
				if (delimiter != -1 &&
					delimiter > b5Index &&
					buffer[start + 3].toInt() == 'i'.code &&
					buffer[b5Index].toInt() == '='.code
				) {
					return@parseUntilStringTerminator KittyGraphicsEvent(
						id = buffer.parseIntDigits(b5Index, delimiter),
						message = buffer.decodeToString(delimiter + 1, stIndex),
					)
				}
			}
			null
		}
	}

	private fun parseCsi(buffer: ByteArray, start: Int, limit: Int): Event? {
		val b3Index = start + 2
		val finalIndex = buffer.indexOfFirstOrElse(
			// Skip leading 0x1B5B.
			start = b3Index,
			end = limit,
			predicate = { it.toInt() in 0x40..0xFF },
			orElse = { return null },
		)

		val end = finalIndex + 1
		offset = end

		error@ do {
			when (buffer[finalIndex].toInt()) {
				'A'.code -> return parseCsiLegacyKeyboard(buffer, start, end, KeyboardEvent.Up)
				'B'.code -> return parseCsiLegacyKeyboard(buffer, start, end, KeyboardEvent.Down)
				'C'.code -> return parseCsiLegacyKeyboard(buffer, start, end, KeyboardEvent.Right)
				'D'.code -> return parseCsiLegacyKeyboard(buffer, start, end, KeyboardEvent.Left)
				'E'.code -> return parseCsiLegacyKeyboard(buffer, start, end, KeyboardEvent.KpBegin)
				'F'.code -> return parseCsiLegacyKeyboard(buffer, start, end, KeyboardEvent.End)
				'H'.code -> return parseCsiLegacyKeyboard(buffer, start, end, KeyboardEvent.Home)
				// TODO Where are these documented? I only see SS3 variants.
				// 'P'.code -> return parseCsiLegacyKeyboard(buffer, start, end, LegacyKeyboardEvent.F1)
				// 'Q'.code -> return parseCsiLegacyKeyboard(buffer, start, end, LegacyKeyboardEvent.F2)
				// 'R'.code -> return parseCsiLegacyKeyboard(buffer, start, end, LegacyKeyboardEvent.F3)
				// 'S'.code -> return parseCsiLegacyKeyboard(buffer, start, end, LegacyKeyboardEvent.F4)

				'~'.code -> {
					val delimiter =
						buffer.indexOfOrDefault(';'.code.toByte(), b3Index, finalIndex, finalIndex)
					val number = buffer.parseIntDigits(b3Index, delimiter, orElse = { break@error })
					val codepoint = when (number) {
						2 -> KeyboardEvent.Insert
						3 -> KeyboardEvent.Delete
						5 -> KeyboardEvent.PageUp
						6 -> KeyboardEvent.PageDown
						7 -> KeyboardEvent.Home
						8 -> KeyboardEvent.End
						11 -> KeyboardEvent.F1
						12 -> KeyboardEvent.F2
						13 -> KeyboardEvent.F3
						14 -> KeyboardEvent.F4
						15 -> KeyboardEvent.F5
						17 -> KeyboardEvent.F6
						18 -> KeyboardEvent.F7
						19 -> KeyboardEvent.F8
						20 -> KeyboardEvent.F9
						21 -> KeyboardEvent.F10
						23 -> KeyboardEvent.F11
						24 -> KeyboardEvent.F12
						200 -> return BracketedPasteEvent(start = true)
						201 -> return BracketedPasteEvent(start = false)
						57427 -> KeyboardEvent.KpBegin
						else -> break@error
					}

					// TODO parse rest of CSI ... ~
					return KeyboardEvent(codepoint)
				}

				'I'.code -> return FocusEvent(focused = true)
				'O'.code -> return FocusEvent(focused = false)

				'm'.code,
				'M'.code,
				-> {
					val cbStart = start + 3
					val cb: Int
					val cx: Int
					val cy: Int

					val release = buffer[finalIndex].toInt() == 'm'.code

					if (b3Index == finalIndex && !release) {
						// CSI M Cb Cx Cy

						if (!xtermExtendedUtf8Mouse) {
							if (end + 3 > limit) return null
							cb = buffer[cbStart].toInt() - 0x20
							cx = buffer[start + 4].toInt() - 0x20
							cy = buffer[start + 5].toInt() - 0x20
							offset = start + 6
						} else {
							val cxStart: Int
							cb = buffer.parseUtf8(
								cbStart,
								limit,
								onUnderflow = { return null },
								onSuccess = { cxStart = it },
								onError = {
									offset = cbStart
									break@error
								},
							) - 0x20
							val cyStart: Int
							cx = buffer.parseUtf8(
								cxStart,
								limit,
								onUnderflow = { return null },
								onSuccess = { cyStart = it },
								onError = {
									offset = cxStart + 1
									break@error
								},
							) - 0x20
							cy = buffer.parseUtf8(
								cyStart,
								limit,
								onUnderflow = { return null },
								onSuccess = { offset = it },
								onError = {
									offset = cyStart + 1
									break@error
								},
							) - 0x20
						}
					} else {
						// CSI < Pb ; Px ; Py {Mm}

						if (buffer[b3Index].toInt() != '<'.code) {
							break@error
						}

						val cbEnd = buffer.indexOfOrElse(';'.code.toByte(), cbStart, finalIndex, orElse = { break@error })
						cb = buffer.parseIntDigits(cbStart, cbEnd, orElse = { break@error })

						val cxStart = cbEnd + 1
						val cxEnd = buffer.indexOfOrElse(';'.code.toByte(), cxStart, finalIndex, orElse = { break@error })
						cx = buffer.parseIntDigits(cxStart, cxEnd, orElse = { break@error })

						val cyStart = cxEnd + 1
						cy = buffer.parseIntDigits(cyStart, finalIndex, orElse = { break@error })
					}

					val button = when (cb and 0b11000011) {
						0 -> MouseEvent.Button.Left
						1 -> MouseEvent.Button.Middle
						2 -> MouseEvent.Button.Right
						3 -> MouseEvent.Button.None
						64 -> MouseEvent.Button.WheelUp
						65 -> MouseEvent.Button.WheelDown
						128 -> MouseEvent.Button.Button8
						129 -> MouseEvent.Button.Button9
						130 -> MouseEvent.Button.Button10
						131 -> MouseEvent.Button.Button11
						else -> break@error
					}
					val motion = (cb and 0b00100000) != 0
					val type = when {
						motion && button != MouseEvent.Button.None -> MouseEvent.Type.Drag
						motion && button == MouseEvent.Button.None -> MouseEvent.Type.Motion
						release -> MouseEvent.Type.Release
						else -> MouseEvent.Type.Press
					}
					val shift = (cb and 0b00000100) != 0
					val alt = (cb and 0b00001000) != 0
					val ctrl = (cb and 0b00010000) != 0

					return MouseEvent(
						// Incoming coordinates are 1-based.
						x = cx - 1,
						y = cy - 1,
						type = type,
						button = button,
						shift = shift,
						alt = alt,
						ctrl = ctrl,
					)
				}

				'c'.code -> {
					if (buffer[b3Index].toInt() == '?'.code) {
						val b4Index = start + 3
						val delimiter = buffer.indexOfOrDefault(';'.code.toByte(), b4Index, finalIndex, finalIndex)
						val id = buffer.parseIntDigits(b4Index, delimiter, orElse = { break@error })
						val data = if (delimiter < finalIndex) {
							buffer.decodeToString(delimiter + 1, finalIndex)
						} else {
							""
						}
						return PrimaryDeviceAttributesEvent(id, data)
					}
				}

				'n'.code -> {
					if (buffer[b3Index].toInt() == '?'.code) {
						val b4Index = start + 3
						val delimiter =
							buffer.indexOfOrDefault(';'.code.toByte(), b4Index, finalIndex, finalIndex)
						val p0 = buffer.parseIntDigits(b4Index, delimiter, orElse = { break@error })
						when (p0) {
							997 -> {
								if (delimiter + 2 == finalIndex) {
									when (buffer[delimiter + 1].toInt()) {
										'1'.code -> return SystemThemeEvent(isDark = true)
										'2'.code -> return SystemThemeEvent(isDark = false)
									}
								}
							}
						}
					} else {
						val p0 = buffer.parseIntDigits(b3Index, finalIndex, orElse = { break@error })
						when (p0) {
							0 -> return OperatingStatusResponseEvent(ok = true)
							3 -> return OperatingStatusResponseEvent(ok = false)
						}
					}
				}

				't'.code -> {
					val modeDelimiter = buffer.indexOfOrElse(';'.code.toByte(), b3Index, finalIndex, orElse = { break@error })
					val mode = buffer.parseIntDigits(b3Index, modeDelimiter, orElse = { break@error })
					when (mode) {
						4 -> {
							// CSI 4 ; height ; width t
							// https://invisible-island.net/xterm/ctlseqs/ctlseqs.html#h4-Functions-using-CSI-_-ordered-by-the-final-character-lparen-s-rparen:CSI-Ps;Ps;Ps-t:Ps-=-1-4.2064

							val heightStart = modeDelimiter + 1
							val heightEnd = buffer.indexOfOrElse(';'.code.toByte(), heightStart, finalIndex, orElse = { break@error })
							val height = buffer.parseIntDigits(heightStart, heightEnd, orElse = { break@error })

							val widthStart = heightEnd + 1
							val width = buffer.parseIntDigits(widthStart, finalIndex, orElse = { break@error })

							return XtermPixelSizeEvent(height, width)
						}
						8 -> {
							// CSI 8 ; height ; width t
							// https://invisible-island.net/xterm/ctlseqs/ctlseqs.html#h4-Functions-using-CSI-_-ordered-by-the-final-character-lparen-s-rparen:CSI-Ps;Ps;Ps-t:Ps-=-1-8.2068

							val rowsStart = modeDelimiter + 1
							val rowsEnd = buffer.indexOfOrElse(';'.code.toByte(), rowsStart, finalIndex, orElse = { break@error })
							val rows = buffer.parseIntDigits(rowsStart, rowsEnd, orElse = { break@error })

							val columnsStart = rowsEnd + 1
							val columns = buffer.parseIntDigits(columnsStart, finalIndex, orElse = { break@error })

							return XtermCharacterSizeEvent(rows, columns)
						}
						48 -> {
							// CSI 48 ; height_chars ; width_chars ; height_pix ; width_pix t
							// https://gist.github.com/rockorager/e695fb2924d36b2bcf1fff4a3704bd83

							val rowsStart = modeDelimiter + 1
							val rowsDelimiter = buffer.indexOfOrElse(';'.code.toByte(), rowsStart, finalIndex, orElse = { break@error })
							val rowsEnd = buffer.indexOfOrDefault(':'.code.toByte(), rowsStart, rowsDelimiter, rowsDelimiter)
							val rows = buffer.parseIntDigits(rowsStart, rowsEnd, orElse = { break@error })

							val columnsStart = rowsDelimiter + 1
							val columnsDelimiter = buffer.indexOfOrElse(';'.code.toByte(), columnsStart, finalIndex, orElse = { break@error })
							val columnsEnd = buffer.indexOfOrDefault(':'.code.toByte(), columnsStart, columnsDelimiter, columnsDelimiter)
							val columns = buffer.parseIntDigits(columnsStart, columnsEnd, orElse = { break@error })

							val heightStart = columnsDelimiter + 1
							val heightDelimiter = buffer.indexOfOrElse(';'.code.toByte(), heightStart, finalIndex, orElse = { break@error })
							val heightEnd = buffer.indexOfOrDefault(':'.code.toByte(), heightStart, heightDelimiter, heightDelimiter)
							val height = buffer.parseIntDigits(heightStart, heightEnd, orElse = { break@error })

							val widthStart = heightDelimiter + 1
							val widthEnd = buffer.indexOfOrDefault(':'.code.toByte(), widthStart, finalIndex, finalIndex)
							val width = buffer.parseIntDigits(widthStart, widthEnd, orElse = { break@error })

							return ResizeEvent(columns, rows, width, height)
						}
					}
				}

				'u'.code -> {
					// CSI unicode-key-code:alternate-key-codes ; modifiers:event-type ; text-as-codepoints u
					//  https://sw.kovidgoyal.net/kitty/keyboard-protocol/#an-overview
					// CSI ? flags u
					//  https://sw.kovidgoyal.net/kitty/keyboard-protocol/#progressive-enhancement

					if (buffer[b3Index].toInt() == '?'.code) {
						val b4Index = start + 3
						if (b4Index != finalIndex) {
							val flags = buffer.parseIntDigits(b4Index, finalIndex, orElse = { break@error })
							return KittyKeyboardQueryEvent(flags)
						}
					} else {
						val codepointDelimiter = buffer.indexOfOrDefault(';'.code.toByte(), b3Index, finalIndex, finalIndex)
						val codepointEnd = buffer.indexOfOrDefault(':'.code.toByte(), b3Index, codepointDelimiter, codepointDelimiter)
						val codepoint = buffer.parseIntDigits(b3Index, codepointEnd, orElse = { break@error })

						var shiftedCodepoint = -1
						var baseLayoutCodepoint = -1
						var modifiers = 0
						var eventType = KeyboardEvent.EventTypePress
						var text: String? = null

						if (codepointEnd != codepointDelimiter) {
							val shiftedCodepointStart = codepointEnd + 1
							val shiftedCodepointEnd = buffer.indexOfOrDefault(':'.code.toByte(), shiftedCodepointStart, codepointDelimiter, codepointDelimiter)
							if (shiftedCodepointEnd != shiftedCodepointStart) {
								shiftedCodepoint = buffer.parseIntDigits(shiftedCodepointStart, shiftedCodepointEnd, orElse = { break@error })
							}
							if (shiftedCodepointEnd != codepointDelimiter) {
								val baseLayoutCodepointStart = shiftedCodepointEnd + 1
								baseLayoutCodepoint = buffer.parseIntDigits(baseLayoutCodepointStart, codepointDelimiter, orElse = { break@error })
							}
						}

						if (codepointDelimiter != finalIndex) {
							val modifiersStart = codepointDelimiter + 1
							val modifiersDelimiter = buffer.indexOfOrDefault(';'.code.toByte(), modifiersStart, finalIndex, finalIndex)
							val modifiersEnd = buffer.indexOfOrDefault(':'.code.toByte(), modifiersStart, modifiersDelimiter, modifiersDelimiter)
							if (modifiersEnd != modifiersStart) {
								modifiers = buffer.parseIntDigits(modifiersStart, modifiersEnd, orElse = { break@error }) - 1

								if (modifiersEnd != modifiersDelimiter) {
									val eventTypeStart = modifiersEnd + 1
									eventType = buffer.parseIntDigits(eventTypeStart, modifiersDelimiter, orElse = { break@error })
								}
							}

							if (modifiersDelimiter != finalIndex) {
								val textCodepoints = StringBuilder()
								var textCodepointStart = modifiersDelimiter + 1
								while (true) {
									val textCodepointEnd = buffer.indexOfOrDefault(':'.code.toByte(), textCodepointStart, finalIndex, finalIndex)
									val textCodepoint = buffer.parseIntDigits(textCodepointStart, textCodepointEnd, orElse = { break@error })
									textCodepoints.appendCodepoint(textCodepoint)
									if (textCodepointEnd == finalIndex) {
										break
									}
									textCodepointStart = textCodepointEnd + 1
								}

								text = textCodepoints.toString()
							}
						}

						return KeyboardEvent(
							codepoint,
							shiftedCodepoint,
							baseLayoutCodepoint,
							modifiers,
							eventType,
							text,
						)
					}
				}

				'y'.code -> {
					// CSI ? Ps ; Pm $ y
					val dollarIndex = finalIndex - 1
					if (buffer[dollarIndex].toInt() == '$'.code) {
						if (buffer[b3Index].toInt() == '?'.code) {
							if (end - start < 8) break@error

							val b4Index = start + 3
							val semi = buffer.indexOfOrElse(';'.code.toByte(), b4Index, dollarIndex, orElse = { break@error })
							val mode = buffer.parseIntDigits(b4Index, semi, orElse = { break@error })
							val settingValue = buffer.parseIntDigits(semi + 1, dollarIndex, orElse = { break@error })

							val setting = when (settingValue) {
								0 -> DecModeReportEvent.Setting.NotRecognized
								1 -> DecModeReportEvent.Setting.Set
								2 -> DecModeReportEvent.Setting.Reset
								3 -> DecModeReportEvent.Setting.PermanentlySet
								4 -> DecModeReportEvent.Setting.PermanentlyReset
								else -> break@error
							}
							return DecModeReportEvent(mode, setting)
						} else {
							// TODO ANSI mode reporter
						}
					}
				}
			}
		} while (false)

		// Use 'offset' not 'end' because some sequences put data after the "final" index. This allows
		// parsing of that trailing data to error and still be included in the unknown event.
		return UnknownEvent(buffer.copyOfRange(start, offset))
	}

	private fun parseCsiLegacyKeyboard(buffer: ByteArray, start: Int, end: Int, codepoint: Int): Event {
		// CSI {ABCDEFHPQS}
		// CSI 1 ; modifier:event-type {ABCDEFHPQS}
		//  https://sw.kovidgoyal.net/kitty/keyboard-protocol/#legacy-key-event-encoding

		val finalIndex = end - 1
		val b3Index = start + 2
		if (b3Index == finalIndex) {
			return KeyboardEvent(codepoint)
		}

		// This is just an 'if' that can also use 'break' to jump out of its own logic.
		error@ while (end - start >= 6 &&
			buffer[b3Index] == '1'.code.toByte() &&
			buffer[start + 3] == ';'.code.toByte()
		) {
			val b5Index = start + 4
			val modifiersDelimiter = buffer.indexOfOrDefault(';'.code.toByte(), b5Index, finalIndex, finalIndex)
			val modifiersEnd = buffer.indexOfOrDefault(':'.code.toByte(), b5Index, modifiersDelimiter, modifiersDelimiter)
			val modifiers = buffer.parseIntDigits(b5Index, modifiersEnd, orElse = { break@error }) - 1
			val eventType = buffer.parseIntDigits(modifiersEnd + 1, modifiersDelimiter, orElse = { 1 })

			return KeyboardEvent(codepoint, modifiers = modifiers, eventType = eventType)
		}

		return UnknownEvent(buffer.copyOfRange(start, end))
	}

	private fun parseDcs(buffer: ByteArray, start: Int, limit: Int): Event? {
		return parseUntilStringTerminator(buffer, start, limit) { b3Index, stIndex ->
			val b4Index = start + 3
			val b5Index = start + 4
			if (stIndex > b4Index &&
				buffer[b3Index].toInt() == '>'.code &&
				buffer[b4Index].toInt() == '|'.code
			) {
				TerminalVersionEvent(buffer.decodeToString(b5Index, stIndex))
			} else if (stIndex == start + 12 &&
				buffer[b3Index].toInt() == '!'.code &&
				buffer[b4Index].toInt() == '|'.code
			) {
				val b7Index = start + 6
				val manufacturingSite = buffer.parseHexDigits(b5Index, b7Index) { return@parseUntilStringTerminator null }
				val terminalId = buffer.parseHexDigits(b7Index, stIndex) { return@parseUntilStringTerminator null }
				TertiaryDeviceAttributesEvent(manufacturingSite, terminalId)
			} else if (stIndex > b5Index &&
				buffer[b4Index].toInt() == '+'.code &&
				buffer[b5Index].toInt() == 'r'.code
			) {
				val b6Index = start + 5
				val success = when (buffer[b3Index].toInt()) {
					'1'.code -> {
						if (stIndex == b6Index) {
							// Success case requires the Pt parameter.
							return@parseUntilStringTerminator null
						}
						true
					}
					'0'.code -> false
					else -> return@parseUntilStringTerminator null
				}
				val data = buildMap {
					var entryStart = b6Index
					while (entryStart < stIndex) {
						val entryEnd = buffer.indexOfOrDefault(';'.code.toByte(), entryStart, stIndex, stIndex)
						val keyEnd = buffer.indexOfOrDefault('='.code.toByte(), entryStart, entryEnd, entryEnd)
						val key = buffer.parseHexString(entryStart, keyEnd) { return@parseUntilStringTerminator null }
						val value = if (keyEnd < entryEnd) {
							if (success) {
								buffer.parseHexString(keyEnd + 1, entryEnd) { return@parseUntilStringTerminator null }
							} else {
								return@parseUntilStringTerminator null
							}
						} else {
							null
						}
						put(key, value)
						entryStart = entryEnd + 1
					}
				}
				CapabilityQueryEvent(success, data)
			} else {
				null
			}
		}
	}

	private fun parseOsc(buffer: ByteArray, start: Int, limit: Int): Event? {
		return parseUntilStringTerminator(buffer, start, limit, allowBell = true) { b3Index, stIndex ->
			error@ do {
				// OSC Ps ; Pt ST
				if (stIndex - b3Index > 2) {
					val psDelimiter =
						buffer.indexOfOrElse(';'.code.toByte(), b3Index, stIndex, orElse = { break@error })
					val ptIndex = psDelimiter + 1
					val ps = buffer.parseIntDigits(b3Index, psDelimiter, orElse = { break@error })
					when (ps) {
						4 -> {
							val cDelimiter = buffer.indexOfOrElse(';'.code.toByte(), ptIndex, stIndex, orElse = { break@error })
							val c = buffer.parseIntDigits(ptIndex, cDelimiter, orElse = { break@error })
							// TODO Actually decode color spec.
							return@parseUntilStringTerminator PaletteColorEvent(
								color = c,
								value = buffer.decodeToString(cDelimiter + 1, stIndex),
							)
						}
						10 -> {
							// TODO Actually decode color spec.
							return@parseUntilStringTerminator TerminalColorEvent(
								color = TerminalColorEvent.Color.Foreground,
								value = buffer.decodeToString(ptIndex, stIndex),
							)
						}
						11 -> {
							// TODO Actually decode color spec.
							return@parseUntilStringTerminator TerminalColorEvent(
								color = TerminalColorEvent.Color.Background,
								value = buffer.decodeToString(ptIndex, stIndex),
							)
						}
						12 -> {
							// TODO Actually decode color spec.
							return@parseUntilStringTerminator TerminalColorEvent(
								color = TerminalColorEvent.Color.Cursor,
								value = buffer.decodeToString(ptIndex, stIndex),
							)
						}
						22 -> {
							name@ do {
								var i = ptIndex
								var values = BooleanArray(10)
								var valuesIndex = 0
								while (i < stIndex) {
									val valuesSize = values.size
									if (valuesIndex == valuesSize) {
										values = values.copyOf(valuesSize * 2)
									}
									val b = buffer[i++].toInt()
									values[valuesIndex++] = when (b) {
										'0'.code -> false
										'1'.code -> true
										else -> break@name
									}
									if (i < stIndex) {
										if (buffer[i++].toInt() == ','.code) {
											continue
										}
										break@name
									}
									return@parseUntilStringTerminator KittyPointerQuerySupportEvent(
										values.copyOf(valuesIndex),
									)
								}
								break@error
							} while (false)

							val name = StringBuilder(stIndex - ptIndex)
							for (i in ptIndex until stIndex) {
								val b = buffer[i].toInt()
								if (b !in '0'.code..'9'.code && b !in 'a'.code..'z'.code && b != '-'.code && b != '_'.code) {
									break@error
								}
								name.append(b.toChar())
							}
							return@parseUntilStringTerminator KittyPointerQueryNameEvent(name.toString())
						}
						99 -> {
							// TODO Actually decode notification spec.
							return@parseUntilStringTerminator KittyNotificationEvent(
								raw = buffer.decodeToString(b3Index, stIndex),
							)
						}
					}
				}
			} while (false)
			null
		}
	}

	private fun parsePm(buffer: ByteArray, start: Int, limit: Int): Event? {
		return parseUntilStringTerminator(buffer, start, limit) { _, _ ->
			null
		}
	}

	private fun parseSos(buffer: ByteArray, start: Int, limit: Int): Event? {
		return parseUntilStringTerminator(buffer, start, limit) { _, _ ->
			null
		}
	}

	private fun parseSs3(buffer: ByteArray, start: Int, limit: Int): Event? {
		// SS3 {ABCDEFHPQRS}
		// https://sw.kovidgoyal.net/kitty/keyboard-protocol/#legacy-functional-keys

		val end = start + 3
		if (end > limit) return null

		offset = end

		val b3Index = start + 2
		error@ do {
			val codepoint = when (buffer[b3Index].toInt()) {
				'A'.code -> KeyboardEvent.Up
				'B'.code -> KeyboardEvent.Down
				'C'.code -> KeyboardEvent.Right
				'D'.code -> KeyboardEvent.Left
				'F'.code -> KeyboardEvent.End
				'H'.code -> KeyboardEvent.Home
				'P'.code -> KeyboardEvent.F1
				'Q'.code -> KeyboardEvent.F2
				'R'.code -> KeyboardEvent.F3
				'S'.code -> KeyboardEvent.F4
				0x1b -> {
					// libvaxis added a guard against this case
					// https://github.com/rockorager/libvaxis/commit/b68864c3babf2767c15c52911179e8ee9158e1d2
					offset = b3Index
					break@error
				}

				else -> break@error
			}
			return KeyboardEvent(codepoint)
		} while (false)

		// Use 'offset' not 'end' because if end is an escape we back up the offset.
		return UnknownEvent(buffer.copyOfRange(start, offset))
	}

	private inline fun parseUntilStringTerminator(
		buffer: ByteArray,
		start: Int,
		limit: Int,
		allowBell: Boolean = false,
		crossinline handler: (b3Index: Int, stIndex: Int) -> Event?,
	): Event? {
		// TODO test string with 0x1b inside of it

		// Skip leading discriminator.
		val b3Index = start + 2

		var stIndex: Int
		val end: Int
		found@ do {
			var searchFrom = b3Index
			while (true) {
				stIndex = buffer.indexOfOrElse(0x1B.toByte(), searchFrom, limit, orElse = { break })

				// If found at end of range, underflow.
				// TODO What if we are not in raw mode and this is a bare escape after a BEL?
				val slashIndex = stIndex + 1
				if (slashIndex == limit) return null

				if (buffer[slashIndex] == '\\'.code.toByte()) {
					end = stIndex + 2
					break@found
				}
				searchFrom = slashIndex
			}

			// Common case: no terminator in buffer and BEL not allowed. Underflow!
			if (!allowBell) return null

			// Rare case: fallback to searching for BEL.
			stIndex = buffer.indexOfOrElse(7.toByte(), b3Index, limit, orElse = { return null })
			end = stIndex + 1
		} while (false)

		offset = end
		return handler(b3Index, stIndex)
			?: UnknownEvent(buffer.copyOfRange(start, end))
	}

	public fun interrupt() {
		platformInput.interrupt()
	}

	/**
	 * Free the resources associated with this reader.
	 *
	 * This call can be omitted if your process is exiting.
	 */
	override fun close() {
		platformInput.close()
	}
}
