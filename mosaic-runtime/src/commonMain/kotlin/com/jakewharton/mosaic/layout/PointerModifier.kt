package com.jakewharton.mosaic.layout

import androidx.compose.runtime.Immutable
import com.jakewharton.mosaic.modifier.Modifier
import dev.drewhamilton.poko.Poko

public interface PointerModifier : Modifier.Element {
	/**
	 * This function is called when a [PointerEvent] is received by this node during the downward
	 * pass. It gives ancestors of a component the chance to intercept an event. Return true to
	 * stop propagation of this event. If you return false, the event will be sent to this
	 * [PointerModifier]'s child. If the child does not consume the event, it will be sent
	 * back up to the root using the [onPointerEvent] function.
	 */
	public fun onPrePointerEvent(event: PointerEvent): Boolean

	/**
	 * This function is called when a [PointerEvent] is received by this node during the upward pass.
	 * While implementing this callback, return true to stop propagation of this event. If you
	 * return false, the event will be sent to this [PointerEvent]'s parent.
	 */
	public fun onPointerEvent(event: PointerEvent): Boolean
}

@[Immutable Poko]
public class PointerEvent(
	public val x: Int,
	public val y: Int,
	public val left: Boolean = false,
	public val right: Boolean = false,
	public val middle: Boolean = false,
	public val mouse4: Boolean = false,
	public val mouse5: Boolean = false,
	public val wheelUp: Boolean = false,
	public val wheelDown: Boolean = false,
	public val wheelLeft: Boolean = false,
	public val wheelRight: Boolean = false,
	public val ctrl: Boolean = false,
	public val alt: Boolean = false,
	public val shift: Boolean = false,
)

/**
 * Adding this [modifier][Modifier] to the [modifier][Modifier] parameter of a component will allow
 * it to intercept pointer events.
 *
 * @param onPreviewPointerEvent This callback is invoked when the user interacts with a pointer
 *   source. It gives ancestors of a focused component the chance to intercept a [PointerEvent].
 *   Return true to stop propagation of this event. If you return false, the event will be sent
 *   to this [onPreviewPointerEvent]'s child. If none of the children consume the event, it will be
 *   sent back up to the root [PointerModifier] using the `onPointerEvent` callback.
 */
public fun Modifier.onPreviewPointerEvent(
	onPreviewPointerEvent: (event: PointerEvent) -> Boolean,
): Modifier = this then PointerModifierElement(onPreviewPointerEvent, null)

/**
 * Adding this [modifier][Modifier] to the [modifier][Modifier] parameter of a component will allow
 * it to intercept pointer events.
 *
 * @param onPointerEvent This callback is invoked when the user interacts with a pointer source.
 *   While implementing this callback, return true to stop propagation of this event. If you return
 *   false, the event will be sent to this [onPointerEvent]'s parent.
 */
public fun Modifier.onPointerEvent(
	onPointerEvent: (event: PointerEvent) -> Boolean,
): Modifier = this then PointerModifierElement(null, onPointerEvent)

private class PointerModifierElement(
	val onPreEvent: ((PointerEvent) -> Boolean)?,
	val onEvent: ((PointerEvent) -> Boolean)?,
) : PointerModifier {
	override fun onPrePointerEvent(event: PointerEvent) = onPreEvent?.invoke(event) ?: false
	override fun onPointerEvent(event: PointerEvent) = onEvent?.invoke(event) ?: false
}
