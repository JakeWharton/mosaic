package com.jakewharton.mosaic.tty

import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
internal actual fun isWindows() = Platform.osFamily == OsFamily.WINDOWS
