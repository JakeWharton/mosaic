package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.CapabilityQueryEvent
import com.jakewharton.mosaic.terminal.UnknownEvent
import kotlin.test.Test

class EventParserDcsCapabilityQueryEventTest : BaseEventParserTest() {
	@Test fun unknownStatus() {
		testTty.writeHex("1b50322b721b5c")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b50322b721b5c".hexToByteArray()),
		)
	}

	@Test fun failureEmpty() {
		testTty.writeHex("1b50302b721b5c")
		assertThat(parser.next()).isEqualTo(
			CapabilityQueryEvent(
				success = false,
				data = emptyMap(),
			),
		)
	}

	@Test fun failureOneEntryNoValue() {
		testTty.writeHex("1b50302b72353337351b5c")
		assertThat(parser.next()).isEqualTo(
			CapabilityQueryEvent(
				success = false,
				data = mapOf("Su" to null),
			),
		)
	}

	@Test fun failureOneEntryNoValueWithEquals() {
		testTty.writeHex("1b50302b72353337353d1b5c")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b50302b72353337353d1b5c".hexToByteArray()),
		)
	}

	@Test fun failureOneEntryWithValue() {
		testTty.writeHex("1b50302b72353337353d35373635374135343635373236441b5c")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b50302b72353337353d35373635374135343635373236441b5c".hexToByteArray()),
		)
	}

	@Test fun successRequiresData() {
		testTty.writeHex("1b50312b721b5c")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b50312b721b5c".hexToByteArray()),
		)
	}

	@Test fun successOneEntryNoValue() {
		testTty.writeHex("1b50312b72353337351b5c")
		assertThat(parser.next()).isEqualTo(
			CapabilityQueryEvent(
				success = true,
				data = mapOf("Su" to null),
			),
		)
	}

	@Test fun successOneEntryNoValueWithEquals() {
		testTty.writeHex("1b50312b72353337353d1b5c")
		assertThat(parser.next()).isEqualTo(
			CapabilityQueryEvent(
				success = true,
				data = mapOf("Su" to ""),
			),
		)
	}

	@Test fun successOneEntryWithValue() {
		testTty.writeHex("1b50312b72353337353d35373635374135343635373236441b5c")
		assertThat(parser.next()).isEqualTo(
			CapabilityQueryEvent(
				success = true,
				data = mapOf("Su" to "WezTerm"),
			),
		)
	}

	@Test fun successMultipleEntries() {
		testTty.writeHex("1b50312b72353337353d35373635374135343635373236443b3638363537393b3733373537303d1b5c")
		assertThat(parser.next()).isEqualTo(
			CapabilityQueryEvent(
				success = true,
				data = mapOf("Su" to "WezTerm", "hey" to null, "sup" to ""),
			),
		)
	}

	@Test fun entryKeyOddNumberOfHex() {
		testTty.writeHex("1b50312b723533371b5c")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b50312b723533371b5c".hexToByteArray()),
		)
	}

	@Test fun entryValueOddNumberOfHex() {
		testTty.writeHex("1b50312b72353337353d353736353741353436353732361b5c")
		assertThat(parser.next()).isEqualTo(
			UnknownEvent("1b50312b72353337353d353736353741353436353732361b5c".hexToByteArray()),
		)
	}
}
