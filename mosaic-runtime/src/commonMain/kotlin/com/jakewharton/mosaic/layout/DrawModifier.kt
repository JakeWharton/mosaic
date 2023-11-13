/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jakewharton.mosaic.layout

import com.jakewharton.mosaic.modifier.Modifier

public interface DrawModifier : Modifier.Element {
	public fun ContentDrawScope.draw()

	// Force subclasses to add a debugging implementation.
	override fun toString(): String
}

public fun Modifier.drawBehind(
	onDraw: DrawScope.() -> Unit,
): Modifier = this then DrawBehindElement(onDraw)

private class DrawBehindElement(
	val onDraw: DrawScope.() -> Unit,
) : DrawModifier {
	override fun ContentDrawScope.draw() {
		onDraw()
		drawContent()
	}

	override fun toString() = "DrawBehind"
}
