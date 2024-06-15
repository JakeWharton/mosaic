package com.jakewharton.mosaic

import com.jakewharton.mosaic.ui.unit.IntSize

internal expect fun platformDisplay(chars: CharSequence)

internal expect fun getPlatformTerminalSize(): IntSize

internal expect fun getEnv(key: String): String?

internal expect fun runningInIdeaJavaAgent(): Boolean

internal expect fun stdoutInteractive(): Boolean
