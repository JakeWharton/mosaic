package com.jakewharton.mosaic

expect fun runMosaicBlocking(body: suspend MosaicScope.() -> Unit)
