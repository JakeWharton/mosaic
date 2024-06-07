package com.jakewharton.mosaic.text

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.jakewharton.mosaic.text.AnnotatedString.Builder
import dev.drewhamilton.poko.Poko

/**
 * The basic data structure of text with multiple styles. To construct an [AnnotatedString] you
 * can use [Builder].
 */
@Immutable
public class AnnotatedString internal constructor(
	public val text: String,
	internal val spanStylesOrNull: List<Range<SpanStyle>>? = null,
) : CharSequence {

	/**
	 * All [SpanStyle] that have been applied to a range of this String
	 */
	public val spanStyles: List<Range<SpanStyle>>
		get() = spanStylesOrNull ?: emptyList()

	override val length: Int
		get() = text.length

	override operator fun get(index: Int): Char = text[index]

	/**
	 * Return a substring for the AnnotatedString and include the styles in the range of [startIndex]
	 * (inclusive) and [endIndex] (exclusive).
	 *
	 * @param startIndex the inclusive start offset of the range
	 * @param endIndex the exclusive end offset of the range
	 */
	override fun subSequence(startIndex: Int, endIndex: Int): AnnotatedString {
		require(startIndex <= endIndex) {
			"start ($startIndex) should be less or equal to end ($endIndex)"
		}
		if (startIndex == 0 && endIndex == text.length) return this
		val text = text.substring(startIndex, endIndex)
		return AnnotatedString(
			text = text,
			spanStylesOrNull = filterRanges(spanStylesOrNull, startIndex, endIndex),
		)
	}

	@Stable
	public operator fun plus(other: AnnotatedString): AnnotatedString {
		return with(Builder(this)) {
			append(other)
			toAnnotatedString()
		}
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is AnnotatedString) return false
		if (text != other.text) return false
		if (spanStylesOrNull != other.spanStylesOrNull) return false
		return true
	}

	override fun hashCode(): Int {
		var result = text.hashCode()
		result = 31 * result + (spanStylesOrNull?.hashCode() ?: 0)
		return result
	}

	override fun toString(): String {
		// AnnotatedString.toString has special value, it converts it into regular String
		// rather than debug string.
		return text
	}

	/**
	 * The information attached on the text such as a [SpanStyle].
	 *
	 * @param item The object attached to [AnnotatedString]s.
	 * @param start The start of the range where [item] takes effect. It's inclusive
	 * @param end The end of the range where [item] takes effect. It's exclusive
	 */
	@[Immutable Poko]
	public class Range<T>(public val item: T, public val start: Int, public val end: Int) {

		init {
			require(start <= end) { "Reversed range is not supported" }
		}
	}

	/**
	 * Builder class for AnnotatedString. Enables construction of an [AnnotatedString] using
	 * methods such as [append] and [addStyle].
	 *
	 * This class implements [Appendable] and can be used with other APIs that don't know about
	 * [AnnotatedString]s:
	 *
	 * @param capacity initial capacity for the internal char buffer
	 */
	public class Builder(capacity: Int = 16) : Appendable {

		private data class MutableRange<T>(
			val item: T,
			val start: Int,
			var end: Int = Int.MIN_VALUE,
		) {
			/**
			 * Create an immutable [Range] object.
			 *
			 * @param defaultEnd if the end is not set yet, it will be set to this value.
			 */
			fun toRange(defaultEnd: Int = Int.MIN_VALUE): Range<T> {
				val end = if (end == Int.MIN_VALUE) defaultEnd else end
				check(end != Int.MIN_VALUE) { "Item.end should be set first" }
				return Range(item = item, start = start, end = end)
			}
		}

		private val text: StringBuilder = StringBuilder(capacity)
		private val spanStyles: MutableList<MutableRange<SpanStyle>> = mutableListOf()
		private val styleStack: MutableList<MutableRange<out Any>> = mutableListOf()

		/**
		 * Create an [Builder] instance using the given [String].
		 */
		public constructor(text: String) : this() {
			append(text)
		}

		/**
		 * Create an [Builder] instance using the given [AnnotatedString].
		 */
		public constructor(text: AnnotatedString) : this() {
			append(text)
		}

		/**
		 * Returns the length of the [String].
		 */
		public val length: Int get() = text.length

		/**
		 * Appends the given [String] to this [Builder].
		 *
		 * @param text the text to append
		 */
		public fun append(text: String) {
			this.text.append(text)
		}

		/**
		 * Appends [text] to this [Builder] if non-null, and returns this [Builder].
		 *
		 * If [text] is an [AnnotatedString], all spans and annotations will be copied over as well.
		 * No other subtypes of [CharSequence] will be treated specially.
		 */
		@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
		override fun append(text: CharSequence?): Builder {
			if (text is AnnotatedString) {
				append(text)
			} else {
				this.text.append(text)
			}
			return this
		}

		/**
		 * Appends the range of [text] between [start] (inclusive) and [end] (exclusive) to this
		 * [Builder] if non-null, and returns this [Builder].
		 *
		 * If [text] is an [AnnotatedString], all spans and annotations from [text] between
		 * [start] and [end] will be copied over as well.
		 * No other subtypes of [CharSequence] will be treated specially.
		 *
		 * @param start The index of the first character in [text] to copy over (inclusive).
		 * @param end The index after the last character in [text] to copy over (exclusive).
		 */
		@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
		override fun append(text: CharSequence?, start: Int, end: Int): Builder {
			if (text is AnnotatedString) {
				append(text, start, end)
			} else {
				this.text.append(text, start, end)
			}
			return this
		}

		// Kdoc comes from interface method.
		@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
		override fun append(char: Char): Builder {
			this.text.append(char)
			return this
		}

		/**
		 * Appends the given [AnnotatedString] to this [Builder].
		 *
		 * @param text the text to append
		 */
		public fun append(text: AnnotatedString) {
			val start = this.text.length
			this.text.append(text.text)
			// offset every style with start and add to the builder
			text.spanStylesOrNull?.forEach {
				addStyle(it.item, start + it.start, start + it.end)
			}
		}

		/**
		 * Appends the range of [text] between [start] (inclusive) and [end] (exclusive) to this
		 * [Builder]. All spans and annotations from [text] between [start] and [end] will be copied
		 * over as well.
		 *
		 * @param start The index of the first character in [text] to copy over (inclusive).
		 * @param end The index after the last character in [text] to copy over (exclusive).
		 */
		public fun append(text: AnnotatedString, start: Int, end: Int) {
			val insertionStart = this.text.length
			this.text.append(text.text, start, end)
			// offset every style with insertionStart and add to the builder
			text.getLocalSpanStyles(start, end)?.forEach {
				addStyle(it.item, insertionStart + it.start, insertionStart + it.end)
			}
		}

		/**
		 * Set a [SpanStyle] for the given range between [start] (inclusive)
		 * and [end] (exclusive) to this [Builder].
		 *
		 * @param style [SpanStyle] to be applied
		 * @param start the inclusive starting offset of the range
		 * @param end the exclusive end offset of the range
		 */
		public fun addStyle(style: SpanStyle, start: Int, end: Int) {
			spanStyles.add(MutableRange(item = style, start = start, end = end))
		}

		/**
		 * Applies the given [SpanStyle] to any appended text until a corresponding [pop] is
		 * called.
		 *
		 * @param style SpanStyle to be applied
		 */
		public fun pushStyle(style: SpanStyle): Int {
			MutableRange(item = style, start = text.length).also {
				styleStack.add(it)
				spanStyles.add(it)
			}
			return styleStack.size - 1
		}

		/**
		 * Ends the style or annotation that was added via a push operation before.
		 *
		 * @see pushStyle
		 */
		public fun pop() {
			check(styleStack.isNotEmpty()) { "Nothing to pop." }
			// pop the last element
			val item = styleStack.removeAt(styleStack.size - 1)
			item.end = text.length
		}

		/**
		 * Ends the styles or annotation up to and `including` the [pushStyle]
		 * that returned the given index.
		 *
		 * @param index the result of the a previous [pushStyle] in order to pop to
		 *
		 * @see pop
		 * @see pushStyle
		 */
		public fun pop(index: Int) {
			check(index < styleStack.size) { "$index should be less than ${styleStack.size}" }
			while ((styleStack.size - 1) >= index) {
				pop()
			}
		}

		/**
		 * Constructs an [AnnotatedString] based on the configurations applied to the [Builder].
		 */
		public fun toAnnotatedString(): AnnotatedString {
			return AnnotatedString(
				text = text.toString(),
				spanStylesOrNull = spanStyles
					.map { it.toRange(text.length) }
					.ifEmpty { null },
			)
		}
	}
}

