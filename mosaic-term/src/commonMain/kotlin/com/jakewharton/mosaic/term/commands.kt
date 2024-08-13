package com.jakewharton.mosaic.term

import kotlinx.io.Sink
import kotlinx.io.writeToInternalBuffer

//region DEC private mode

fun Sink.writeDecPrivateModeSet(mode: Int) {
	writeToInternalBuffer {
		writeByte(0x1B.toByte())
		writeByte('['.code.toByte())
		writeByte('?'.code.toByte())
		writeDecimalInt(mode)
		writeByte('h'.code.toByte())
	}
}

fun Sink.writeDecPrivateModeSet(mode1: Int, mode2: Int) {
	writeToInternalBuffer {
		writeByte(0x1B.toByte())
		writeByte('['.code.toByte())
		writeByte('?'.code.toByte())
		writeDecimalInt(mode1)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode2)
		writeByte('h'.code.toByte())
	}
}

fun Sink.writeDecPrivateModeSet(mode1: Int, mode2: Int, mode3: Int) {
	writeToInternalBuffer {
		writeByte(0x1B.toByte())
		writeByte('['.code.toByte())
		writeByte('?'.code.toByte())
		writeDecimalInt(mode1)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode2)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode3)
		writeByte('h'.code.toByte())
	}
}

fun Sink.writeDecPrivateModeSet(mode1: Int, mode2: Int, mode3: Int, mode4: Int) {
	writeToInternalBuffer {
		writeByte(0x1B.toByte())
		writeByte('['.code.toByte())
		writeByte('?'.code.toByte())
		writeDecimalInt(mode1)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode2)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode3)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode4)
		writeByte('h'.code.toByte())
	}
}

fun Sink.writeDecPrivateModeSet(mode1: Int, mode2: Int, mode3: Int, mode4: Int, mode5: Int) {
	writeToInternalBuffer {
		writeByte(0x1B.toByte())
		writeByte('['.code.toByte())
		writeByte('?'.code.toByte())
		writeDecimalInt(mode1)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode2)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode3)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode4)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode5)
		writeByte('h'.code.toByte())
	}
}

fun Sink.writeDecPrivateModeSet(
	mode1: Int,
	mode2: Int,
	mode3: Int,
	mode4: Int,
	mode5: Int,
	mode6: Int,
) {
	writeToInternalBuffer {
		writeByte(0x1B.toByte())
		writeByte('['.code.toByte())
		writeByte('?'.code.toByte())
		writeDecimalInt(mode1)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode2)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode3)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode4)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode5)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode6)
		writeByte('h'.code.toByte())
	}
}

fun Sink.writeDecPrivateModeSet(
	mode1: Int,
	mode2: Int,
	mode3: Int,
	mode4: Int,
	mode5: Int,
	mode6: Int,
	mode7: Int,
) {
	writeToInternalBuffer {
		writeByte(0x1B.toByte())
		writeByte('['.code.toByte())
		writeByte('?'.code.toByte())
		writeDecimalInt(mode1)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode2)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode3)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode4)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode5)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode6)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode7)
		writeByte('h'.code.toByte())
	}
}

fun Sink.writeDecPrivateModeSet(
	mode1: Int,
	mode2: Int,
	mode3: Int,
	mode4: Int,
	mode5: Int,
	mode6: Int,
	mode7: Int,
	mode8: Int,
) {
	writeToInternalBuffer {
		writeByte(0x1B.toByte())
		writeByte('['.code.toByte())
		writeByte('?'.code.toByte())
		writeDecimalInt(mode1)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode2)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode3)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode4)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode5)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode6)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode7)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode8)
		writeByte('h'.code.toByte())
	}
}

fun Sink.writeDecPrivateModeSet(
	mode1: Int,
	mode2: Int,
	mode3: Int,
	mode4: Int,
	mode5: Int,
	mode6: Int,
	mode7: Int,
	mode8: Int,
	vararg rest: Int
) {
	writeToInternalBuffer {
		writeByte(0x1B.toByte())
		writeByte('['.code.toByte())
		writeByte('?'.code.toByte())
		writeDecimalInt(mode1)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode2)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode3)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode4)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode5)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode6)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode7)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode8)
		for (mode in rest) {
			writeByte(';'.code.toByte())
			writeDecimalInt(mode4)
		}
		writeByte('h'.code.toByte())
	}
}

fun Sink.writeDecPrivateModeReset(mode: Int) {
	writeToInternalBuffer {
		writeByte(0x1B.toByte())
		writeByte('['.code.toByte())
		writeByte('?'.code.toByte())
		writeDecimalInt(mode)
		writeByte('l'.code.toByte())
	}
}

fun Sink.writeDecPrivateModeReset(mode1: Int, mode2: Int) {
	writeToInternalBuffer {
		writeByte(0x1B.toByte())
		writeByte('['.code.toByte())
		writeByte('?'.code.toByte())
		writeDecimalInt(mode1)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode2)
		writeByte('l'.code.toByte())
	}
}

