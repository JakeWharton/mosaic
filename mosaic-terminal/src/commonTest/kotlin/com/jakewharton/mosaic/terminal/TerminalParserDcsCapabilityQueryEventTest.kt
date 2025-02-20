package com.jakewharton.mosaic.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.jakewharton.mosaic.terminal.event.CapabilityQueryEvent
import com.jakewharton.mosaic.terminal.event.UnknownEvent
import kotlin.test.Test
import kotlinx.coroutines.test.runTest

class TerminalParserDcsCapabilityQueryEventTest : BaseTerminalParserTest() {
	@Test fun unknownStatus() = runTest {
		writer.writeHex("1b50322b721b5c")
		assertThat(reader.next()).isEqualTo(
			UnknownEvent("1b50322b721b5c".hexToByteArray()),
		)
	}

	@Test fun failureEmpty() = runTest {
		writer.writeHex("1b50302b721b5c")
		assertThat(reader.next()).isEqualTo(
			CapabilityQueryEvent(
				success = false,
				data = emptyMap(),
			),
		)
	}

	@Test fun failureOneEntryNoValue() = runTest {
		writer.writeHex("1b50302b72353337351b5c")
		assertThat(reader.next()).isEqualTo(
			CapabilityQueryEvent(
				success = false,
				data = mapOf("Su" to null),
			),
		)
	}

	@Test fun failureOneEntryNoValueWithEquals() = runTest {
		writer.writeHex("1b50302b72353337353d1b5c")
		assertThat(reader.next()).isEqualTo(
			UnknownEvent("1b50302b72353337353d1b5c".hexToByteArray()),
		)
	}

	@Test fun failureOneEntryWithValue() = runTest {
		writer.writeHex("1b50302b72353337353d35373635374135343635373236441b5c")
		assertThat(reader.next()).isEqualTo(
			UnknownEvent("1b50302b72353337353d35373635374135343635373236441b5c".hexToByteArray()),
		)
	}

	@Test fun successRequiresData() = runTest {
		writer.writeHex("1b50312b721b5c")
		assertThat(reader.next()).isEqualTo(
			UnknownEvent("1b50312b721b5c".hexToByteArray()),
		)
	}

	@Test fun successOneEntryNoValue() = runTest {
		writer.writeHex("1b50312b72353337351b5c")
		assertThat(reader.next()).isEqualTo(
			CapabilityQueryEvent(
				success = true,
				data = mapOf("Su" to null),
			),
		)
	}

	@Test fun successOneEntryNoValueWithEquals() = runTest {
		writer.writeHex("1b50312b72353337353d1b5c")
		assertThat(reader.next()).isEqualTo(
			CapabilityQueryEvent(
				success = true,
				data = mapOf("Su" to ""),
			),
		)
	}

	@Test fun successOneEntryWithValue() = runTest {
		writer.writeHex("1b50312b72353337353d35373635374135343635373236441b5c")
		assertThat(reader.next()).isEqualTo(
			CapabilityQueryEvent(
				success = true,
				data = mapOf("Su" to "WezTerm"),
			),
		)
	}

	@Test fun successMultipleEntries() = runTest {
		writer.writeHex("1b50312b72353337353d35373635374135343635373236443b3638363537393b3733373537303d1b5c")
		assertThat(reader.next()).isEqualTo(
			CapabilityQueryEvent(
				success = true,
				data = mapOf("Su" to "WezTerm", "hey" to null, "sup" to ""),
			),
		)
	}

	@Test fun entryKeyOddNumberOfHex() = runTest {
		writer.writeHex("1b50312b723533371b5c")
		assertThat(reader.next()).isEqualTo(
			UnknownEvent("1b50312b723533371b5c".hexToByteArray()),
		)
	}

	@Test fun entryValueOddNumberOfHex() = runTest {
		writer.writeHex("1b50312b72353337353d353736353741353436353732361b5c")
		assertThat(reader.next()).isEqualTo(
			UnknownEvent("1b50312b72353337353d353736353741353436353732361b5c".hexToByteArray()),
		)
	}
}
