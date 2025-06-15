package com.jakewharton.mosaic.focus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.jakewharton.mosaic.layout.focusable
import com.jakewharton.mosaic.modifier.Modifier
import com.jakewharton.mosaic.terminal.KeyboardEvent
import com.jakewharton.mosaic.testing.runMosaicTest
import com.jakewharton.mosaic.ui.Box
import com.jakewharton.mosaic.ui.Column
import com.jakewharton.mosaic.ui.Row
import com.jakewharton.mosaic.ui.RowScope
import com.jakewharton.mosaic.ui.Text
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest

class MosaicFocusManagerTest {

	@Test
	fun initialFocusTest() = runTest {
		runMosaicTest {
			var button1Focus: FocusState? = null
			var button2Focus: FocusState? = null
			setContent {
				Column {
					Button({ button1Focus = it }) { Text("Button 1") }
					Button({ button2Focus = it }) { Text("Button 2") }
				}
			}
			assertThat(button1Focus?.isFocused).isEqualTo(true)
			assertThat(button2Focus?.isFocused).isNull()
		}
	}

	@Test
	fun moveFocusTest() = runTest {
		runMosaicTest {
			var button1Focus: FocusState? = null
			var button2Focus: FocusState? = null
			setContent {
				Column {
					Button({ button1Focus = it }) { Text("Button 1") }
					Button({ button2Focus = it }) { Text("Button 2") }
				}
			}
			sendKeyEvent(KeyboardEvent(9)) // Tab
			awaitSnapshot()
			assertThat(button1Focus?.isFocused).isEqualTo(false)
			assertThat(button2Focus?.isFocused).isEqualTo(true)
		}
	}

	@Test
	fun clearFocusTest() = runTest {
		runMosaicTest {
			var button1Focus: FocusState? = null
			var button2Focus: FocusState? = null
			setContent {
				val focusManager = LocalFocusManager.current
				Column {
					Button({ button1Focus = it }) { Text("Button 1") }
					Button({ button2Focus = it }) { Text("Button 2") }
				}
				LaunchedEffect(Unit) {
					focusManager.moveFocus(FocusDirection.Next)
					focusManager.moveFocus(FocusDirection.Next)
					delay(0.5.seconds)
					focusManager.clearFocus()
				}
			}
			awaitSnapshot()
			awaitComplete()
			assertThat(button1Focus?.isFocused).isEqualTo(false)
			assertThat(button2Focus?.isFocused).isEqualTo(false)
		}
	}

	@Test
	fun setFocusAfterClearAgainTest() = runTest {
		runMosaicTest {
			var button1Focus: FocusState? = null
			var button2Focus: FocusState? = null
			setContent {
				val focusManager = LocalFocusManager.current
				Column {
					Button({ button1Focus = it }) { Text("Button 1") }
					Button({ button2Focus = it }) { Text("Button 2") }
				}
				LaunchedEffect(Unit) {
					focusManager.moveFocus(FocusDirection.Next)
					delay(0.1.seconds)
					focusManager.clearFocus()
					delay(0.1.seconds)
					focusManager.moveFocus(FocusDirection.Next)
				}
			}
			awaitSnapshot()
			awaitComplete()
			assertThat(button1Focus?.isFocused).isEqualTo(true)
			assertThat(button2Focus?.isFocused).isEqualTo(false)
		}
	}

	@Test
	fun moveFocusForwardAround() = runTest {
		runMosaicTest {
			var button1Focus: FocusState? = null
			var button2Focus: FocusState? = null
			var button3Focus: FocusState? = null
			setContent {
				val focusManager = LocalFocusManager.current
				Column {
					Button({ button1Focus = it }) { Text("Button 1") }
					Button({ button2Focus = it }) { Text("Button 2") }
					Button({ button3Focus = it }) { Text("Button 3") }
				}
				LaunchedEffect(Unit) {
					focusManager.moveFocus(FocusDirection.Next)
					delay(0.1.seconds)
					focusManager.moveFocus(FocusDirection.Next)
					delay(0.1.seconds)
					focusManager.moveFocus(FocusDirection.Next)
				}
			}
			awaitSnapshot()
			awaitComplete()
			assertThat(button1Focus?.isFocused).isEqualTo(true)
			assertThat(button2Focus?.isFocused).isEqualTo(false)
			assertThat(button3Focus?.isFocused).isEqualTo(false)
		}
	}

