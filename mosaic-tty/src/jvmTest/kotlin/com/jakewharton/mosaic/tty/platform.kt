package com.jakewharton.mosaic.tty

internal actual fun isWindows() = System.getProperty("os.name").contains("windows", ignoreCase = true)
