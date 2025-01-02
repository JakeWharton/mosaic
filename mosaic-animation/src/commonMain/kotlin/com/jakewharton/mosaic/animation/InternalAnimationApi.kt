package com.jakewharton.mosaic.animation

@RequiresOptIn(message = "This API is internal to library.")
@Target(
	AnnotationTarget.CLASS,
	AnnotationTarget.FUNCTION,
	AnnotationTarget.PROPERTY,
	AnnotationTarget.FIELD,
	AnnotationTarget.PROPERTY_GETTER,
)
@Retention(AnnotationRetention.BINARY)
public annotation class InternalAnimationApi
