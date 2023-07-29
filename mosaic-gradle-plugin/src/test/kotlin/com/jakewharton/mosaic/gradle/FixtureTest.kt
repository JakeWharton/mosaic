package com.jakewharton.mosaic.gradle

import com.google.common.truth.Truth.assertThat
import java.io.File
import org.gradle.testkit.runner.GradleRunner
import org.junit.Test

class FixtureTest {
	@Test fun counter() {
		fixture("counter").build()
	}

	@Test fun customCompilerCoordinates() {
		fixture("custom-compiler-coordinates").build()
	}

	@Test fun customCompilerInvalid() {
		val result = fixture("custom-compiler-invalid").buildAndFail()
		assertThat(result.output).contains(
			"""
			|Illegal format of 'mosaic.kotlinCompilerPlugin' property.
			|Expected format: either '<VERSION>' or '<GROUP_ID>:<ARTIFACT_ID>:<VERSION>'
			|Actual value: 'wrong:format'
			""".trimMargin(),
		)
	}

	@Test fun customCompilerVersion() {
		fixture("custom-compiler-version").build()
	}

	private fun fixture(
		name: String,
		tasks: Array<String> = arrayOf("clean", "build"),
	): GradleRunner {
		val fixtureDir = File("src/test/fixture", name)
		val gradleWrapper = File(fixtureDir, "gradle/wrapper").also { it.mkdirs() }
		File("../gradle/wrapper").copyRecursively(File(gradleWrapper.path), true)

		return GradleRunner.create()
			.withProjectDir(fixtureDir)
			.withArguments(*tasks, "--stacktrace", "-PmosaicVersion=$mosaicVersion")
			.withDebug(true)
	}
}
