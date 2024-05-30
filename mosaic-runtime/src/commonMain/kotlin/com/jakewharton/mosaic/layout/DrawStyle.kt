package com.jakewharton.mosaic.layout

import dev.drewhamilton.poko.Poko

/**
 * Represents how the shapes should be drawn within a [DrawScope]
 */
public sealed interface DrawStyle {

	/**
	 * Default [DrawStyle] indicating shapes should be drawn completely filled in with the
	 * provided color or pattern
	 */
	public data object Fill : DrawStyle

	/**
	 * [DrawStyle] that provides information for drawing content with a stroke
	 *
	 * @param width Configure the width of the stroke in cells
	 */
	@Poko
	public class Stroke(internal val width: Int = 1) : DrawStyle
}
