package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.event.MouseEvent
import com.jakewharton.mosaic.terminal.event.MouseEvent.Button
import com.jakewharton.mosaic.terminal.event.MouseEvent.Type
import com.jakewharton.mosaic.terminal.event.UnknownEvent
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class TerminalParserCsiMouseEventTest : BaseTerminalParserTest() {
	@Test fun motion() = runTest {
		testTty.writeHex("1b5b4d434837")
		assertThat(reader.next()).isEqualTo(
			MouseEvent(39, 22, Type.Motion, Button.None),
		)
	}

	@Test fun click() = runTest {
		testTty.writeHex("1b5b4d204837")
		assertThat(reader.next()).isEqualTo(
			MouseEvent(39, 22, Type.Press, Button.Left),
		)
	}

	@Test fun drag() = runTest {
		testTty.writeHex("1b5b4d404837")
		assertThat(reader.next()).isEqualTo(
			MouseEvent(39, 22, Type.Drag, Button.Left),
		)
	}

	@Test fun clickMouseUp() = runTest {
		testTty.writeHex("1b5b4d234837")
		assertThat(reader.next()).isEqualTo(
			MouseEvent(39, 22, Type.Press, Button.None),
		)
	}

	@Test fun shiftClick() = runTest {
		testTty.writeHex("1b5b4d244837")
		assertThat(reader.next()).isEqualTo(
			MouseEvent(39, 22, Type.Press, Button.Left, shift = true),
		)
	}

	@Test fun altClick() = runTest {
		testTty.writeHex("1b5b4d284837")
		assertThat(reader.next()).isEqualTo(
			MouseEvent(39, 22, Type.Press, Button.Left, alt = true),
		)
	}

	@Test fun ctrlClick() = runTest {
		testTty.writeHex("1b5b4d304837")
		assertThat(reader.next()).isEqualTo(
			MouseEvent(39, 22, Type.Press, Button.Left, ctrl = true),
		)
	}

	@Test fun clickRight() = runTest {
		testTty.writeHex("1b5b4d224837")
		assertThat(reader.next()).isEqualTo(
			MouseEvent(39, 22, Type.Press, Button.Right),
		)
	}

	@Test fun clickMiddle() = runTest {
		testTty.writeHex("1b5b4d214837")
		assertThat(reader.next()).isEqualTo(
			MouseEvent(39, 22, Type.Press, Button.Middle),
		)
	}

	@Test fun clickWheelUp() = runTest {
		testTty.writeHex("1b5b4d604837")
		assertThat(reader.next()).isEqualTo(
			MouseEvent(39, 22, Type.Press, Button.WheelUp),
		)
	}

	@Test fun clickWheelDown() = runTest {
		testTty.writeHex("1b5b4d614837")
		assertThat(reader.next()).isEqualTo(
			MouseEvent(39, 22, Type.Press, Button.WheelDown),
		)
	}

	@Test fun clickButton8() = runTest {
		testTty.writeHex("1b5b4da04837")
		assertThat(reader.next()).isEqualTo(
			MouseEvent(39, 22, Type.Press, Button.Button8),
		)
	}

	@Test fun clickButton9() = runTest {
		testTty.writeHex("1b5b4da14837")
		assertThat(reader.next()).isEqualTo(
			MouseEvent(39, 22, Type.Press, Button.Button9),
		)
	}

	@Test fun clickButton10() = runTest {
		testTty.writeHex("1b5b4da24837")
		assertThat(reader.next()).isEqualTo(
			MouseEvent(39, 22, Type.Press, Button.Button10),
		)
	}

	@Test fun clickButton11() = runTest {
		testTty.writeHex("1b5b4da34837")
		assertThat(reader.next()).isEqualTo(
			MouseEvent(39, 22, Type.Press, Button.Button11),
		)
	}

	@Test fun clickUtf8() = runTest {
		reader.xtermExtendedUtf8Mouse = true

		testTty.writeHex("1b5b4d20c28037")
		assertThat(reader.next()).isEqualTo(
			MouseEvent(95, 22, Type.Press, Button.Left),
		)
	}

	// TODO all types & buttons utf-8 in both single-byte and multi-byte form

	@Test fun lowercaseMDelimiterInvalid() = runTest {
		testTty.writeHex("1b5b6d204837")
		assertThat(reader.next()).isEqualTo(
			UnknownEvent("1b5b6d".hexToByteArray()),
		)
	}
}
