module mosaic.runtime {
	requires transitive kotlin.stdlib;
	requires transitive kotlinx.coroutines.core;
	requires transitive mosaic.terminal;
	requires mosaic.tty.terminal;

	exports com.jakewharton.mosaic;
	exports com.jakewharton.mosaic.layout;
	exports com.jakewharton.mosaic.modifier;
	exports com.jakewharton.mosaic.text;
	exports com.jakewharton.mosaic.ui;
	exports com.jakewharton.mosaic.ui.unit;
}
