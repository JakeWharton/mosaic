package com.jakewharton.mosaic.modifier

import androidx.compose.runtime.Applier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.runtime.RecomposeScope
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.withRunningRecomposer
import assertk.assertThat
import assertk.assertions.containsNone
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNotNull
import kotlin.test.Test
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

private class TestTagModifier<T>(val name: String, val value: T) : Modifier.Element

private fun <T> Modifier.testTag(name: String, value: T) = this then TestTagModifier(name, value)

private fun <T> Modifier.getTestTag(name: String, default: T): T =
	foldIn(default) { acc, element ->
		@Suppress("UNCHECKED_CAST")
		if (element is TestTagModifier<*> && element.name == name) element.value as T else acc
	}

class ComposedModifierTest {

	/**
	 * Confirm that a [composed] modifier correctly constructs separate instances when materialized
	 */
	@Test fun materializeComposedModifier() = runBlocking(TestFrameClock()) {
		// Note: assumes single-threaded composition
		var counter = 0
		val sourceMod = Modifier.testTag("static", 0).composed { testTag("dynamic", ++counter) }

		withRunningRecomposer { recomposer ->
			lateinit var firstMaterialized: Modifier
			lateinit var secondMaterialized: Modifier
			compose(recomposer) {
				firstMaterialized = currentComposer.materialize(sourceMod)
				secondMaterialized = currentComposer.materialize(sourceMod)
			}

			assertThat(counter, name = "I recomposed some modifiers").isNotEqualTo(0)
			assertThat(
				firstMaterialized.getTestTag("static", Int.MAX_VALUE),
				"first static value equal to source",
			)
				.isEqualTo(sourceMod.getTestTag("static", Int.MIN_VALUE))
			assertThat(
				secondMaterialized.getTestTag("static", Int.MAX_VALUE),
				"second static value equal to source",
			)
				.isEqualTo(sourceMod.getTestTag("static", Int.MIN_VALUE))
			assertThat(
				sourceMod.getTestTag("dynamic", Int.MIN_VALUE),
				"dynamic value not present in source",
			)
				.isEqualTo(Int.MIN_VALUE)
			assertThat(
				firstMaterialized.getTestTag("dynamic", Int.MIN_VALUE),
				"dynamic value present in first materialized",
			)
				.isNotEqualTo(Int.MIN_VALUE)
			assertThat(
				firstMaterialized.getTestTag("dynamic", Int.MIN_VALUE),
				"dynamic value present in second materialized",
			)
				.isNotEqualTo(Int.MIN_VALUE)
			assertThat(
				secondMaterialized.getTestTag("dynamic", Int.MIN_VALUE),
				"first and second dynamic values must be unequal",
			)
				.isNotEqualTo(firstMaterialized.getTestTag("dynamic", Int.MIN_VALUE))
		}
	}

	/** Confirm that recomposition occurs on invalidation */
	@Test fun recomposeComposedModifier() = runBlocking {
		// Manually invalidate the composition of the modifier instead of using mutableStateOf
		// Snapshot-based recomposition requires explicit snapshot commits/global write observers.
		var value = 0
		lateinit var scope: RecomposeScope

		val sourceMod =
			Modifier.composed {
				scope = currentRecomposeScope
				testTag("changing", value)
			}

		val frameClock = TestFrameClock()
		withContext(frameClock) {
			withRunningRecomposer { recomposer ->
				lateinit var materialized: Modifier
				compose(recomposer) { materialized = currentComposer.materialize(sourceMod) }

				assertThat(
					materialized.getTestTag("changing", Int.MIN_VALUE),
					"initial composition value",
				)
					.isEqualTo(0)

				value = 5
				scope.invalidate()
				frameClock.frame(0L)

				assertThat(
					materialized.getTestTag("changing", Int.MIN_VALUE),
					"recomposed composition value",
				)
					.isEqualTo(5)
			}
		}
	}

	@Test fun rememberComposedModifier() = runBlocking {
		lateinit var scope: RecomposeScope
		val sourceMod =
			Modifier.composed {
				scope = currentRecomposeScope
				val state = remember { Any() }
				testTag("remembered", state)
			}

		val frameClock = TestFrameClock()

		withContext(frameClock) {
			withRunningRecomposer { recomposer ->
				val results = mutableListOf<Any?>()
				val notFound = Any()
				compose(recomposer) {
					results.add(
						currentComposer.materialize(sourceMod).getTestTag("remembered", notFound),
					)
				}

				assertThat(results.size, "one item added for initial composition").isEqualTo(1)
				assertThat(results[0], "remembered object not null").isNotNull()

				scope.invalidate()
				frameClock.frame(0)

				assertThat(results.size, "two items added after recomposition").isEqualTo(2)
				assertThat(results, "no null items").containsNone(notFound)
				assertThat(results[1], "remembered references are equal").isEqualTo(results[0])
			}
		}
	}

	@Test fun nestedComposedModifiers() = runBlocking {
		val mod = Modifier.composed { composed { testTag("nested", 10) } }

		val frameClock = TestFrameClock()

		withContext(frameClock) {
			withRunningRecomposer { recomposer ->
				lateinit var materialized: Modifier
				compose(recomposer) { materialized = currentComposer.materialize(mod) }

				assertThat(
					materialized.getTestTag("nested", 0),
					"fully unwrapped composed modifier value",
				).isEqualTo(10)
			}
		}
	}

