package com.jakewharton.mosaic.layout

import androidx.compose.runtime.Stable
import com.jakewharton.mosaic.modifier.Modifier
import dev.drewhamilton.poko.Poko

/**
 * Applies a tag to allow modified element to be found in tests.
 */
@Stable
internal fun Modifier.testTag(tag: String): Modifier = this then TestTagModifier(tag)

@Poko
internal class TestTagModifier(internal val tag: String) : Modifier.Element {

	override fun toString(): String = "TestTag($tag)"
}
