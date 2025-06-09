package com.jakewharton.mosaic.focus

import com.jakewharton.mosaic.layout.MosaicNode
import com.jakewharton.mosaic.layout.onKeyEvent
import com.jakewharton.mosaic.modifier.Modifier

internal class MosaicFocusManager(private val rootNode: MosaicNode) : FocusManager {
	init {
		rootNode.focusState = FocusStateImpl.Active
		rootNode.setModifier(
			Modifier.Companion.onKeyEvent { event ->
				if (event.key == "Tab") {
					moveFocus(FocusDirection.Next)
					true
				} else {
					false
				}
			},
		)
	}

	override fun clearFocus() {
		rootNode.focusState = FocusStateImpl.Active
		for (node in rootNode.children) clearFocus(node)
	}

	private fun clearFocus(node: MosaicNode) {
		node.focusState = FocusStateImpl.Inactive
		for (child in node.children) {
			clearFocus(child)
		}
	}

	override fun moveFocus(focusDirection: FocusDirection): Boolean {
		val marks = mutableMapOf<MosaicNode, FocusStateImpl>()
		var clearFocus = false
		fun addFocusChange(node: MosaicNode?, state: FocusStateImpl?) {
			when {
				node == null -> clearFocus = true
				state != null -> marks[node] = state
				else -> marks.remove(node)
			}
		}

		val result = when (focusDirection) {
			FocusDirection.Next -> rootNode.forwardMarkFocus(changeFocus = ::addFocusChange)
			FocusDirection.Previous -> rootNode.backwardMarkFocus(changeFocus = ::addFocusChange)
			else -> error("Unsupported focus direction: $focusDirection")
		}
		if (clearFocus) {
			clearFocus()
			return true
		}
		if (result) {
			for ((node, focusState) in marks) {
				node.focusState = focusState
			}
		}
		return result
	}

	private fun MosaicNode.forwardMarkFocus(
		searchFromStart: Boolean = false,
		changeFocus: (MosaicNode?, FocusStateImpl?) -> Unit,
	): Boolean {
		return when (focusState) {
			FocusStateImpl.Active -> {
				if (searchFromStart) {
					if (!isFocusable) changeFocus(null, null)
					return false
				}
				for (child in children) {
					if (child.forwardMarkFocus(searchFromStart, changeFocus)) {
						changeFocus(this, FocusStateImpl.ActiveParent)
						return true
					}
				}
				if (this !== rootNode) changeFocus(this, FocusStateImpl.Inactive)
				false
			}

			FocusStateImpl.Inactive -> {
				if (isFocusable) {
					changeFocus(this, FocusStateImpl.Active)
					true
				} else {
					for (child in children) {
						if (child.forwardMarkFocus(searchFromStart, changeFocus)) {
							changeFocus(this, FocusStateImpl.ActiveParent)
							return true
						}
					}
					false
				}
			}

			FocusStateImpl.ActiveParent -> {
				// What if active moved out?
				val focusChild = children.indexOfFirst { it.focusState.hasFocus }
				if (searchFromStart) {
					for (childIndex in 0..if (focusChild != -1) focusChild else children.lastIndex) {
						if (children[childIndex].forwardMarkFocus(searchFromStart, changeFocus)) {
							changeFocus(this, null)
							return true
						}
					}
				} else if (focusChild != -1) {
					for (childIndex in focusChild..children.lastIndex) {
						if (children[childIndex].forwardMarkFocus(searchFromStart, changeFocus)) return true
					}
				}
				if (this === rootNode) {
					for (childIndex in 0..if (focusChild != -1) focusChild else children.lastIndex) {
						if (children[childIndex].forwardMarkFocus(childIndex == focusChild, changeFocus)) return true
					}
					false
				} else {
					changeFocus(this, FocusStateImpl.Inactive)
					false
				}
			}
		}
	}

	private fun MosaicNode.backwardMarkFocus(
		searchFromStart: Boolean = false,
		changeFocus: (MosaicNode?, FocusStateImpl?) -> Unit,
	): Boolean {
		return when (focusState) {
			FocusStateImpl.Active -> {
				if (searchFromStart) {
					if (!isFocusable) changeFocus(null, null)
					return false
				}
				if (this === rootNode) {
					for (childIndex in children.lastIndex downTo 0) {
						if (children[childIndex].backwardMarkFocus(searchFromStart, changeFocus)) {
							changeFocus(this, FocusStateImpl.ActiveParent)
							return true
						}
					}
				} else {
					changeFocus(this, FocusStateImpl.Inactive)
				}
				false
			}

			FocusStateImpl.Inactive -> {
				for (childIndex in children.lastIndex downTo 0) {
					if (children[childIndex].backwardMarkFocus(searchFromStart, changeFocus)) {
						changeFocus(this, FocusStateImpl.ActiveParent)
						return true
					}
				}
				if (isFocusable) {
					changeFocus(this, FocusStateImpl.Active)
					true
				} else {
					false
				}
			}

			FocusStateImpl.ActiveParent -> {
				val focusChild = children.indexOfFirst { it.focusState.hasFocus }
				if (searchFromStart) {
					for (childIndex in children.lastIndex downTo (if (focusChild != -1) focusChild else 0)) {
						if (children[childIndex].backwardMarkFocus(searchFromStart, changeFocus)) {
							changeFocus(this, null)
							return true
						}
					}
				} else if (focusChild != -1) {
					for (childIndex in focusChild downTo 0) {
						if (children[childIndex].backwardMarkFocus(searchFromStart, changeFocus)) return true
					}
				}
				if (isFocusable) {
					if (this === rootNode) {
						for (childIndex in children.lastIndex downTo if (focusChild != -1) focusChild else 0) {
							if (children[childIndex].backwardMarkFocus(childIndex == focusChild, changeFocus)) return true
						}
						false
					} else {
						changeFocus(this, FocusStateImpl.Active)
						true
					}
				} else {
					changeFocus(this, FocusStateImpl.Inactive)
					false
				}
			}
		}
	}
}