/**
 * Helper function used to find the [SpanStyle]s in the given range and also convert the
 * range of those [SpanStyle]s to local range.
 *
 * @param start The start index of the range, inclusive
 * @param end The end index of the range, exclusive
 * @return The list of converted [SpanStyle]s in the given range
 */
private fun AnnotatedString.getLocalSpanStyles(
	start: Int,
	end: Int,
): List<AnnotatedString.Range<SpanStyle>>? {
	if (start == end) return null
	val spanStyles = spanStylesOrNull ?: return null
	// If the given range covers the whole AnnotatedString, return SpanStyles without conversion.
	if (start == 0 && end >= this.text.length) {
		return spanStyles
	}
	return spanStyles.filter { intersect(start, end, it.start, it.end) }
		.map {
			AnnotatedString.Range(
				it.item,
				it.start.coerceIn(start, end) - start,
				it.end.coerceIn(start, end) - start,
			)
		}
}

/**
 * Filter the range list based on [com.jakewharton.mosaic.text.AnnotatedString.Range.start]
 * and [com.jakewharton.mosaic.text.AnnotatedString.Range.end] to include ranges only in the range
 * of [start] (inclusive) and [end] (exclusive).
 *
 * @param start the inclusive start offset of the text range
 * @param end the exclusive end offset of the text range
 */
