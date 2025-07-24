package com.jakewharton.mosaic.buildsupport

import org.gradle.api.Project
import org.gradle.api.tasks.CompileClasspath
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.process.CommandLineArgumentProvider
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation.Companion.MAIN_COMPILATION_NAME
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal class MosaicBuildExtensionImpl(
	private val project: Project,
) : MosaicBuildExtension {
	override fun patchJavaModuleWithKotlinClasses(name: String) {
		var gotMpp = false
		project.afterEvaluate {
			check(gotMpp) {
				"JVM test distribution requires the Kotlin multiplatform plugin"
			}
		}
		project.pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
			gotMpp = true

			val kotlin = project.extensions.getByType(KotlinMultiplatformExtension::class.java)

			kotlin.targets.withType(KotlinJvmTarget::class.java).configureEach { target ->
				target.compilations.named(MAIN_COMPILATION_NAME).configure { main ->
					main.compileJavaTaskProvider!!.configure { javaCompile: JavaCompile ->
						val kotlinCompile = project.tasks.getByName(main.compileKotlinTaskName) as KotlinCompile
						javaCompile.options.compilerArgumentProviders.add(object : CommandLineArgumentProvider {
							@CompileClasspath
							val classes = kotlinCompile.destinationDirectory

							override fun asArguments(): Iterable<String> {
								return listOf(
									"--patch-module",
									"$name=${classes.get().asFile.absolutePath}"
								)
							}
						})
					}
				}
			}
		}
	}
}