	@Test fun keyedComposedModifiersAreEqual() {
		val key1 = Any()
		val key2 = Any()
		val key3 = Any()
		val keyN = Array<Any?>(10) { Any() }
		assertThat(Modifier.composed("name", key1) { Modifier })
			.isEqualTo(Modifier.composed("name", key1) { Modifier })
		assertThat(Modifier.composed("name", key1, key2) { Modifier })
			.isEqualTo(Modifier.composed("name", key1, key2) { Modifier })
		assertThat(Modifier.composed("name", key1, key2, key3) { Modifier })
			.isEqualTo(Modifier.composed("name", key1, key2, key3) { Modifier })
		assertThat(Modifier.composed("name", *keyN) { Modifier })
			.isEqualTo(Modifier.composed("name", *keyN) { Modifier })
	}

	@Test fun mismatchedKeyedComposedModifiersAreNotEqual() {
		val key1 = Any()
		val key2 = Any()
		val key3 = Any()
		val keyN = Array<Any?>(10) { Any() }
		assertThat(Modifier.composed("namey mcnameface", key1) { Modifier })
			.isNotEqualTo(Modifier.composed("name", key1) { Modifier })
		assertThat(Modifier.composed("name", key2) { Modifier })
			.isNotEqualTo(Modifier.composed("name", key1) { Modifier })
		assertThat(Modifier.composed("namey mcnameface", key1, key2) { Modifier })
			.isNotEqualTo(Modifier.composed("name", key1, key2) { Modifier })
		assertThat(Modifier.composed("name", Any(), key2) { Modifier })
			.isNotEqualTo(Modifier.composed("name", key1, key2) { Modifier })
		assertThat(Modifier.composed("name", key1, Any()) { Modifier })
			.isNotEqualTo(Modifier.composed("name", key1, key2) { Modifier })
		assertThat(Modifier.composed("namey mcnameface", key1, key2, key3) { Modifier })
			.isNotEqualTo(Modifier.composed("name", key1, key2, key3) { Modifier })
		assertThat(Modifier.composed("name", Any(), key2, key3) { Modifier })
			.isNotEqualTo(Modifier.composed("name", key1, key2, key3) { Modifier })
		assertThat(Modifier.composed("name", key1, Any(), key3) { Modifier })
			.isNotEqualTo(Modifier.composed("name", key1, key2, key3) { Modifier })
		assertThat(Modifier.composed("name", key1, key2, Any()) { Modifier })
			.isNotEqualTo(Modifier.composed("name", key1, key2, key3) { Modifier })
		assertThat(Modifier.composed("namey mcnameface", *keyN) { Modifier })
			.isNotEqualTo(Modifier.composed("name", *keyN) { Modifier })
		repeat(keyN.size) { i ->
			assertThat(
				Modifier.composed(
					"name",
					*(keyN.copyOf().also { it[i] = Any() }),
				) { Modifier },
			)
				.isNotEqualTo(Modifier.composed("name", *keyN) { Modifier })
		}
	}

	@Test fun recomposingKeyedComposedModifierSkips() = runBlocking {
		// Manually invalidate the composition instead of using mutableStateOf
		// Snapshot-based recomposition requires explicit snapshot commits/global write observers.
		lateinit var scope: RecomposeScope

		val frameClock = TestFrameClock()
		withContext(frameClock) {
			withRunningRecomposer { recomposer ->
				var composeCount = 0
				var childComposeCount = 0
				// Use the same lambda instance; the capture used here is unstable
				// and would prevent skipping.
				val increment: (Modifier) -> Unit = { childComposeCount++ }
				val key = Any()

				compose(recomposer) {
					scope = currentRecomposeScope
					SideEffect { composeCount++ }
					ModifiedComposable(Modifier.composed("name", key) { Modifier }, increment)
				}

				assertThat(composeCount, "initial compositions").isEqualTo(1)
				assertThat(childComposeCount, "initial child compositions").isEqualTo(1)

				scope.invalidate()
				frameClock.frame(0L)

				assertThat(composeCount, "recomposed compositions").isEqualTo(2)
				assertThat(childComposeCount, "recomposed child compositions").isEqualTo(1)
			}
		}
	}
}

@Suppress("ktlint:compose:parameter-naming")
@Composable
private fun ModifiedComposable(modifier: Modifier = Modifier, onComposed: (Modifier) -> Unit) {
	SideEffect {
		// Use the modifier parameter so that compiler optimizations don't ignore it
		onComposed(modifier)
	}
}

private fun compose(recomposer: Recomposer, block: @Composable () -> Unit): Composition {
	return Composition(EmptyApplier(), recomposer).apply { setContent(block) }
}

private class TestFrameClock : MonotonicFrameClock {

	private val frameCh = Channel<Long>()

	suspend fun frame(frameTimeNanos: Long) {
		frameCh.send(frameTimeNanos)
	}

	override suspend fun <R> withFrameNanos(onFrame: (Long) -> R): R = onFrame(frameCh.receive())
}

private class EmptyApplier : Applier<Unit> {
	override val current: Unit = Unit

	override fun down(node: Unit) {}

	override fun up() {}

	override fun insertTopDown(index: Int, instance: Unit) {
		error("Unexpected")
	}

	override fun insertBottomUp(index: Int, instance: Unit) {
		error("Unexpected")
	}

	override fun remove(index: Int, count: Int) {
		error("Unexpected")
	}

	override fun move(from: Int, to: Int, count: Int) {
		error("Unexpected")
	}

	override fun clear() {}
}
