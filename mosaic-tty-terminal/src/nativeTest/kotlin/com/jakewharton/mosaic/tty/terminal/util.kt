package com.jakewharton.mosaic.tty.terminal

import kotlin.experimental.ExperimentalNativeApi

@OptIn(ExperimentalNativeApi::class)
actual fun isWindows(): Boolean = Platform.osFamily == OsFamily.WINDOWS
