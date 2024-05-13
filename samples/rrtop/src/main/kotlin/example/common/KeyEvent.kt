package example.common

fun KeyEvent(key: Key, utf16CodePoint: Int = 0): KeyEvent {
	return KeyEvent(key.keyCode.toLong().shl(32) or (utf16CodePoint.toLong() and 0xFFFFFFFF))
}

@JvmInline
value class KeyEvent internal constructor(@PublishedApi internal val packed: Long)

/**
 * The key that was pressed.
 */
@Suppress("NOTHING_TO_INLINE")
inline val KeyEvent.key: Key
	get() = Key(packed.shr(32).toInt())

/**
 * The UTF16 value corresponding to the key event that was pressed. The unicode character
 * takes into account any meta keys that are pressed (eg. Pressing shift results in capital
 * alphabets). The UTF16 value uses the
 * [U+n notation][http://www.unicode.org/reports/tr27/#notation] of the Unicode Standard.
 *
 * An [Int] is used instead of a [Char] so that we can support supplementary characters. The
 * Unicode Standard allows for characters whose representation requires more than 16 bits.
 * The range of legal code points is U+0000 to U+10FFFF, known as Unicode scalar value.
 *
 * The set of characters from U+0000 to U+FFFF is sometimes referred to as the Basic
 * Multilingual Plane (BMP). Characters whose code points are greater than U+FFFF are called
 * supplementary characters. In this representation, supplementary characters are represented
 * as a pair of char values, the first from the high-surrogates range, (\uD800-\uDBFF), the
 * second from the low-surrogates range (\uDC00-\uDFFF).
 */
@Suppress("NOTHING_TO_INLINE")
inline val KeyEvent.utf16CodePoint: Int
	get() = packed.and(0xFFFFFFFF).toInt()
