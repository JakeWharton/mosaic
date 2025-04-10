package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.MouseEvent
import com.jakewharton.mosaic.terminal.MouseEvent.Button
import com.jakewharton.mosaic.terminal.MouseEvent.Type
import kotlin.test.Test

class EventParserCsiMouseEventTest : BaseEventParserTest() {
	@Test fun motion() {
		testTty.write("${CSI}MCH7")
		assertThat(parser.next()).isEqualTo(
			MouseEvent(39, 22, Type.Motion, Button.None),
		)
	}

	@Test fun click() {
		testTty.write("${CSI}M H7")
		assertThat(parser.next()).isEqualTo(
			MouseEvent(39, 22, Type.Press, Button.Left),
		)
	}

	@Test fun drag() {
		testTty.write("${CSI}M@H7")
		assertThat(parser.next()).isEqualTo(
			MouseEvent(39, 22, Type.Drag, Button.Left),
		)
	}

	@Test fun clickMouseUp() {
		testTty.write("${CSI}M#H7")
		assertThat(parser.next()).isEqualTo(
			MouseEvent(39, 22, Type.Press, Button.None),
		)
	}

	@Test fun shiftClick() {
		testTty.write("${CSI}M\$H7")
		assertThat(parser.next()).isEqualTo(
			MouseEvent(39, 22, Type.Press, Button.Left, shift = true),
		)
	}

	@Test fun altClick() {
		testTty.write("${CSI}M(H7")
		assertThat(parser.next()).isEqualTo(
			MouseEvent(39, 22, Type.Press, Button.Left, alt = true),
		)
	}

	@Test fun ctrlClick() {
		testTty.write("${CSI}M0H7")
		assertThat(parser.next()).isEqualTo(
			MouseEvent(39, 22, Type.Press, Button.Left, ctrl = true),
		)
	}

	@Test fun clickRight() {
		testTty.write("${CSI}M\"H7")
		assertThat(parser.next()).isEqualTo(
			MouseEvent(39, 22, Type.Press, Button.Right),
		)
	}

	@Test fun clickMiddle() {
		testTty.write("${CSI}M!H7")
		assertThat(parser.next()).isEqualTo(
			MouseEvent(39, 22, Type.Press, Button.Middle),
		)
	}

	@Test fun clickWheelUp() {
		testTty.write("${CSI}M`H7")
		assertThat(parser.next()).isEqualTo(
			MouseEvent(39, 22, Type.Press, Button.WheelUp),
		)
	}

	@Test fun clickWheelDown() {
		testTty.write("${CSI}MaH7")
		assertThat(parser.next()).isEqualTo(
			MouseEvent(39, 22, Type.Press, Button.WheelDown),
		)
	}

	@Test fun clickButton8() {
		testTty.writeHex("1b5b4da04837")
		assertThat(parser.next()).isEqualTo(
			MouseEvent(39, 22, Type.Press, Button.Button8),
		)
	}

	@Test fun clickButton9() {
		testTty.writeHex("1b5b4da14837")
		assertThat(parser.next()).isEqualTo(
			MouseEvent(39, 22, Type.Press, Button.Button9),
		)
	}

	@Test fun clickButton10() {
		testTty.writeHex("1b5b4da24837")
		assertThat(parser.next()).isEqualTo(
			MouseEvent(39, 22, Type.Press, Button.Button10),
		)
	}

	@Test fun clickButton11() {
		testTty.writeHex("1b5b4da34837")
		assertThat(parser.next()).isEqualTo(
			MouseEvent(39, 22, Type.Press, Button.Button11),
		)
	}

	@Test fun clickUtf8() {
		parser.xtermExtendedUtf8Mouse = true

		testTty.writeHex("1b5b4d20c28037")
		assertThat(parser.next()).isEqualTo(
			MouseEvent(95, 22, Type.Press, Button.Left),
		)
	}

	// TODO all types & buttons utf-8 in both single-byte and multi-byte form
}
