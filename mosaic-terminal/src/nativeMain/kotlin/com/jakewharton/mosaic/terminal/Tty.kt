package com.jakewharton.mosaic.terminal

import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
public actual object Tty {
	public actual fun save(): Boolean = tty_save()
	public actual fun restore(): Boolean = tty_restore()
	public actual fun setRawMode(): Boolean = tty_setRawMode()
}
