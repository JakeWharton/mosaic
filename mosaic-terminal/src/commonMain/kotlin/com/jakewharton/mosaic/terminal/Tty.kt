package com.jakewharton.mosaic.terminal

public expect object Tty {
	public fun save(): Boolean
	public fun restore(): Boolean
	public fun setRawMode(): Boolean
}