	@Test
	fun moveFocusBackwardsAroundTest() = runTest {
		runMosaicTest {
			var button1Focus: FocusState? = null
			var button2Focus: FocusState? = null
			var button3Focus: FocusState? = null
			setContent {
				val focusManager = LocalFocusManager.current
				Column {
					Button({ button1Focus = it }) { Text("Button 1") }
					Button({ button2Focus = it }) { Text("Button 2") }
					Button({ button3Focus = it }) { Text("Button 3") }
				}
				LaunchedEffect(Unit) {
					delay(0.1.seconds)
					focusManager.moveFocus(FocusDirection.Previous)
				}
			}
			awaitSnapshot()
			awaitComplete()
			assertThat(button1Focus?.isFocused).isEqualTo(false)
			assertThat(button2Focus?.isFocused).isNull()
			assertThat(button3Focus?.isFocused).isEqualTo(true)
		}
	}

	@Test
	fun continueFocusWhenFocusedElementRemoved() = runTest {
		runMosaicTest {
			var button1Focus: FocusState? = null
			var button2Focus: FocusState? = null
			var button3Focus: FocusState? = null
			setContent {
				var showButton2 by remember { mutableStateOf(true) }
				val focusManager = LocalFocusManager.current
				Column {
					Button({ button1Focus = it }) { Text("Button 1") }
					if (showButton2) {
						Button({ button2Focus = it }) { Text("Button 2") }
					}
					Button({ button3Focus = it }) { Text("Button 3") }
				}
				LaunchedEffect(Unit) {
					delay(0.1.seconds)
					focusManager.moveFocus(FocusDirection.Next)
					focusManager.moveFocus(FocusDirection.Next)
					focusManager.moveFocus(FocusDirection.Next)
					focusManager.moveFocus(FocusDirection.Next)
					delay(0.1.seconds)
					showButton2 = false
					delay(0.1.seconds)
					focusManager.moveFocus(FocusDirection.Next)
				}
			}
			awaitSnapshot()
			awaitSnapshot()
			awaitComplete()
			assertThat(button1Focus?.isFocused).isEqualTo(true)
			// assertThat(button2Focus?.isFocused).isEqualTo(false) // deleted so no state update
			assertThat(button3Focus?.isFocused).isEqualTo(false)
		}
	}

	@Test
	fun continueFocusBackwardsWhenFocusedElementRemoved() = runTest {
		runMosaicTest {
			var button1Focus: FocusState? = null
			var button2Focus: FocusState? = null
			var button3Focus: FocusState? = null
			setContent {
				var showButton2 by remember { mutableStateOf(true) }
				val focusManager = LocalFocusManager.current
				Column {
					Button({ button1Focus = it }) { Text("Button 1") }
					if (showButton2) {
						Button({ button2Focus = it }) { Text("Button 2") }
					}
					Button({ button3Focus = it }) { Text("Button 3") }
				}
				LaunchedEffect(Unit) {
					delay(0.1.seconds)
					focusManager.moveFocus(FocusDirection.Next)
					focusManager.moveFocus(FocusDirection.Next)
					focusManager.moveFocus(FocusDirection.Next)
					focusManager.moveFocus(FocusDirection.Next)
					delay(0.1.seconds)
					showButton2 = false
					delay(0.1.seconds)
					focusManager.moveFocus(FocusDirection.Previous)
				}
			}
			awaitSnapshot()
			awaitSnapshot()
			awaitComplete()
			assertThat(button1Focus?.isFocused).isEqualTo(false)
			// assertThat(button2Focus?.isFocused).isEqualTo(false) // deleted so no state update
			assertThat(button3Focus?.isFocused).isEqualTo(true)
		}
	}

	@Test
	fun focusToParentFocusableElementTest() = runTest {
		runMosaicTest {
			var parentFocus: FocusState? = null
			var button1Focus: FocusState? = null
			var button2Focus: FocusState? = null
			setContent {
				Box {
					Column(modifier = Modifier.focusable { parentFocus = it }) {
						Button({ button1Focus = it }) { Text("Button 1") }
						Button({ button2Focus = it }) { Text("Button 2") }
					}
				}
			}
			awaitSnapshot()
			awaitComplete()
			assertThat(parentFocus?.isFocused).isEqualTo(true)
			assertThat(button1Focus?.isFocused).isNull()
			assertThat(button2Focus?.isFocused).isNull()
		}
	}

