package com.jakewharton.mosaic.terminal

@OptIn(ExperimentalStdlibApi::class)
internal fun StdinWriter.writeHex(hex: String) {
	write(hex.hexToByteArray())
}
