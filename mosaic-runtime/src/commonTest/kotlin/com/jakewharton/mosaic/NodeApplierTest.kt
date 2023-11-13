package com.jakewharton.mosaic

import androidx.compose.runtime.Applier
import com.jakewharton.mosaic.layout.DebugPolicy
import com.jakewharton.mosaic.layout.MosaicNode
import com.jakewharton.mosaic.ui.NodeFactory
import kotlin.test.Test
import kotlin.test.assertEquals

class NodeApplierTest {
	private val root = createRootNode()
	private val applier = MosaicNodeApplier(root)

	private fun <T> Applier<T>.insert(index: Int, instance: T) {
		insertBottomUp(index, instance)
	}

	@Test fun insertAtEnd() {
		val one = textNode("one")
		applier.insert(0, one)
		val two = textNode("two")
		applier.insert(1, two)
		val three = textNode("three")
		applier.insert(2, three)
		assertChildren(one, two, three)
	}

	@Test fun insertAtStart() {
		val one = textNode("one")
		applier.insert(0, one)
		val two = textNode("two")
		applier.insert(0, two)
		val three = textNode("three")
		applier.insert(0, three)
		assertChildren(three, two, one)
	}

	@Test fun insertAtMiddle() {
		val one = textNode("one")
		applier.insert(0, one)
		val two = textNode("two")
		applier.insert(1, two)
		val three = textNode("three")
		applier.insert(1, three)
		assertChildren(one, three, two)
	}

	@Test fun removeSingleAtEnd() {
		val one = textNode("one")
		applier.insert(0, one)
		val two = textNode("two")
		applier.insert(1, two)
		val three = textNode("three")
		applier.insert(2, three)

		applier.remove(2, 1)
		assertChildren(one, two)
	}

	@Test fun removeSingleAtStart() {
		val one = textNode("one")
		applier.insert(0, one)
		val two = textNode("two")
		applier.insert(1, two)
		val three = textNode("three")
		applier.insert(2, three)

		applier.remove(0, 1)
		assertChildren(two, three)
	}

	@Test fun removeSingleInMiddle() {
		val one = textNode("one")
		applier.insert(0, one)
		val two = textNode("two")
		applier.insert(1, two)
		val three = textNode("three")
		applier.insert(2, three)

		applier.remove(1, 1)
		assertChildren(one, three)
	}

	@Test fun removeMultipleAtEnd() {
		val one = textNode("one")
		applier.insert(0, one)
		val two = textNode("two")
		applier.insert(1, two)
		val three = textNode("three")
		applier.insert(2, three)

		applier.remove(1, 2)
		assertChildren(one)
	}

	@Test fun removeMultipleAtStart() {
		val one = textNode("one")
		applier.insert(0, one)
		val two = textNode("two")
		applier.insert(1, two)
		val three = textNode("three")
		applier.insert(2, three)

		applier.remove(0, 2)
		assertChildren(three)
	}

	@Test fun removeMultipleInMiddle() {
		val one = textNode("one")
		applier.insert(0, one)
		val two = textNode("two")
		applier.insert(1, two)
		val three = textNode("three")
		applier.insert(2, three)
		val four = textNode("four")
		applier.insert(3, four)

		applier.remove(1, 2)
		assertChildren(one, four)
	}

	@Test fun removeAll() {
		val one = textNode("one")
		applier.insert(0, one)
		val two = textNode("two")
		applier.insert(1, two)
		val three = textNode("three")
		applier.insert(2, three)

		applier.remove(0, 3)
		assertChildren()
	}

	@Test fun moveSingleLower() {
		val one = textNode("one")
		applier.insert(0, one)
		val two = textNode("two")
		applier.insert(1, two)
		val three = textNode("three")
		applier.insert(2, three)

		applier.move(2, 0, 1)
		assertChildren(three, one, two)
	}

	@Test fun moveSingleHigher() {
		val one = textNode("one")
		applier.insert(0, one)
		val two = textNode("two")
		applier.insert(1, two)
		val three = textNode("three")
		applier.insert(2, three)

		applier.move(0, 2, 1)
		assertChildren(two, one, three)
	}

	@Test fun moveMultipleLower() {
		val one = textNode("one")
		applier.insert(0, one)
		val two = textNode("two")
		applier.insert(1, two)
		val three = textNode("three")
		applier.insert(2, three)
		val four = textNode("four")
		applier.insert(3, four)

		applier.move(2, 0, 2)
		assertChildren(three, four, one, two)
	}

	@Test fun moveMultipleHigher() {
		val one = textNode("one")
		applier.insert(0, one)
		val two = textNode("two")
		applier.insert(1, two)
		val three = textNode("three")
		applier.insert(2, three)
		val four = textNode("four")
		applier.insert(3, four)

		applier.move(0, 4, 2)
		assertChildren(three, four, one, two)
	}

	private fun assertChildren(vararg nodes: MosaicNode) {
		assertEquals(nodes.toList(), root.children)
	}

	private fun textNode(name: String): MosaicNode {
		return NodeFactory().apply {
			debugPolicy = DebugPolicy { name }
		}
	}
}
