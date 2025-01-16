package com.jakewharton.mosaic.terminal

import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.PROPERTY

@RequiresOptIn
@Target(CLASS, FUNCTION, PROPERTY)
internal annotation class TestApi
