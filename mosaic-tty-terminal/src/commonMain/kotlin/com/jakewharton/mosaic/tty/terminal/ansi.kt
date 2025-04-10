package com.jakewharton.mosaic.tty.terminal

internal const val ESC = "\u001B"
internal const val ST = "${ESC}\\"

internal const val APC = "${ESC}_"
internal const val CSI = "$ESC["
internal const val DCS = "${ESC}P"
internal const val OSC = "$ESC]"
internal const val SS3 = "${ESC}O"

internal const val cursorMode = 25
internal const val cursorEnable = "$CSI?${cursorMode}h"
internal const val cursorDisable = "$CSI?${cursorMode}l"

internal const val focusMode = 1004
internal const val focusEnable = "$CSI?${focusMode}h"
internal const val focusDisable = "$CSI?${focusMode}l"

internal const val synchronizedOutputMode = 2026

internal const val systemThemeMode = 2031
internal const val systemThemeEnable = "$CSI?${systemThemeMode}h"
internal const val systemThemeDisable = "$CSI?${systemThemeMode}l"

internal const val inBandResizeMode = 2048
internal const val inBandResizeEnable = "$CSI?${inBandResizeMode}h"
internal const val inBandResizeDisable = "$CSI?${inBandResizeMode}l"
