package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.CapabilityQueryEvent
import com.jakewharton.mosaic.terminal.UnknownEvent
import kotlin.test.Test

class EventParserDcsCapabilityQueryEventTest : BaseEventParserTest() {
	@Test fun unknownStatus() {
		testTty.write("${DCS}2+r$ST")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b50322b721b5c".hexToByteArray()),
		)
	}

	@Test fun failureEmpty() {
		testTty.write("${DCS}0+r$ST")
		assertThat(parser.next()).isEqualTo(
			CapabilityQueryEvent(
				success = false,
				data = emptyMap(),
			),
		)
	}

	@Test fun failureOneEntryNoValue() {
		testTty.write("${DCS}0+r5375$ST")
		assertThat(parser.next()).isEqualTo(
			CapabilityQueryEvent(
				success = false,
				data = mapOf("Su" to null),
			),
		)
	}

	@Test fun failureOneEntryNoValueWithEquals() {
		testTty.write("${DCS}0+r5375=$ST")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b50302b72353337353d1b5c".hexToByteArray()),
		)
	}

	@Test fun failureOneEntryWithValue() {
		testTty.write("${DCS}0+r5375=57657A5465726D$ST")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b50302b72353337353d35373635374135343635373236441b5c".hexToByteArray()),
		)
	}

	@Test fun successRequiresData() {
		testTty.write("${DCS}1+r$ST")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b50312b721b5c".hexToByteArray()),
		)
	}

	@Test fun successOneEntryNoValue() {
		testTty.write("${DCS}1+r5375$ST")
		assertThat(parser.next()).isEqualTo(
			CapabilityQueryEvent(
				success = true,
				data = mapOf("Su" to null),
			),
		)
	}

	@Test fun successOneEntryNoValueWithEquals() {
		testTty.write("${DCS}1+r5375=$ST")
		assertThat(parser.next()).isEqualTo(
			CapabilityQueryEvent(
				success = true,
				data = mapOf("Su" to ""),
			),
		)
	}

	@Test fun successOneEntryWithValue() {
		testTty.write("${DCS}1+r5375=57657A5465726D$ST")
		assertThat(parser.next()).isEqualTo(
			CapabilityQueryEvent(
				success = true,
				data = mapOf("Su" to "WezTerm"),
			),
		)
	}

	@Test fun successMultipleEntries() {
		testTty.write("${DCS}1+r5375=57657A5465726D;686579;737570=$ST")
		assertThat(parser.next()).isEqualTo(
			CapabilityQueryEvent(
				success = true,
				data = mapOf("Su" to "WezTerm", "hey" to null, "sup" to ""),
			),
		)
	}

	@Test fun entryKeyOddNumberOfHex() {
		testTty.write("${DCS}1+r537$ST")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b50312b723533371b5c".hexToByteArray()),
		)
	}

	@Test fun entryValueOddNumberOfHex() {
		testTty.write("${DCS}1+r5375=57657A5465726$ST")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b50312b72353337353d353736353741353436353732361b5c".hexToByteArray()),
		)
	}
}