fun Sink.writeDecPrivateModeReset(mode1: Int, mode2: Int, mode3: Int) {
	writeToInternalBuffer {
		writeByte(0x1B.toByte())
		writeByte('['.code.toByte())
		writeByte('?'.code.toByte())
		writeDecimalInt(mode1)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode2)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode3)
		writeByte('l'.code.toByte())
	}
}

fun Sink.writeDecPrivateModeReset(mode1: Int, mode2: Int, mode3: Int, mode4: Int) {
	writeToInternalBuffer {
		writeByte(0x1B.toByte())
		writeByte('['.code.toByte())
		writeByte('?'.code.toByte())
		writeDecimalInt(mode1)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode2)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode3)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode4)
		writeByte('l'.code.toByte())
	}
}

fun Sink.writeDecPrivateModeReset(mode1: Int, mode2: Int, mode3: Int, mode4: Int, mode5: Int) {
	writeToInternalBuffer {
		writeByte(0x1B.toByte())
		writeByte('['.code.toByte())
		writeByte('?'.code.toByte())
		writeDecimalInt(mode1)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode2)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode3)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode4)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode5)
		writeByte('l'.code.toByte())
	}
}

fun Sink.writeDecPrivateModeReset(
	mode1: Int,
	mode2: Int,
	mode3: Int,
	mode4: Int,
	mode5: Int,
	mode6: Int,
) {
	writeToInternalBuffer {
		writeByte(0x1B.toByte())
		writeByte('['.code.toByte())
		writeByte('?'.code.toByte())
		writeDecimalInt(mode1)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode2)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode3)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode4)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode5)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode6)
		writeByte('l'.code.toByte())
	}
}

fun Sink.writeDecPrivateModeReset(
	mode1: Int,
	mode2: Int,
	mode3: Int,
	mode4: Int,
	mode5: Int,
	mode6: Int,
	mode7: Int,
) {
	writeToInternalBuffer {
		writeByte(0x1B.toByte())
		writeByte('['.code.toByte())
		writeByte('?'.code.toByte())
		writeDecimalInt(mode1)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode2)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode3)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode4)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode5)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode6)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode7)
		writeByte('l'.code.toByte())
	}
}

fun Sink.writeDecPrivateModeReset(
	mode1: Int,
	mode2: Int,
	mode3: Int,
	mode4: Int,
	mode5: Int,
	mode6: Int,
	mode7: Int,
	mode8: Int,
) {
	writeToInternalBuffer {
		writeByte(0x1B.toByte())
		writeByte('['.code.toByte())
		writeByte('?'.code.toByte())
		writeDecimalInt(mode1)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode2)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode3)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode4)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode5)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode6)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode7)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode8)
		writeByte('l'.code.toByte())
	}
}

fun Sink.writeDecPrivateModeReset(
	mode1: Int,
	mode2: Int,
	mode3: Int,
	mode4: Int,
	mode5: Int,
	mode6: Int,
	mode7: Int,
	mode8: Int,
	vararg rest: Int
) {
	writeToInternalBuffer {
		writeByte(0x1B.toByte())
		writeByte('['.code.toByte())
		writeByte('?'.code.toByte())
		writeDecimalInt(mode1)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode2)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode3)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode4)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode5)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode6)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode7)
		writeByte(';'.code.toByte())
		writeDecimalInt(mode8)
		for (mode in rest) {
			writeByte(';'.code.toByte())
			writeDecimalInt(mode4)
		}
		writeByte('l'.code.toByte())
	}
}

fun Sink.writeDecPrivateModeRequest(mode: Int) {
	writeToInternalBuffer {
		writeByte(0x1B.toByte())
		writeByte('['.code.toByte())
		writeByte('?'.code.toByte())
		writeDecimalInt(mode)
		writeByte('$'.code.toByte())
		writeByte('p'.code.toByte())
	}
}

//endregion

fun Sink.writeSendDeviceAttributesPrimary() {
	writeToInternalBuffer {
		writeByte(0x1B.toByte())
		writeByte('['.code.toByte())
		writeByte('c'.code.toByte())
	}
}

fun Sink.writeSendDeviceAttributesSecondary() {
	writeToInternalBuffer {
		writeByte(0x1B.toByte())
		writeByte('['.code.toByte())
		writeByte('>'.code.toByte())
		writeByte('c'.code.toByte())
	}
}

fun Sink.writeSendDeviceAttributesTertiary() {
	writeToInternalBuffer {
		writeByte(0x1B.toByte())
		writeByte('['.code.toByte())
		writeByte('='.code.toByte())
		// https://invisible-island.net/xterm/ctlseqs/ctlseqs.html says the 0 is required here unlike
		// primary and secondary attribute requests. (In practice I've found it also to be optional)
		writeByte('0'.code.toByte())
		writeByte('c'.code.toByte())
	}
}

fun Sink.writeNameAndVersionRequest() {
	writeToInternalBuffer {
		writeByte(0x1B.toByte())
		writeByte('['.code.toByte())
		writeByte('>'.code.toByte())
		writeByte('0'.code.toByte())
		writeByte('q'.code.toByte())
	}
}
