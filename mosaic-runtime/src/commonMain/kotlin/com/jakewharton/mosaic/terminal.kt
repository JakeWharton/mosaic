package com.jakewharton.mosaic

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import com.jakewharton.mosaic.terminal.TerminalReader
import com.jakewharton.mosaic.terminal.event.DecModeReportEvent
import com.jakewharton.mosaic.terminal.event.KittyGraphicsEvent
import com.jakewharton.mosaic.terminal.event.KittyKeyboardQueryEvent
import com.jakewharton.mosaic.terminal.event.OperatingStatusResponseEvent
import com.jakewharton.mosaic.terminal.event.PrimaryDeviceAttributesEvent
import com.jakewharton.mosaic.ui.unit.IntSize
import dev.drewhamilton.poko.Poko
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.withTimeoutOrNull

public val LocalTerminal: ProvidableCompositionLocal<Terminal> = compositionLocalOf {
	error("No terminal info provided")
}

@[Immutable Poko]
public class Terminal(
	public val size: IntSize,
) {
	public companion object {
		public val Default: Terminal = Terminal(
			// https://en.wikipedia.org/wiki/VT52
			size = IntSize(width = 80, height = 24),
		)
	}
}

internal suspend fun TerminalReader.queryCapabilities(): Capabilities {
	var focus: DecModeReportEvent.Setting? = null
	var synchronizedRendering: DecModeReportEvent.Setting? = null
	var systemTheme: DecModeReportEvent.Setting? = null
	var inBandResize: DecModeReportEvent.Setting? = null
	var kittyKeyboard = false
	var kittyGraphics = false
	// TODO var kittyNotifications: Boolean = false
	// TODO var kittyPointerShape: Boolean = false

	// Step 1: DA1 (primary device attributes) query. We need to determine the level of VT compliance
	// before issuing any other queries, as they may not be properly parsed and inadvertently render.
	print("${CSI}0c")

	// Spend at most 1 second determining capabilities. In theory, there could exist a terminal which
	// does not respond to DA1 or DSR. Does that terminal actually work? Who knows, but we don't want
	// to hang forever waiting. Take whatever we got so far (if anything) and move on with rendering.
	withTimeoutOrNull(1.seconds) {
		for (event in events) {
			when (event) {
				is PrimaryDeviceAttributesEvent -> {
					if (event.id == 1) {
						// VT100 terminals can't handle most of the other queries so just bail.
						break
					}

					// Step 2: individual feature queries.
					print(
						"$CSI?${modeFocus}\$p" +
							"$CSI?${modeSynchronizedRendering}\$p" +
							"$CSI?${modeSystemTheme}\$p" +
							"$CSI?${modeInBandResize}\$p" +
							"$CSI?u" + // Kitty keyboard
							"${APC}Gi=31,s=1,v=1,a=q,t=d,f=24;AAAA$ST" + // Kitty graphics
							// TODO "${OSC}99;i=1:p=?$ST" + // Kitty notifications
							// TODO "${OSC}22;?__current__$ST" + // Kitty pointer shape
							"${CSI}5n", // DSR (used as an end marker)
					)
				}

				is DecModeReportEvent -> {
					when (event.mode) {
						modeFocus -> focus = event.setting
						modeSynchronizedRendering -> synchronizedRendering = event.setting
						modeSystemTheme -> systemTheme = event.setting
						modeInBandResize -> inBandResize = event.setting
					}
				}

				is KittyKeyboardQueryEvent -> {
					kittyKeyboard = true
				}

				is KittyGraphicsEvent -> {
					kittyGraphics = true
				}

				is OperatingStatusResponseEvent -> break
				else -> {}
			}
		}
	}

	return Capabilities(
		focus = focus,
		synchronizedRendering = synchronizedRendering,
		systemTheme = systemTheme,
		inBandResize = inBandResize,
		kittyKeyboard = kittyKeyboard,
		kittyGraphics = kittyGraphics,
	)
}

internal class Capabilities(
	var focus: DecModeReportEvent.Setting?,
	var synchronizedRendering: DecModeReportEvent.Setting?,
	var systemTheme: DecModeReportEvent.Setting?,
	var inBandResize: DecModeReportEvent.Setting?,
	var kittyKeyboard: Boolean,
	var kittyGraphics: Boolean,
) {
	companion object {
		val Default = Capabilities(
			focus = null,
			synchronizedRendering = null,
			systemTheme = null,
			inBandResize = null,
			kittyKeyboard = false,
			kittyGraphics = false,
		)
	}
}