	@Test
	fun focusParentToChildFocusableElementTest() = runTest {
		runMosaicTest {
			var parentFocus: FocusState? = null
			var button1Focus: FocusState? = null
			var button2Focus: FocusState? = null
			setContent {
				val focusManager = LocalFocusManager.current
				Box {
					Column(modifier = Modifier.focusable { parentFocus = it }) {
						Button({ button1Focus = it }) { Text("Button 1") }
						Button({ button2Focus = it }) { Text("Button 2") }
					}
				}
				LaunchedEffect(Unit) {
					focusManager.moveFocus(FocusDirection.Next)
				}
			}
			awaitSnapshot()
			awaitComplete()
			assertThat(parentFocus?.isFocused).isEqualTo(false)
			assertThat(parentFocus?.hasFocus).isEqualTo(true)
			assertThat(button1Focus?.isFocused).isEqualTo(true)
			assertThat(button2Focus?.isFocused).isNull()
		}
	}

	@Test
	fun focusAroundToParentAgainTest() = runTest {
		runMosaicTest {
			var parentFocus: FocusState? = null
			var button1Focus: FocusState? = null
			var button2Focus: FocusState? = null
			setContent {
				val focusManager = LocalFocusManager.current
				Box {
					Column(modifier = Modifier.focusable { parentFocus = it }) {
						Button({ button1Focus = it }) { Text("Button 1") }
						Button({ button2Focus = it }) { Text("Button 2") }
					}
				}
				LaunchedEffect(Unit) {
					focusManager.moveFocus(FocusDirection.Next)
					focusManager.moveFocus(FocusDirection.Next)
					focusManager.moveFocus(FocusDirection.Next)
				}
			}
			awaitSnapshot()
			awaitComplete()
			assertThat(parentFocus?.isFocused).isEqualTo(true)
			assertThat(parentFocus?.hasFocus).isEqualTo(true)
			assertThat(button1Focus?.isFocused).isEqualTo(false)
			assertThat(button2Focus?.isFocused).isEqualTo(false)
		}
	}

	@Test
	fun focusParentToPreviousFocusableElementTest() = runTest {
		runMosaicTest {
			var parentFocus: FocusState? = null
			var button1Focus: FocusState? = null
			var button2Focus: FocusState? = null
			setContent {
				val focusManager = LocalFocusManager.current
				Box {
					Column(modifier = Modifier.focusable { parentFocus = it }) {
						Button({ button1Focus = it }) { Text("Button 1") }
						Button({ button2Focus = it }) { Text("Button 2") }
					}
				}
				LaunchedEffect(Unit) {
					focusManager.moveFocus(FocusDirection.Previous)
				}
			}
			awaitSnapshot()
			awaitComplete()
			assertThat(parentFocus?.isFocused).isEqualTo(false)
			assertThat(parentFocus?.hasFocus).isEqualTo(true)
			assertThat(button1Focus?.isFocused).isNull()
			assertThat(button2Focus?.isFocused).isEqualTo(true)
		}
	}

	@Test
	fun focusBackwardsAroundToParentAgainTest() = runTest {
		runMosaicTest {
			var parentFocus: FocusState? = null
			var button1Focus: FocusState? = null
			var button2Focus: FocusState? = null
			setContent {
				val focusManager = LocalFocusManager.current
				Box {
					Column(modifier = Modifier.focusable { parentFocus = it }) {
						Button({ button1Focus = it }) { Text("Button 1") }
						Button({ button2Focus = it }) { Text("Button 2") }
					}
				}
				LaunchedEffect(Unit) {
					focusManager.moveFocus(FocusDirection.Previous)
					focusManager.moveFocus(FocusDirection.Previous)
					focusManager.moveFocus(FocusDirection.Previous)
				}
			}
			awaitSnapshot()
			awaitComplete()
			assertThat(parentFocus?.isFocused).isEqualTo(true)
			assertThat(parentFocus?.hasFocus).isEqualTo(true)
			assertThat(button1Focus?.isFocused).isEqualTo(false)
			assertThat(button2Focus?.isFocused).isEqualTo(false)
		}
	}

	@Composable
	private fun Button(onFocusStateChange: ((FocusState) -> Unit)?, content: @Composable RowScope.() -> Unit) {
		Row(modifier = Modifier.focusable(onFocusStateChange), content = content)
	}
}
