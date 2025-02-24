package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.event.CapabilityQueryEvent
import com.jakewharton.mosaic.terminal.event.UnknownEvent
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class TerminalParserDcsCapabilityQueryEventTest : BaseTerminalParserTest() {
	@Test fun unknownStatus() = runTest {
		testTty.writeHex("1b50322b721b5c")
		assertThat(reader.next()).isEqualTo(
			UnknownEvent("1b50322b721b5c".hexToByteArray()),
		)
	}

	@Test fun failureEmpty() = runTest {
		testTty.writeHex("1b50302b721b5c")
		assertThat(reader.next()).isEqualTo(
			CapabilityQueryEvent(
				success = false,
				data = emptyMap(),
			),
		)
	}

	@Test fun failureOneEntryNoValue() = runTest {
		testTty.writeHex("1b50302b72353337351b5c")
		assertThat(reader.next()).isEqualTo(
			CapabilityQueryEvent(
				success = false,
				data = mapOf("Su" to null),
			),
		)
	}

	@Test fun failureOneEntryNoValueWithEquals() = runTest {
		testTty.writeHex("1b50302b72353337353d1b5c")
		assertThat(reader.next()).isEqualTo(
			UnknownEvent("1b50302b72353337353d1b5c".hexToByteArray()),
		)
	}

	@Test fun failureOneEntryWithValue() = runTest {
		testTty.writeHex("1b50302b72353337353d35373635374135343635373236441b5c")
		assertThat(reader.next()).isEqualTo(
			UnknownEvent("1b50302b72353337353d35373635374135343635373236441b5c".hexToByteArray()),
		)
	}

	@Test fun successRequiresData() = runTest {
		testTty.writeHex("1b50312b721b5c")
		assertThat(reader.next()).isEqualTo(
			UnknownEvent("1b50312b721b5c".hexToByteArray()),
		)
	}

	@Test fun successOneEntryNoValue() = runTest {
		testTty.writeHex("1b50312b72353337351b5c")
		assertThat(reader.next()).isEqualTo(
			CapabilityQueryEvent(
				success = true,
				data = mapOf("Su" to null),
			),
		)
	}

	@Test fun successOneEntryNoValueWithEquals() = runTest {
		testTty.writeHex("1b50312b72353337353d1b5c")
		assertThat(reader.next()).isEqualTo(
			CapabilityQueryEvent(
				success = true,
				data = mapOf("Su" to ""),
			),
		)
	}

	@Test fun successOneEntryWithValue() = runTest {
		testTty.writeHex("1b50312b72353337353d35373635374135343635373236441b5c")
		assertThat(reader.next()).isEqualTo(
			CapabilityQueryEvent(
				success = true,
				data = mapOf("Su" to "WezTerm"),
			),
		)
	}

	@Test fun successMultipleEntries() = runTest {
		testTty.writeHex("1b50312b72353337353d35373635374135343635373236443b3638363537393b3733373537303d1b5c")
		assertThat(reader.next()).isEqualTo(
			CapabilityQueryEvent(
				success = true,
				data = mapOf("Su" to "WezTerm", "hey" to null, "sup" to ""),
			),
		)
	}

	@Test fun entryKeyOddNumberOfHex() = runTest {
		testTty.writeHex("1b50312b723533371b5c")
		assertThat(reader.next()).isEqualTo(
			UnknownEvent("1b50312b723533371b5c".hexToByteArray()),
		)
	}

	@Test fun entryValueOddNumberOfHex() = runTest {
		testTty.writeHex("1b50312b72353337353d353736353741353436353732361b5c")
		assertThat(reader.next()).isEqualTo(
			UnknownEvent("1b50312b72353337353d353736353741353436353732361b5c".hexToByteArray()),
		)
	}
}
