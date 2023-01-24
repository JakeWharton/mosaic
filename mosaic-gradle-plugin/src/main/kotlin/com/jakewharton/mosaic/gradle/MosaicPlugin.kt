package com.jakewharton.mosaic.gradle

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin.API_CONFIGURATION_NAME
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet.Companion.COMMON_MAIN_SOURCE_SET_NAME
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

class MosaicPlugin : KotlinCompilerPluginSupportPlugin {
	override fun isApplicable(kotlinCompilation: KotlinCompilation<*>) = true

	override fun getCompilerPluginId() = "com.jakewharton.mosaic"

	override fun getPluginArtifact() = SubpluginArtifact(
		"androidx.compose.compiler",
		"compiler",
		composeCompilerVersion,
	)

	override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
		return kotlinCompilation.target.project.provider { emptyList() }
	}

	override fun apply(target: Project) {
		super.apply(target)

		if (target.isInternal() && target.path == ":mosaic-runtime") {
			// Being lazy and using our own plugin to configure the Compose compiler on our runtime.
			// Bail out because otherwise we create a circular dependency reference on ourselves!
			return
		}

		target.afterEvaluate {
			val multiplatform = target.extensions.findByType(KotlinMultiplatformExtension::class.java)
			val jvm = target.extensions.findByType(KotlinJvmProjectExtension::class.java)

			val dependency: Any = if (target.isInternal()) {
				target.dependencies.project(mapOf("path" to ":mosaic-runtime"))
			} else {
				"com.jakewharton.mosaic:mosaic-runtime:$mosaicVersion"
			}

			if (jvm != null) {
				target.dependencies.add(API_CONFIGURATION_NAME, dependency)
			} else if (multiplatform != null) {
				multiplatform.sourceSets.getByName(COMMON_MAIN_SOURCE_SET_NAME) { sourceSet ->
					sourceSet.dependencies {
						api(dependency)
					}
				}
			} else {
				throw IllegalStateException("Kotlin/JVM or Kotlin/Multiplatform plugin must be applied.")
			}
		}
	}

	private fun Project.isInternal(): Boolean {
		return properties["com.jakewharton.mosaic.internal"].toString() == "true"
	}
}
