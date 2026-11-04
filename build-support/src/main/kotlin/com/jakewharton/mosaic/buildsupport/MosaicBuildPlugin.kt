package com.jakewharton.mosaic.buildsupport

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.STARTED

@Suppress("unused") // Invoked reflectively by Gradle.
public class MosaicBuildPlugin : Plugin<Project> {
	override fun apply(target: Project) {
		target.extensions.add(
			MosaicBuildExtension::class.java,
			"mosaicBuild",
			MosaicBuildExtensionImpl(target),
		)

		target.tasks.withType(Test::class.java).configureEach {
			it.useJUnitPlatform()
			it.testLogging {
				it.events(STARTED, PASSED, FAILED)
			}
		}
	}
}
