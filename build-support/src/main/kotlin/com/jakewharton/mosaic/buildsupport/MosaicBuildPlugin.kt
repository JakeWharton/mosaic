package com.jakewharton.mosaic.buildsupport

import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused") // Invoked reflectively by Gradle.
public class MosaicBuildPlugin : Plugin<Project> {
	override fun apply(target: Project) {
		target.extensions.add(
			MosaicBuildExtension::class.java,
			"mosaicBuild",
			MosaicBuildExtensionImpl(target),
		)
	}
}

