@file:JvmName("Main")

package example

import com.jakewharton.finalization.withFinalizationHook
import com.jakewharton.mosaic.terminal.Tty
import kotlin.jvm.JvmName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalStdlibApi::class)
fun main() = runBlocking {
	val values = Channel<String>(UNLIMITED)
	launch(Dispatchers.IO) {
		val buffer = ByteArray(1024)
		while (isActive) {
			val read = stdinRead(buffer, 0, 1024)
			val hex = buffer.toHexString(endIndex = read)
			values.trySend(hex)
			if (hex == "03") {
				values.close()
				break
			}
		}
	}

	Tty.save()
	Tty.setRawMode()
	withFinalizationHook(
		hook = { Tty.restore() },
		block = {
			for (value in values) {
				print(value)
				print("\r\n")
			}
		},
	)
}
