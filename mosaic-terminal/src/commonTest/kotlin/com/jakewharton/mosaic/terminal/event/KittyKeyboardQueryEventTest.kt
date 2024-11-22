package com.jakewharton.mosaic.terminal.event

import assertk.Assert
import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import kotlin.test.Test

class KittyKeyboardQueryEventTest {
	@Test fun none() {
		assertThat(KittyKeyboardQueryEvent(0)).hasFlags(
			disambiguateEscapeCodes = false,
			reportEventTypes = false,
			reportAlternateKeys = false,
			reportAllKeysAsEscapeCodes = false,
			reportAssociatedText = false,
		)
	}

	@Test fun disambiguateEscapeCodes() {
		assertThat(KittyKeyboardQueryEvent(0b1)).hasFlags(
			disambiguateEscapeCodes = true,
			reportEventTypes = false,
			reportAlternateKeys = false,
			reportAllKeysAsEscapeCodes = false,
			reportAssociatedText = false,
		)
	}

	@Test fun reportEventTypes() {
		assertThat(KittyKeyboardQueryEvent(0b10)).hasFlags(
			disambiguateEscapeCodes = false,
			reportEventTypes = true,
			reportAlternateKeys = false,
			reportAllKeysAsEscapeCodes = false,
			reportAssociatedText = false,
		)
	}

	@Test fun reportAlternateKeys() {
		assertThat(KittyKeyboardQueryEvent(0b100)).hasFlags(
			disambiguateEscapeCodes = false,
			reportEventTypes = false,
			reportAlternateKeys = true,
			reportAllKeysAsEscapeCodes = false,
			reportAssociatedText = false,
		)
	}

	@Test fun reportAllKeysAsEscapeCodes() {
		assertThat(KittyKeyboardQueryEvent(0b1000)).hasFlags(
			disambiguateEscapeCodes = false,
			reportEventTypes = false,
			reportAlternateKeys = false,
			reportAllKeysAsEscapeCodes = true,
			reportAssociatedText = false,
		)
	}

	@Test fun reportAssociatedText() {
		assertThat(KittyKeyboardQueryEvent(0b10000)).hasFlags(
			disambiguateEscapeCodes = false,
			reportEventTypes = false,
			reportAlternateKeys = false,
			reportAllKeysAsEscapeCodes = false,
			reportAssociatedText = true,
		)
	}

	@Test fun unknownFlags() {
		assertThat(KittyKeyboardQueryEvent(0b11111.inv())).hasFlags(
			disambiguateEscapeCodes = false,
			reportEventTypes = false,
			reportAlternateKeys = false,
			reportAllKeysAsEscapeCodes = false,
			reportAssociatedText = false,
		)
	}

	private fun Assert<KittyKeyboardQueryEvent>.hasFlags(
		disambiguateEscapeCodes: Boolean,
		reportEventTypes: Boolean,
		reportAlternateKeys: Boolean,
		reportAllKeysAsEscapeCodes: Boolean,
		reportAssociatedText: Boolean,
	) = all {
		prop(KittyKeyboardQueryEvent::disambiguateEscapeCodes).isEqualTo(disambiguateEscapeCodes)
		prop(KittyKeyboardQueryEvent::reportEventTypes).isEqualTo(reportEventTypes)
		prop(KittyKeyboardQueryEvent::reportAlternateKeys).isEqualTo(reportAlternateKeys)
		prop(KittyKeyboardQueryEvent::reportAllKeysAsEscapeCodes).isEqualTo(reportAllKeysAsEscapeCodes)
		prop(KittyKeyboardQueryEvent::reportAssociatedText).isEqualTo(reportAssociatedText)
	}
}
