package com.jakewharton.mosaic

import kotlin.jvm.JvmInline

internal object UnicodeBlocks {

	// region Latin

	val LatinBasic = Codepoint("U+0000")..Codepoint("U+007F")
	val Latin1Supplement = Codepoint("U+00A0")..Codepoint("U+00FF")
	val LatinExtendedA = Codepoint("U+0100")..Codepoint("U+017F")
	val LatinExtendedB = Codepoint("U+0180")..Codepoint("U+024F")
	val LatinExtendedC = Codepoint("U+2C60")..Codepoint("U+2C7F")
	val LatinExtendedD = Codepoint("U+A720")..Codepoint("U+A7FF")
	val LatinExtendedE = Codepoint("U+AB30")..Codepoint("U+AB6F")
	val LatinExtendedF = Codepoint("U+10780")..Codepoint("U+107BF")
	val LatinExtendedG = Codepoint("U+1DF00")..Codepoint("U+1DFFF")
	val LatinExtendedAdditional = Codepoint("U+1E00")..Codepoint("U+1EFF")

	val LatinIpaExtensions = Codepoint("U+0250")..Codepoint("U+02AF")
	val PhoneticExtensions = Codepoint("U+1D00")..Codepoint("U+1D7F")
	val PhoneticExtensionsSupplement = Codepoint("U+1D80")..Codepoint("U+1DBF")

	val SpacingModifierLetters = Codepoint("U+02B0")..Codepoint("U+02FF")
	val CombiningMarks = Codepoint("U+0300")..Codepoint("U+036F")

	val latin = listOf(
		LatinBasic,
		Latin1Supplement,
		LatinExtendedA, LatinExtendedB, LatinExtendedC,
		LatinExtendedD, LatinExtendedE, LatinExtendedF,
		LatinExtendedG,
		LatinExtendedAdditional,
		LatinIpaExtensions, PhoneticExtensions, PhoneticExtensionsSupplement,
		SpacingModifierLetters, CombiningMarks
	)
	// endregion

	// TODO: Greek, Cyrillic, ...
}

@JvmInline
internal value class Codepoint constructor(val dec: Int) {

	constructor(unicode: String) : this(unicode.removePrefix(PREFIX).toInt(16))

	val hex: String get() = dec.toString(16).uppercase()
	val unicode: String get() = PREFIX + hex

	companion object {
		const val PREFIX = "U+"
	}

	fun toChar(): Char = dec.toChar()

	override fun toString(): String = buildString {
		if (dec <= 0xFFFF) {
			// simple BMP code point:
			append(toChar())
		} else {
			// surrogate pair calculation:
			// https://en.wikipedia.org/wiki/Plane_(Unicode)#Surrogate_pairs
			val high = ((dec - 0x10000) shr 10) + 0xD800
			val low = ((dec - 0x10000) and 0x3FF) + 0xDC00
			append(high.toChar())
			append(low.toChar())
		}
	}

	operator fun rangeTo(other: Codepoint) = UnicodeBlock(start = this, endInclusive = other)
}

internal data class UnicodeBlock(val start: Codepoint, val endInclusive: Codepoint) {

	operator fun contains(codepoint: Codepoint): Boolean {
		return codepoint.dec in this.toIntRange()
	}

	fun toIntRange() = IntRange(start.dec, endInclusive.dec)
}

internal fun IntRange.toUnicodeBlock(): UnicodeBlock {
	return UnicodeBlock(
		start = Codepoint(start), endInclusive = Codepoint(endInclusive)
	)
}

internal fun Int.toCodepoint() = Codepoint(this)
internal fun String.toCodepoint() = Codepoint(this)
