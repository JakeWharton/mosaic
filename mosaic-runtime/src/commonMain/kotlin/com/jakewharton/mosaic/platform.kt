package com.jakewharton.mosaic

import com.jakewharton.mosaic.ui.unit.IntSize

internal expect fun platformDisplay(chars: CharSequence)

internal expect fun getPlatformTerminalSize(): IntSize
