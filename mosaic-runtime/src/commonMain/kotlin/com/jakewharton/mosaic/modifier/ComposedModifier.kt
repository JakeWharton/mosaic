package com.jakewharton.mosaic.modifier

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composer
import androidx.compose.runtime.Stable

/**
 * Declare a just-in-time composition of a [Modifier] that will be composed for each element it
 * modifies. [composed] may be used to implement **stateful modifiers** that have instance-specific
 * state for each modified element, allowing the same [Modifier] instance to be safely reused for
 * multiple elements while maintaining element-specific state.
 *
 * [materialize] must be called to create instance-specific modifiers if you are directly applying a
 * [Modifier] to an element tree node.
 */
public fun Modifier.composed(
	factory: @Composable Modifier.() -> Modifier,
): Modifier = this.then(ComposedModifier(factory))

/**
 * Declare a just-in-time composition of a [Modifier] that will be composed for each element it
 * modifies. [composed] may be used to implement **stateful modifiers** that have instance-specific
 * state for each modified element, allowing the same [Modifier] instance to be safely reused for
 * multiple elements while maintaining element-specific state.
 *
 * When keys are provided, [composed] produces a [Modifier] that will compare [equals] to another
 * modifier constructed with the same keys in order to take advantage of caching and skipping
 * optimizations. [fullyQualifiedName] should be the fully-qualified `import` name for your modifier
 * factory function, e.g. `com.example.myapp.ui.fancyPadding`.
 *
 * [materialize] must be called to create instance-specific modifiers if you are directly applying a
 * [Modifier] to an element tree node.
 */
public fun Modifier.composed(
	fullyQualifiedName: String,
	key1: Any?,
	factory: @Composable Modifier.() -> Modifier,
): Modifier = this.then(KeyedComposedModifier1(fullyQualifiedName, key1, factory))

/**
 * Declare a just-in-time composition of a [Modifier] that will be composed for each element it
 * modifies. [composed] may be used to implement **stateful modifiers** that have instance-specific
 * state for each modified element, allowing the same [Modifier] instance to be safely reused for
 * multiple elements while maintaining element-specific state.
 *
 * When keys are provided, [composed] produces a [Modifier] that will compare [equals] to another
 * modifier constructed with the same keys in order to take advantage of caching and skipping
 * optimizations. [fullyQualifiedName] should be the fully-qualified `import` name for your modifier
 * factory function, e.g. `com.example.myapp.ui.fancyPadding`.
 *
 * [materialize] must be called to create instance-specific modifiers if you are directly applying a
 * [Modifier] to an element tree node.
 */
public fun Modifier.composed(
	fullyQualifiedName: String,
	key1: Any?,
	key2: Any?,
	factory: @Composable Modifier.() -> Modifier,
): Modifier =
	this.then(KeyedComposedModifier2(fullyQualifiedName, key1, key2, factory))

/**
 * Declare a just-in-time composition of a [Modifier] that will be composed for each element it
 * modifies. [composed] may be used to implement **stateful modifiers** that have instance-specific
 * state for each modified element, allowing the same [Modifier] instance to be safely reused for
 * multiple elements while maintaining element-specific state.
 *
 * When keys are provided, [composed] produces a [Modifier] that will compare [equals] to another
 * modifier constructed with the same keys in order to take advantage of caching and skipping
 * optimizations. [fullyQualifiedName] should be the fully-qualified `import` name for your modifier
 * factory function, e.g. `com.example.myapp.ui.fancyPadding`.
 *
 * [materialize] must be called to create instance-specific modifiers if you are directly applying a
 * [Modifier] to an element tree node.
 */
public fun Modifier.composed(
	fullyQualifiedName: String,
	key1: Any?,
	key2: Any?,
	key3: Any?,
	factory: @Composable Modifier.() -> Modifier,
): Modifier =
	this.then(KeyedComposedModifier3(fullyQualifiedName, key1, key2, key3, factory))

/**
 * Declare a just-in-time composition of a [Modifier] that will be composed for each element it
 * modifies. [composed] may be used to implement **stateful modifiers** that have instance-specific
 * state for each modified element, allowing the same [Modifier] instance to be safely reused for
 * multiple elements while maintaining element-specific state.
 *
 * When keys are provided, [composed] produces a [Modifier] that will compare [equals] to another
 * modifier constructed with the same keys in order to take advantage of caching and skipping
 * optimizations. [fullyQualifiedName] should be the fully-qualified `import` name for your modifier
 * factory function, e.g. `com.example.myapp.ui.fancyPadding`.
 *
 * [materialize] must be called to create instance-specific modifiers if you are directly applying a
 * [Modifier] to an element tree node.
 */
