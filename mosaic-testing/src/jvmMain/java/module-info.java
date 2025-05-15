module mosaic.testing {
	requires transitive kotlin.stdlib;
	requires transitive kotlinx.coroutines.core;
	requires transitive mosaic.runtime;

	exports com.jakewharton.mosaic.testing;
}
