package example

import kotlin.system.exitProcess

internal actual fun exitProcess(code: Int) {
	exitProcess(code)
}
