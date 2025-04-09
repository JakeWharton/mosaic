package com.jakewharton.mosaic.tty.terminal

import assertk.Assert
import assertk.fail
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.encodeToByteString
import kotlinx.io.bytestring.indexOf

fun Assert<ByteString>.contains(s: String) = given { b ->
	val needle = s.encodeToByteString()
	if (b.indexOf(needle) == -1) {
		fail("expected $b to contain $needle, but did not")
	}
}

fun Assert<ByteString>.doesNotContain(s: String) = given { b ->
	val needle = s.encodeToByteString()
	if (b.indexOf(needle) != -1) {
		fail("expected $b to not contain $needle, but it does")
	}
}
