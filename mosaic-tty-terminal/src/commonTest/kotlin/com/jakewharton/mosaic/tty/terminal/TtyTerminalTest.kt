package com.jakewharton.mosaic.tty.terminal

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.encodeToByteString

class TtyTerminalTest {
	@Test fun worksEvenWithoutReply() = terminalTest {
		val teardown = withTerminal { setup ->
			assertThat(setup).isEqualTo("${CSI}0c".encodeToByteString())
		}
		assertThat(teardown).isEqualTo(ByteString())
	}
}
