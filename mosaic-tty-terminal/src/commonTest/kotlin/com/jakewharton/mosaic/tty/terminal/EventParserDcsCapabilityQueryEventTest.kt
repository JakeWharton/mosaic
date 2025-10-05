package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.CapabilityQueryEvent
import com.jakewharton.mosaic.terminal.UnknownEvent
import kotlin.test.Test

class EventParserDcsCapabilityQueryEventTest : BaseEventParserTest() {
	@Test fun unknownStatus() {
		testTerminal.write("${DCS}2+r$ST")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b50322b721b5c".hexToByteArray()),
		)
	}

	@Test fun failureEmpty() {
		testTerminal.write("${DCS}0+r$ST")
		assertThat(parser.next()).isEqualTo(
			CapabilityQueryEvent(
				success = false,
				data = emptyMap(),
			),
		)
	}

	@Test fun failureOneEntryNoValue() {
		testTerminal.write("${DCS}0+r5375$ST")
		assertThat(parser.next()).isEqualTo(
			CapabilityQueryEvent(
				success = false,
				data = mapOf("Su" to null),
			),
		)
	}

	@Test fun failureOneEntryNoValueWithEquals() {
		testTerminal.write("${DCS}0+r5375=$ST")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b50302b72353337353d1b5c".hexToByteArray()),
		)
	}

	@Test fun failureOneEntryWithValue() {
		testTerminal.write("${DCS}0+r5375=57657A5465726D$ST")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b50302b72353337353d35373635374135343635373236441b5c".hexToByteArray()),
		)
	}

	@Test fun successRequiresData() {
		testTerminal.write("${DCS}1+r$ST")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b50312b721b5c".hexToByteArray()),
		)
	}

	@Test fun successOneEntryNoValue() {
		testTerminal.write("${DCS}1+r5375$ST")
		assertThat(parser.next()).isEqualTo(
			CapabilityQueryEvent(
				success = true,
				data = mapOf("Su" to null),
			),
		)
	}

	@Test fun successOneEntryNoValueWithEquals() {
		testTerminal.write("${DCS}1+r5375=$ST")
		assertThat(parser.next()).isEqualTo(
			CapabilityQueryEvent(
				success = true,
				data = mapOf("Su" to ""),
			),
		)
	}

	@Test fun successOneEntryWithValue() {
		testTerminal.write("${DCS}1+r5375=57657A5465726D$ST")
		assertThat(parser.next()).isEqualTo(
			CapabilityQueryEvent(
				success = true,
				data = mapOf("Su" to "WezTerm"),
			),
		)
	}

	@Test fun successMultipleEntries() {
		testTerminal.write("${DCS}1+r5375=57657A5465726D;686579;737570=$ST")
		assertThat(parser.next()).isEqualTo(
			CapabilityQueryEvent(
				success = true,
				data = mapOf("Su" to "WezTerm", "hey" to null, "sup" to ""),
			),
		)
	}

	@Test fun entryKeyOddNumberOfHex() {
		testTerminal.write("${DCS}1+r537$ST")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b50312b723533371b5c".hexToByteArray()),
		)
	}

	@Test fun entryValueOddNumberOfHex() {
		testTerminal.write("${DCS}1+r5375=57657A5465726$ST")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b50312b72353337353d353736353741353436353732361b5c".hexToByteArray()),
		)
	}
}
