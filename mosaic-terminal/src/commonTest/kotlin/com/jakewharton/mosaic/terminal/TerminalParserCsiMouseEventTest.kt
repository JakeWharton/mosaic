package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.event.MouseEvent
import com.jakewharton.mosaic.terminal.event.MouseEvent.Button
import com.jakewharton.mosaic.terminal.event.MouseEvent.Type
import com.jakewharton.mosaic.terminal.event.UnknownEvent
import kotlin.test.AfterTest
import kotlin.test.Test

@OptIn(ExperimentalStdlibApi::class)
class TerminalParserCsiMouseEventTest {
	private val writer = Tty.stdinWriter()
	private val parser = TerminalParser(writer.reader)

	@AfterTest fun after() {
		writer.close()
	}

	@Test fun motion() {
		writer.writeHex("1b5b4d434837")
		assertThat(parser.next()).isEqualTo(
			MouseEvent(39, 22, Type.Motion, Button.None),
		)
	}

	@Test fun click() {
		writer.writeHex("1b5b4d204837")
		assertThat(parser.next()).isEqualTo(
			MouseEvent(39, 22, Type.Press, Button.Left),
		)
	}

	@Test fun drag() {
		writer.writeHex("1b5b4d404837")
		assertThat(parser.next()).isEqualTo(
			MouseEvent(39, 22, Type.Drag, Button.Left),
		)
	}

	@Test fun clickMouseUp() {
		writer.writeHex("1b5b4d234837")
		assertThat(parser.next()).isEqualTo(
			MouseEvent(39, 22, Type.Press, Button.None),
		)
	}

	@Test fun shiftClick() {
		writer.writeHex("1b5b4d244837")
		assertThat(parser.next()).isEqualTo(
			MouseEvent(39, 22, Type.Press, Button.Left, shift = true),
		)
	}

	@Test fun altClick() {
		writer.writeHex("1b5b4d284837")
		assertThat(parser.next()).isEqualTo(
			MouseEvent(39, 22, Type.Press, Button.Left, alt = true),
		)
	}

	@Test fun ctrlClick() {
		writer.writeHex("1b5b4d304837")
		assertThat(parser.next()).isEqualTo(
			MouseEvent(39, 22, Type.Press, Button.Left, ctrl = true),
		)
	}

	@Test fun clickRight() {
		writer.writeHex("1b5b4d224837")
		assertThat(parser.next()).isEqualTo(
			MouseEvent(39, 22, Type.Press, Button.Right),
		)
	}

	@Test fun clickMiddle() {
		writer.writeHex("1b5b4d214837")
		assertThat(parser.next()).isEqualTo(
			MouseEvent(39, 22, Type.Press, Button.Middle),
		)
	}

	@Test fun clickWheelUp() {
		writer.writeHex("1b5b4d604837")
		assertThat(parser.next()).isEqualTo(
			MouseEvent(39, 22, Type.Press, Button.WheelUp),
		)
	}

	@Test fun clickWheelDown() {
		writer.writeHex("1b5b4d614837")
		assertThat(parser.next()).isEqualTo(
			MouseEvent(39, 22, Type.Press, Button.WheelDown),
		)
	}

	@Test fun clickButton8() {
		writer.writeHex("1b5b4da04837")
		assertThat(parser.next()).isEqualTo(
			MouseEvent(39, 22, Type.Press, Button.Button8),
		)
	}

	@Test fun clickButton9() {
		writer.writeHex("1b5b4da14837")
		assertThat(parser.next()).isEqualTo(
			MouseEvent(39, 22, Type.Press, Button.Button9),
		)
	}

	@Test fun clickButton10() {
		writer.writeHex("1b5b4da24837")
		assertThat(parser.next()).isEqualTo(
			MouseEvent(39, 22, Type.Press, Button.Button10),
		)
	}

	@Test fun clickButton11() {
		writer.writeHex("1b5b4da34837")
		assertThat(parser.next()).isEqualTo(
			MouseEvent(39, 22, Type.Press, Button.Button11),
		)
	}

	@Test fun clickUtf8() {
		parser.xtermExtendedUtf8Mouse = true

		writer.writeHex("1b5b4d20c28037")
		assertThat(parser.next()).isEqualTo(
			MouseEvent(95, 22, Type.Press, Button.Left),
		)
	}

	// TODO all types & buttons utf-8 in both single-byte and multi-byte form

	@Test fun lowercaseMDelimiterInvalid() {
		writer.writeHex("1b5b6d204837")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b5b6d".hexToByteArray()),
		)
	}
}
