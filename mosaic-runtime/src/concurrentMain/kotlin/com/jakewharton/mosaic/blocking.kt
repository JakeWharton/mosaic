package com.jakewharton.mosaic

import kotlinx.coroutines.runBlocking

fun runMosaicBlocking(body: suspend MosaicScope.() -> Unit) {
	runBlocking {
		runMosaic(body)
	}
}
