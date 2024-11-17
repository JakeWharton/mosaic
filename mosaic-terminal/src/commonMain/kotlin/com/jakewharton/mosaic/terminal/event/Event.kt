package com.jakewharton.mosaic.terminal.event

internal sealed interface Event

// Some temporary events while we spin up parsing...

internal object KeyEscape : Event
