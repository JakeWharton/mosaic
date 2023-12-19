package example

import com.jakewharton.mosaic.runMosaicBlocking
import example.palletes.BlackBirdColorsPalette
import example.palletes.DefaultColorsPalette
import example.palletes.DraculaColorsPalette
import example.palletes.NordColorsPalette
import example.palletes.OneDarkColorsPalette
import example.palletes.SolarizedDarkColorsPalette

fun main(args: Array<String>) {
	val rrtopColorsPalette = parseArgsForColorsPalette(args)
	val rrtopViewModel = RrtopViewModel()

	runMosaicBlocking {
		RrtopApp(rrtopViewModel, rrtopColorsPalette)
	}
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
