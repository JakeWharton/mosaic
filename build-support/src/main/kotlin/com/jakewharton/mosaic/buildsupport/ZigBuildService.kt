package com.jakewharton.mosaic.buildsupport

import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.FileSystemException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption.ATOMIC_MOVE
import java.util.Locale.US
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters

/**
 * Supplies a Zig executable, downloading the release on first use. Releases are installed outside
 * of any build directory so that `clean` does not force a re-download, and so that other builds
 * of any project can reuse the same copy.
 *
 * A release directory only ever appears complete, because it is renamed into place in one step.
 * Nothing watches it afterwards, though, so damage from outside the build is not repaired: delete
 * the directory under [Parameters.installDir] to force a fresh download.
 */
public abstract class ZigBuildService : BuildService<ZigBuildService.Parameters> {
	public interface Parameters : BuildServiceParameters {
		/** Zig release to use, such as `0.15.1`. */
		public val version: Property<String>

		/** Directory into which releases are installed. Must be outside of any build directory. */
		public val installDir: DirectoryProperty
	}

	private val osName = System.getProperty("os.name").lowercase(US)
	private val os = when {
		"windows" in osName -> "windows"
		"linux" in osName -> "linux"
		"mac" in osName -> "macos"
		else -> throw IllegalStateException("Unsupported OS: $osName")
	}
	private val isWindows = os == "windows"
	private val arch = when (val arch = System.getProperty("os.arch").lowercase(US)) {
		"amd64" -> "x86_64"
		else -> arch
	}
	private val executableName = if (isWindows) "zig.exe" else "zig"
	private val archiveExtension = if (isWindows) "zip" else "tar.xz"

	/** Reading this installs the release if it is not already present, which takes about a minute. */
	public val zigExecutable: File by lazy { install().toFile() }

	private fun install(): Path {
		val name = "zig-$arch-$os-${parameters.version.get()}"
		val installDir = parameters.installDir.get().asFile.toPath()
		val releaseDir = installDir.resolve(name)
		val executable = releaseDir.resolve(executableName)
		if (executable.exists()) return executable

		Files.createDirectories(installDir)
		// Install into a sibling directory which is renamed into place only once complete, so that a
		// partial release never becomes the one that later builds find and use. A build which is
		// killed outright leaves its sibling behind, which wastes space but is never used.
		val pendingDir = Files.createTempDirectory(installDir, "$name-")
		try {
			val archiveFile = tempFile()
			try {
				download("$name.$archiveExtension", archiveFile)
				extract(archiveFile, pendingDir.toFile())
			} finally {
				archiveFile.deleteIfExists()
			}
			pendingDir.resolve(executableName).toFile().setExecutable(true)

			// Ask for an atomic move so that a build running alongside this one cannot see a
			// half-populated release, and fall back for the filesystems which refuse the option.
			try {
				try {
					Files.move(pendingDir, releaseDir, ATOMIC_MOVE)
				} catch (e: AtomicMoveNotSupportedException) {
					Files.move(pendingDir, releaseDir)
				}
			} catch (e: FileSystemException) {
				// A release already sitting there means another build finished this one first, which
				// is no reason to fail. Which exception says so varies with the filesystem and with
				// whether the move was atomic, so go by the state left behind rather than the type.
				if (!executable.exists()) throw e
			}
		} finally {
			pendingDir.toFile().deleteRecursively()
		}

		// The move above tolerates losing a race, but only because the winner leaves a release
		// behind. Anything else which swallowed the move has to surface here rather than later.
		check(executable.exists()) {
			"Zig ${parameters.version.get()} did not install to $executable"
		}
		return executable
	}

	private fun download(archiveName: String, into: Path) {
		val link = "https://ziglang.org/download/${parameters.version.get()}/$archiveName"

		val response = HttpClient.newHttpClient().use { client ->
			client.send(
				HttpRequest.newBuilder()
					.GET()
					.uri(URI(link))
					.build(),
				HttpResponse.BodyHandlers.ofFile(into),
			)
		}
		check(response.statusCode() == 200) {
			"HTTP ${response.statusCode()}: ${response.uri()}"
		}
	}

	private fun extract(archiveFile: Path, into: File) {
		val tarFile = if (isWindows) null else tempFile()
		try {
			val archive = if (tarFile == null) {
				ZipArchiveInputStream(archiveFile.inputStream().buffered())
			} else {
				XZCompressorInputStream(archiveFile.inputStream().buffered()).use { xz ->
					tarFile.outputStream().buffered().use(xz::copyTo)
				}
				TarArchiveInputStream(tarFile.inputStream().buffered())
			}
			archive.use { archive ->
				for (entry in generateSequence { archive.nextEntry }.filterNot { it.isDirectory }) {
					val name = entry.name.substringAfter('/')
					into.resolve(name)
						.also { it.parentFile.mkdirs() }
						.outputStream()
						.buffered()
						.use(archive::copyTo)
				}
			}
		} finally {
			tarFile?.deleteIfExists()
		}
	}

	private fun tempFile(): Path {
		return createTempFile("${ZigBuildService::class.java.simpleName}-${parameters.version.get()}-")
	}
}
