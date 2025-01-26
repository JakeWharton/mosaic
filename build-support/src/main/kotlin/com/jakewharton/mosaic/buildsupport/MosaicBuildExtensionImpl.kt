package com.jakewharton.mosaic.buildsupport

import java.io.File
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.api.internal.tasks.testing.TestClassProcessor
import org.gradle.api.internal.tasks.testing.TestClassRunInfo
import org.gradle.api.internal.tasks.testing.TestResultProcessor
import org.gradle.api.internal.tasks.testing.detection.ClassFileExtractionManager
import org.gradle.api.internal.tasks.testing.detection.DefaultTestClassScanner
import org.gradle.api.internal.tasks.testing.junit.JUnitDetector
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.application.CreateStartScripts
import org.gradle.api.tasks.bundling.Zip
import org.gradle.internal.Factory
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

internal class MosaicBuildExtensionImpl(
	private val project: Project,
) : MosaicBuildExtension {
	override fun jvmTestDistribution() {
		var gotMpp = false
		project.afterEvaluate {
			check(gotMpp) {
				"JVM test distribution requires the Kotlin multiplatform plugin"
			}
		}
		project.pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
			gotMpp = true

			val gradleSupport: GradleSupport = Gradle_8_10_Support()

			val installDistributions = project.tasks.register("installTestDistributions") {
				it.group = "distribution"
				it.description = "Installs all test distributions."
			}
			val zipDistributions = project.tasks.register("zipTestDistributions") {
				it.group = "distribution"
				it.description = "Bundles all test distributions."
			}

			val base = project.extensions.getByType(BasePluginExtension::class.java)
			val kotlin = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
			kotlin.targets.withType(KotlinJvmTarget::class.java) { target ->
				target.testRuns.configureEach { testRun ->
					val name = testRun.name
					val nameUpper = name.replaceFirstChar(Char::uppercase)

//					val testJarProvider = project.tasks.register("jarTest$nameUpper", Jar::class.java) {
//						it.from(testClassesDirs)
//						it.archiveAppendix.set(target.name)
//						it.archiveClassifier.set(if (name == "test") "tests" else "tests$nameUpper")
//					}

					val testScriptsProvider = project.tasks.register("scriptsTest$nameUpper", CreateStartScripts::class.java) { task ->
						task.outputDir = project.layout.buildDirectory.dir("scripts/$name").get().asFile
						task.applicationName = base.archivesName.get() + "-test"

						val executionSource = testRun.executionSource
						val testClasspath = executionSource.classpath
						val testClassesDirs = executionSource.testClassesDirs

						// The classpath property is not lazy, so we need explicit dependencies here.
						task.dependsOn(testClasspath)
//						it.dependsOn(testJarProvider)

						// However, this 'plus' result will be live, and can still be set at configuration time.
//						val classpath = testJarProvider.get().outputs.files
//							.plus(testClasspath)
						task.doFirst {
							task.classpath = testClasspath
						}

						task.mainClass.set(
							project.provider {
								println("XXXXX $name\n  ${testClasspath.files}\n  ${testClassesDirs.files}")
								val testFqcns = gradleSupport.detectTestClassNames(
									testClassesDirs.asFileTree,
									testClassesDirs.files.toList(),
									testClasspath.files.toList()
								)
								"org.junit.runner.JUnitCore ${testFqcns.joinToString(" ") { """"$it"""" }}"
							}
						)
					}

					val installProvider = project.tasks.register("installTest${nameUpper}Distribution", Copy::class.java) {
						it.group = "distribution"
						it.description = "Installs test $name as a distribution as-is."

						it.dependsOn(testRun.executionTask.get().inputs.files)
						val classpath = testRun.executionTask.get().classpath

						it.into("bin") {
							it.from(testScriptsProvider)
						}
						it.into("lib") {
//							it.from(mainJarProvider)
//							it.from(testJarProvider)
							println("YYYYY $name\n  $classpath")
							it.from(classpath)
						}
						it.destinationDir = project.layout.buildDirectory.dir("tests-install/$name").get().asFile
					}
					installDistributions.configure {
						it.dependsOn(installProvider)
					}

					val zipProvider = project.tasks.register("zipTest${nameUpper}Distribution", Zip::class.java) {
						it.group = "distribution"
						it.description = "Bundles test $name as a distribution."

						it.from(installProvider)
						it.destinationDirectory.set(project.layout.buildDirectory.dir("tests-distribution"))
						it.archiveAppendix.set(target.name)
						it.archiveClassifier.set(if (name == "test") "tests" else "tests$nameUpper")
					}
					zipDistributions.configure {
						it.dependsOn(zipProvider)
					}
				}
			}
		}
	}

	interface GradleSupport {
		fun detectTestClassNames(
			testClasses: FileTree,
			testClassDirectories: List<File>,
			testClasspath: List<File>,
		): List<String>
	}

	class Gradle_8_10_Support : GradleSupport {
		override fun detectTestClassNames(
			testClasses: FileTree,
			testClassDirectories: List<File>,
			testClasspath: List<File>,
		): List<String> {
			val detector = JUnitDetector(ClassFileExtractionManager(object : Factory<File> {
				override fun create() = File.createTempFile("gradle", "test-class-detection").apply {
					deleteOnExit()
				}
			}))
			detector.setTestClasses(testClassDirectories)
			detector.setTestClasspath(testClasspath)

			val testFqcns = mutableListOf<String>()
			val testClassProcessor = object : TestClassProcessor {
				override fun processTestClass(testClass: TestClassRunInfo) {
					testFqcns += testClass.testClassName
				}
				override fun startProcessing(resultProcessor: TestResultProcessor) {}
				override fun stop() {}
				override fun stopNow() {}
			}

			DefaultTestClassScanner(testClasses, detector, testClassProcessor).run()

			return testFqcns
		}
	}
}
