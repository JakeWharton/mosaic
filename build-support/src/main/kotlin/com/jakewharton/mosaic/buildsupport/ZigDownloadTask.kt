package com.jakewharton.mosaic.buildsupport

import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Path
import java.util.Locale.US
import kotlin.io.path.createTempFile
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

public abstract class ZigDownloadTask : DefaultTask() {
	private val osName = System.getProperty("os.name").lowercase(US)
	private val os = when {
		"windows" in osName -> "windows"
		"linux" in osName -> "linux"
		"mac" in osName -> "macos"
		else -> throw IllegalStateException("Unsupported OS: $osName")
	}
	private val isWindows = os == "windows"

	@get:Input
	public abstract val version: Property<String>

	@get:OutputDirectory
	public abstract val outputDir: DirectoryProperty

	@get:Internal
	public val zigExecutable: File get() = outputDir.get().file(if (isWindows) "zig.exe" else "zig").asFile

	@TaskAction
	public fun run() {
		val arch = when (val arch = System.getProperty("os.arch").lowercase(US)) {
			"amd64" -> "x86_64"
			else -> arch
		}

		val ext = if (isWindows) "zip" else "tar.xz"
		val link = "https://ziglang.org/download/${version.get()}/zig-$arch-$os-${version.get()}.$ext"

		val archiveFile = tempFile()
		val response = HttpClient.newHttpClient().use { client ->
			client.send(
				HttpRequest.newBuilder()
					.GET()
					.uri(URI(link))
					.build(),
				HttpResponse.BodyHandlers.ofFile(archiveFile),
			)
		}
		check(response.statusCode() == 200) {
			"HTTP ${response.statusCode()}: ${response.uri()}"
		}

		val outputDir = outputDir.get().asFile
		outputDir.deleteRecursively()

		val archive = if (isWindows) {
			ZipArchiveInputStream(archiveFile.inputStream().buffered())
		} else {
			val tarFile = tempFile()
			XZCompressorInputStream(archiveFile.inputStream().buffered()).use { xz ->
				tarFile.outputStream().buffered().use(xz::copyTo)
			}
			TarArchiveInputStream(tarFile.inputStream().buffered())
		}
		archive.use { archive ->
			for (entry in generateSequence { archive.nextEntry }.filterNot { it.isDirectory }) {
				val name = entry.name.substringAfter('/')
				outputDir.resolve(name)
					.also { it.parentFile.mkdirs() }
					.outputStream()
					.buffered()
					.use(archive::copyTo)
			}
		}

		zigExecutable.setExecutable(true)
	}

	private fun tempFile(): Path {
		return createTempFile("${ZigDownloadTask::class.java.simpleName}-${version.get()}-").also {
			it.toFile().deleteOnExit()
		}
	}

}
