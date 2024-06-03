package example

import com.jakewharton.mosaic.runMosaicBlocking
import example.palletes.BlackBirdColorsPalette
import example.palletes.DefaultColorsPalette
import example.palletes.DraculaColorsPalette
import example.palletes.NordColorsPalette
import example.palletes.OneDarkColorsPalette
import example.palletes.SolarizedDarkColorsPalette
import kotlinx.coroutines.awaitCancellation

fun main(args: Array<String>) = runMosaicBlocking {
	val rrtopColorsPalette = parseArgsForColorsPalette(args)
	val rrtopViewModel = RrtopViewModel(this)

	setContent {
		RrtopApp(rrtopViewModel, rrtopColorsPalette)
	}

	awaitCancellation()
}

private fun parseArgsForColorsPalette(args: Array<String>): RrtopColorsPalette {
	val keyIndex = args.indexOf("-c")
	if (keyIndex < 0 || keyIndex >= args.lastIndex) {
		return DefaultColorsPalette
	}
	return when (args[keyIndex + 1]) {
		"default" -> DefaultColorsPalette
		"blackbird" -> BlackBirdColorsPalette
		"dracula" -> DraculaColorsPalette
		"nord" -> NordColorsPalette
		"one-dark" -> OneDarkColorsPalette
		"solarized-dark" -> SolarizedDarkColorsPalette
		else -> DefaultColorsPalette
	}
}