public fun Modifier.composed(
	fullyQualifiedName: String,
	vararg keys: Any?,
	factory: @Composable Modifier.() -> Modifier,
): Modifier = this.then(KeyedComposedModifierN(fullyQualifiedName, keys, factory))

private open class ComposedModifier(
	val factory: @Composable Modifier.() -> Modifier,
) : Modifier.Element

@Stable
private class KeyedComposedModifier1(
	val fqName: String,
	val key1: Any?,
	factory: @Composable Modifier.() -> Modifier,
) : ComposedModifier(factory) {
	override fun equals(other: Any?) =
		other is KeyedComposedModifier1 && fqName == other.fqName && key1 == other.key1

	override fun hashCode(): Int = 31 * fqName.hashCode() + key1.hashCode()
}

@Stable
private class KeyedComposedModifier2(
	val fqName: String,
	val key1: Any?,
	val key2: Any?,
	factory: @Composable Modifier.() -> Modifier,
) : ComposedModifier(factory) {
	override fun equals(other: Any?) =
		other is KeyedComposedModifier2 &&
			fqName == other.fqName &&
			key1 == other.key1 &&
			key2 == other.key2

	override fun hashCode(): Int {
		var result = fqName.hashCode()
		result = 31 * result + key1.hashCode()
		result = 31 * result + key2.hashCode()
		return result
	}
}

@Stable
private class KeyedComposedModifier3(
	val fqName: String,
	val key1: Any?,
	val key2: Any?,
	val key3: Any?,
	factory: @Composable Modifier.() -> Modifier,
) : ComposedModifier(factory) {
	override fun equals(other: Any?) =
		other is KeyedComposedModifier3 &&
			fqName == other.fqName &&
			key1 == other.key1 &&
			key2 == other.key2 &&
			key3 == other.key3

	override fun hashCode(): Int {
		var result = fqName.hashCode()
		result = 31 * result + key1.hashCode()
		result = 31 * result + key2.hashCode()
		result = 31 * result + key3.hashCode()
		return result
	}
}

@Stable
private class KeyedComposedModifierN(
	val fqName: String,
	val keys: Array<out Any?>,
	factory: @Composable Modifier.() -> Modifier,
) : ComposedModifier(factory) {
	override fun equals(other: Any?) =
		other is KeyedComposedModifierN && fqName == other.fqName && keys.contentEquals(other.keys)

	override fun hashCode() = 31 * fqName.hashCode() + keys.contentHashCode()
}

/**
 * Materialize any instance-specific [composed modifiers][composed] for applying to a raw tree node.
 * Call right before setting the returned modifier on an emitted node. You almost certainly do not
 * need to call this function directly.
 */
public fun Composer.materialize(modifier: Modifier): Modifier {
	// A group is required here so the number of slot added to the caller's group
	// is unconditionally the same (in this case, none) as is now required by the runtime.
	startReplaceGroup(0x1a365f2c) // Random number for fake group key. Chosen by fair die roll.
	val result = materializeImpl(modifier)
	endReplaceGroup()
	return result
}

private fun Composer.materializeImpl(modifier: Modifier): Modifier {
	if (modifier.all { it !is ComposedModifier }) {
		return modifier
	}

	// This is a fake composable function that invokes the compose runtime directly so that it
	// can call the element factory functions from the non-@Composable lambda of Modifier.foldIn.
	// It would be more efficient to redefine the Modifier type hierarchy such that the fold
	// operations could be inlined or otherwise made cheaper, which could make this unnecessary.

	// Random number for fake group key. Chosen by fair die roll.
	startReplaceableGroup(0x48ae8da7)

	val result = modifier.foldIn<Modifier>(Modifier) { acc, element ->
		acc.then(
			if (element is ComposedModifier) {
				@Suppress("UNCHECKED_CAST")
				val factory = element.factory as Modifier.(Composer, Int) -> Modifier
				val composedMod = factory(Modifier, this, 0)
				materializeImpl(composedMod)
			} else {
				element
			},
		)
	}

	endReplaceableGroup()
	return result
}
