package com.jakewharton.mosaic.tty

import kotlin.native.runtime.GC
import kotlin.native.runtime.NativeRuntimeApi

@OptIn(NativeRuntimeApi::class)
internal actual fun gc() {
	GC.collect()
}