private fun <T> filterRanges(
	ranges: List<AnnotatedString.Range<out T>>?,
	start: Int,
	end: Int,
): List<AnnotatedString.Range<T>>? {
	require(start <= end) { "start ($start) should be less than or equal to end ($end)" }
	val nonNullRange = ranges ?: return null

	return nonNullRange.filter { intersect(start, end, it.start, it.end) }.map {
		AnnotatedString.Range(
			item = it.item,
			start = maxOf(start, it.start) - start,
			end = minOf(end, it.end) - start,
		)
	}.ifEmpty { null }
}

/**
 * Helper function used to find the [SpanStyle]s in the given range.
 *
 * @param start The start index of the range, inclusive
 * @param end The end index of the range, exclusive
 * @return The list of [SpanStyle]s in the given range
 */
internal fun AnnotatedString.getLocalRawSpanStyles(
	start: Int,
	end: Int,
): List<SpanStyle> {
	require(start <= end) { "start ($start) should be less than or equal to end ($end)" }
	val nonNullRange = spanStylesOrNull ?: return emptyList()

	return nonNullRange.filter { intersect(start, end, it.start, it.end) }.map { it.item }
}

/**
 * Splits this [AnnotatedString] to a list of [AnnotatedString]s around occurrences
 * of the specified [delimiter].
 * This is specialized version of split which receives single non-empty delimiter
 * and offers better performance.
 *
 * @param delimiter String used as delimiter
 * @param ignoreCase `true` to ignore character case when matching a delimiter. By default `false`.
 * @param limit The maximum number of substrings to return.
 */
internal fun AnnotatedString.split(
	delimiter: String,
	ignoreCase: Boolean = false,
	limit: Int = 0,
): List<AnnotatedString> {
	require(limit >= 0) { "Limit must be non-negative, but was $limit" }

	var currentOffset = 0
	var nextIndex = indexOf(delimiter, currentOffset, ignoreCase)
	if (nextIndex == -1 || limit == 1) {
		return listOf(this)
	}

	val isLimited = limit > 0
	val result = ArrayList<AnnotatedString>(if (isLimited) limit.coerceAtMost(10) else 10)
	do {
		result.add(subSequence(currentOffset, nextIndex))
		currentOffset = nextIndex + delimiter.length
		// Do not search for next occurrence if we're reaching limit
		if (isLimited && result.size == limit - 1) break
		nextIndex = indexOf(delimiter, currentOffset, ignoreCase)
	} while (nextIndex != -1)

	result.add(subSequence(currentOffset, length))
	return result
}

/**
 * Pushes [style] to the [AnnotatedString.Builder], executes [block] and then pops the [style].
 *
 * @param style [SpanStyle] to be applied
 * @param block function to be executed
 *
 * @return result of the [block]
 *
 * @see AnnotatedString.Builder.pushStyle
 * @see AnnotatedString.Builder.pop
 */
public inline fun <R : Any> Builder.withStyle(
	style: SpanStyle,
	block: Builder.() -> R,
): R {
	val index = pushStyle(style)
	return try {
		block(this)
	} finally {
		pop(index)
	}
}

/**
 * Create an AnnotatedString with a [spanStyle] that will apply to the whole text.
 *
 * @param spanStyle [SpanStyle] to be applied to whole text
 */
public fun AnnotatedString(
	text: String,
	spanStyle: SpanStyle,
): AnnotatedString = AnnotatedString(
	text,
	listOf(AnnotatedString.Range(spanStyle, 0, text.length)),
)

/**
 * Build a new AnnotatedString by populating newly created [AnnotatedString.Builder] provided
 * by [builder].
 *
 * @param builder lambda to modify [AnnotatedString.Builder]
 */
public inline fun buildAnnotatedString(builder: (Builder).() -> Unit): AnnotatedString =
	Builder().apply(builder).toAnnotatedString()

/**
 * Helper function that checks if the range [baseStart, baseEnd) contains the range
 * [targetStart, targetEnd).
 *
 * @return true if [baseStart, baseEnd) contains [targetStart, targetEnd), vice versa.
 * When [baseStart]==[baseEnd] it return true iff [targetStart]==[targetEnd]==[baseStart].
 */
private fun contains(baseStart: Int, baseEnd: Int, targetStart: Int, targetEnd: Int) =
	(baseStart <= targetStart && targetEnd <= baseEnd) &&
		(baseEnd != targetEnd || (targetStart == targetEnd) == (baseStart == baseEnd))

/**
 * Helper function that checks if the range [lStart, lEnd) intersects with the range
 * [rStart, rEnd).
 *
 * @return [lStart, lEnd) intersects with range [rStart, rEnd), vice versa.
 */
private fun intersect(lStart: Int, lEnd: Int, rStart: Int, rEnd: Int) =
	maxOf(lStart, rStart) < minOf(lEnd, rEnd) ||
		contains(lStart, lEnd, rStart, rEnd) ||
		contains(rStart, rEnd, lStart, lEnd)

private val EmptyAnnotatedString: AnnotatedString = AnnotatedString("")

/**
 * Returns an AnnotatedString with empty text and no annotations.
 */
internal fun emptyAnnotatedString() = EmptyAnnotatedString
