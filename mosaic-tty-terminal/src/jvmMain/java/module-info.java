module mosaic.tty.terminal {
	requires transitive kotlin.stdlib;
	requires transitive kotlinx.coroutines.core;
	requires transitive mosaic.tty;
	requires transitive mosaic.terminal;

	exports com.jakewharton.mosaic.tty.terminal;
}
