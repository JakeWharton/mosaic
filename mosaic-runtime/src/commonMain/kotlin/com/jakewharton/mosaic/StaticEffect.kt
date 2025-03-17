@file:JvmName("Static")

package com.jakewharton.mosaic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.staticCompositionLocalOf
import kotlin.jvm.JvmName
import kotlinx.coroutines.channels.SendChannel

@Stable
public class StaticLogger internal constructor(
	private val logs: SendChannel<Any>,
	private val onLog: () -> Unit,
) {
	private fun send(value: Any) {
		logs.trySend(value).getOrThrow()
		onLog()
	}

	public fun log(string: String): Unit = send(string)
	public fun log(canvas: TextCanvas): Unit = send(canvas)

	public operator fun plusAssign(string: String): Unit = send(string)
	public operator fun plusAssign(canvas: TextCanvas): Unit = send(canvas)
}

public val LocalStaticLogger: ProvidableCompositionLocal<StaticLogger> =
	staticCompositionLocalOf {
		throw AssertionError("No static logger provided")
	}

/**
 * Render [content] once as permanent output above the regular display.
 *
 * The [content] function will be recomposed once and then never again.
 * Any contained [SideEffect]s or [DisposableEffect]s will run (and be disposed),
 * but [LaunchedEffect]s will not launch.
 */
@Composable
public fun StaticEffect(
	content: @Composable () -> Unit,
) {
	val compositionContext = rememberCompositionContext()
	val staticLogging = LocalStaticLogger.current
	SideEffect {
		val applier = MosaicNodeApplier()
		val composition = Composition(applier, compositionContext)
		composition.setContent(content)

		val root = applier.root
		root.measureAndPlace()
		staticLogging += root.draw()

		composition.dispose()
	}
}
