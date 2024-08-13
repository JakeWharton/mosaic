package com.jakewharton.mosaic.term

import kotlin.test.Test

@OptIn(ExperimentalStdlibApi::class)
class ParserTest {
	@Test
	fun whevs() {
		val bytes = "1b503e7c67686f7374747920302e312e302d6d61696e2b35363230643362661b5c1b5b3f313030343b312479".hexToUByteArray()
		val event = parseInputEvent(bytes, 0, bytes.size)
		println(event)
	}
}
