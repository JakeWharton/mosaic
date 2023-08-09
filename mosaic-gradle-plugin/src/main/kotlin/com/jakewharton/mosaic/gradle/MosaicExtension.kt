package com.jakewharton.mosaic.gradle

import org.gradle.api.provider.Property

interface MosaicExtension {
	/**
	 * The version of the JetBrains Compose compiler to use, or a Maven coordinate triple of
	 * the custom Compose compiler to use.
	 *
	 * Example: using a custom version of the JetBrains Compose compiler
	 * ```kotlin
	 * mosaic {
	 *   kotlinCompilerPlugin.set("1.4.8")
	 * }
	 * ```
	 *
	 * Example: using a custom Maven coordinate for the Compose compiler
	 * ```kotlin
	 * mosaic {
	 *   kotlinCompilerPlugin.set("com.example:custom-compose-compiler:1.0.0")
	 * }
	 * ```
	 */
	val kotlinCompilerPlugin: Property<String>
}
