package com.jakewharton.mosaic.buildsupport

import java.io.File
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.api.internal.file.FileCollectionFactory
import org.gradle.api.internal.tasks.testing.TestClassProcessor
import org.gradle.api.internal.tasks.testing.TestClassRunInfo
import org.gradle.api.internal.tasks.testing.TestResultProcessor
import org.gradle.api.internal.tasks.testing.detection.ClassFileExtractionManager
import org.gradle.api.internal.tasks.testing.detection.DefaultTestClassScanner
import org.gradle.api.internal.tasks.testing.junit.JUnitDetector
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.application.CreateStartScripts
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Zip
import org.gradle.internal.Factory
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation.Companion.TEST_COMPILATION_NAME
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

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

			val base = project.extensions.getByType(BasePluginExtension::class.java)
			val kotlin = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
			kotlin.targets.configureEach { target ->
				if (target.platformType != KotlinPlatformType.jvm) return@configureEach

				val name = target.name + "Test"
				val nameUpper = name.replaceFirstChar(Char::uppercase)

				val mainJarProvider = project.tasks.named(target.artifactsTaskName)

				val testCompilation = target.compilations.named(TEST_COMPILATION_NAME)
				val testClassesProvider = testCompilation.map { it.output.allOutputs }
				val testDependenciesProvider = testCompilation.map {
					it.runtimeDependencyFiles?.filter { it.isFile }
						?: FileCollectionFactory.empty()
				}

				val testJarProvider = project.tasks.register("jar$nameUpper", Jar::class.java) {
					it.from(testClassesProvider)
					it.archiveAppendix.set(target.name)
					it.archiveClassifier.set("tests")
				}

				val testScriptsProvider = project.tasks.register("scripts$nameUpper", CreateStartScripts::class.java) {
					it.outputDir = project.layout.buildDirectory.dir("scripts/$name").get().asFile
					it.applicationName = base.archivesName.get() + "-test"

					// The classpath property is not lazy, so we need explicit dependencies here.
					it.dependsOn(mainJarProvider)
					it.dependsOn(testJarProvider)
					it.dependsOn(testDependenciesProvider)
					// However, this 'plus' result will be live, and can still be set at configuration time.
					val classpath = mainJarProvider.get().outputs.files
						.plus(testJarProvider.get().outputs.files)
						.plus(testDependenciesProvider.get())
					it.classpath = classpath

					it.mainClass.set(
						testClassesProvider.zip(testDependenciesProvider) { testClasses, testDependencies ->
							val testFqcns = gradleSupport.detectTestClassNames(
								testClasses.asFileTree,
								testClasses.files.toList(),
								testDependencies.files.toList()
							)
							"org.junit.runner.JUnitCore ${testFqcns.joinToString(" ") { """"$it"""" }}"
						}
					)
				}

				val installProvider = project.tasks.register("install${nameUpper}Distribution", Copy::class.java) {
					it.group = "distribution"
					it.description = "Installs $name as a distribution as-is."

					it.into("bin") {
						it.from(testScriptsProvider)
					}
					it.into("lib") {
						it.from(testJarProvider)
						it.from(mainJarProvider)
						it.from(testDependenciesProvider)
					}
					it.destinationDir = project.layout.buildDirectory.dir("install/$name").get().asFile
				}

				project.tasks.register("zip${nameUpper}Distribution", Zip::class.java) {
					it.group = "distribution"
					it.description = "Bundles $name as a distribution."

					it.from(installProvider)
					it.destinationDirectory.set(project.layout.buildDirectory.dir("dist"))
					it.archiveAppendix.set(target.name)
					it.archiveClassifier.set("tests")
				}

				// TODO add to archives?
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
