package example.common

import androidx.compose.runtime.Stable
import com.jakewharton.mosaic.layout.ContentDrawScope
import com.jakewharton.mosaic.layout.DrawModifier
import com.jakewharton.mosaic.layout.padding
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.ui.Color

@Stable
fun Modifier.border(
	topStart: Char = '┌',
	topEnd: Char = '┐',
	bottomStart: Char = '└',
	bottomEnd: Char = '┘',
	verticalStart: Char = '│',
	verticalEnd: Char = '│',
	horizontalTop: Char = '─',
	horizontalBottom: Char = '─',
	color: Color = Color.Unspecified,
): Modifier = this.then(
	BorderModifier(
		topStart.toString(),
		topEnd.toString(),
		bottomStart.toString(),
		bottomEnd.toString(),
		verticalStart.toString(),
		verticalEnd.toString(),
		horizontalTop.toString(),
		horizontalBottom.toString(),
		color,
	),
).padding(all = 1)

private class BorderModifier(
	private val topStart: String,
	private val topEnd: String,
	private val bottomStart: String,
	private val bottomEnd: String,
	private val verticalStart: String,
	private val verticalEnd: String,
	private val horizontalTop: String,
	private val horizontalBottom: String,
	private val color: Color,
) : DrawModifier {

	override fun ContentDrawScope.draw() {
		drawText(string = topStart, row = 0, column = 0, foreground = color)
		drawText(string = topEnd, row = 0, column = width - 1, foreground = color)
		drawText(string = bottomEnd, row = height - 1, column = width - 1, foreground = color)
		drawText(string = bottomStart, row = height - 1, column = 0, foreground = color)
		drawText(string = horizontalTop.repeat(width - 2), row = 0, column = 1, foreground = color)
		drawText(
			string = horizontalBottom.repeat(width - 2),
			row = height - 1,
			column = 1,
			foreground = color,
		)
		for (row in 1..height - 2) {
			drawText(string = verticalStart, row = row, column = 0, foreground = color)
			drawText(string = verticalEnd, row = row, column = width - 1, foreground = color)
		}
		drawContent()
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as BorderModifier

		if (verticalStart != other.verticalStart) return false
		if (topStart != other.topStart) return false
		if (horizontalTop != other.horizontalTop) return false
		if (topEnd != other.topEnd) return false
		if (verticalEnd != other.verticalEnd) return false
		if (bottomEnd != other.bottomEnd) return false
		if (horizontalBottom != other.horizontalBottom) return false
		if (bottomStart != other.bottomStart) return false
		if (color != other.color) return false

		return true
	}

	override fun hashCode(): Int {
		var result = verticalStart.hashCode()
		result = 31 * result + topStart.hashCode()
		result = 31 * result + horizontalTop.hashCode()
		result = 31 * result + topEnd.hashCode()
		result = 31 * result + verticalEnd.hashCode()
		result = 31 * result + bottomEnd.hashCode()
		result = 31 * result + horizontalBottom.hashCode()
		result = 31 * result + bottomStart.hashCode()
		result = 31 * result + color.hashCode()
		return result
	}

	override fun toString() = "Border"
}
