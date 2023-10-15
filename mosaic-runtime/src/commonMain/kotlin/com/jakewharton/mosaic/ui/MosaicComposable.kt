package com.jakewharton.mosaic.ui

import androidx.compose.runtime.ComposableTargetMarker

/**
 * An annotation that can be used to mark an composable function as being expected to be use in a
 * composable function that is also marked or inferred to be marked as a [MosaicComposable].
 *
 * Using this annotation explicitly is rarely necessary as the Compose compiler plugin will infer
 * the necessary equivalent annotations automatically. See
 * [androidx.compose.runtime.ComposableTarget] for details.
 */
@Retention(AnnotationRetention.BINARY)
@ComposableTargetMarker(description = "Mosaic Composable")
@Target(
	AnnotationTarget.FUNCTION,
	AnnotationTarget.PROPERTY_GETTER,
	AnnotationTarget.TYPE,
	AnnotationTarget.TYPE_PARAMETER,
)
public annotation class MosaicComposable
