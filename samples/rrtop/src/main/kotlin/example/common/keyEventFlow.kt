package example.common

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import org.jline.terminal.TerminalBuilder

private val terminalReader by lazy {
	val terminal = TerminalBuilder.terminal()
	terminal.enterRawMode()
	terminal.reader()
}

val keyEventFlow: Flow<KeyEvent> = flow {
	while (currentCoroutineContext().isActive) {
		val firstInput = terminalReader.read()
		if (firstInput == '\u001B'.code) {
			val secondInput = terminalReader.read()
			if (secondInput == '['.code || secondInput == 'O'.code) {
				val thirdInput = terminalReader.read()
				when (val thirdInputChar = thirdInput.toChar()) {
					'A' -> emit(KeyEvent(Key.DirectionUp))
					'B' -> emit(KeyEvent(Key.DirectionDown))
					'C' -> emit(KeyEvent(Key.DirectionRight))
					'D' -> emit(KeyEvent(Key.DirectionLeft))
					'F' -> emit(KeyEvent(Key.MoveHome))
					'H' -> emit(KeyEvent(Key.MoveEnd))
					'P' -> emit(KeyEvent(Key.F1))
					'Q' -> emit(KeyEvent(Key.F2))
					'R' -> emit(KeyEvent(Key.F3))
					'S' -> emit(KeyEvent(Key.F4))
					else -> {
						if (thirdInputChar in '1'..'6') {
							val fourthInput = terminalReader.read()
							val fourthInputChar = fourthInput.toChar()
							if (fourthInputChar == '~') {
								when (thirdInputChar) {
									'1' -> emit(KeyEvent(Key.MoveHome))
									'2' -> emit(KeyEvent(Key.Insert))
									'3' -> emit(KeyEvent(Key.Delete))
									'4' -> emit(KeyEvent(Key.MoveEnd))
									'5' -> emit(KeyEvent(Key.PageUp))
									'6' -> emit(KeyEvent(Key.PageDown))
									else -> emit(KeyEvent(Key.Unknown))
								}
							} else {
								when (thirdInputChar) {
									'1' -> when (fourthInputChar) {
										'5' -> {
											terminalReader.read()
											emit(KeyEvent(Key.F5))
										}
										'7' -> {
											terminalReader.read()
											emit(KeyEvent(Key.F6))
										}
										'8' -> {
											terminalReader.read()
											emit(KeyEvent(Key.F7))
										}
										'9' -> {
											terminalReader.read()
											emit(KeyEvent(Key.F8))
										}
										else -> emit(KeyEvent(Key.Unknown))
									}
									'2' -> when (fourthInputChar) {
										'0' -> {
											terminalReader.read()
											emit(KeyEvent(Key.F9))
										}
										'1' -> {
											terminalReader.read()
											emit(KeyEvent(Key.F10))
										}
										'3' -> {
											terminalReader.read()
											emit(KeyEvent(Key.F11))
										}
										'4' -> {
											terminalReader.read()
											emit(KeyEvent(Key.F12))
										}
										else -> emit(KeyEvent(Key.Unknown))
									}
									else -> emit(KeyEvent(Key.Unknown))
								}
							}
						} else {
							emit(KeyEvent(Key.Unknown))
						}
					}
				}
			}
		} else {
			val firstInputChar = firstInput.toChar()
			if (firstInputChar.isISOControl()) {
				when (firstInputChar) {
					'\u0008', '\u007F' -> emit(KeyEvent(Key.Backspace))
					'\u0009' -> emit(KeyEvent(Key.Tab))
					'\u000D' -> emit(KeyEvent(Key.Enter))
					else -> emit(KeyEvent(Key.Unknown, firstInput))
				}
			} else if (firstInputChar.isLetterOrDigit() || firstInputChar.isWhitespace()) {
				emit(KeyEvent(Key.Type, firstInputChar.code))
			} else if (firstInputChar.isHighSurrogate()) {
				val secondInput = terminalReader.read()
				val secondInputChar = secondInput.toChar()
				if (secondInputChar.isLowSurrogate()) {
					emit(KeyEvent(Key.Type, "$firstInputChar$secondInputChar".codePointAt(0)))
				} else {
					emit(KeyEvent(Key.Unknown))
				}
			} else if (firstInputChar.isDefined()) {
				emit(KeyEvent(Key.Type, firstInput))
			} else {
				emit(KeyEvent(Key.Unknown, firstInput))
			}
		}
	}
}
