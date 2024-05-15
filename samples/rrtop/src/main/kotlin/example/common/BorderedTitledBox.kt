package example.common

import androidx.compose.runtime.Composable
import com.jakewharton.mosaic.layout.offset
import com.jakewharton.mosaic.layout.padding
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.ui.Alignment
import com.jakewharton.mosaic.ui.Box
import com.jakewharton.mosaic.ui.BoxScope
import com.jakewharton.mosaic.ui.Text

@Composable
fun BorderedTitledBox(
	title: String,
	modifier: Modifier = Modifier,
	content: @Composable BoxScope.() -> Unit,
) {
	Box(modifier = modifier.border().padding(horizontal = 1)) {
		Text("┐ $title ┌", modifier = Modifier.align(Alignment.TopStart).offset(x = -1, y = -1))
		content()
	}
}
