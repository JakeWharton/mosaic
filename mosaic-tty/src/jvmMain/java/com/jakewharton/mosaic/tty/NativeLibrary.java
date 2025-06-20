package com.jakewharton.mosaic.tty;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Locale.US;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

final class NativeLibrary {
	private static final AtomicBoolean loaded = new AtomicBoolean();

	static Path ensureLoaded() {
		if (loaded.compareAndSet(false, true)) {
			return loadEmbeddedNativeLibrary("jni", "mosaic");
		}
		return null;
	}

	@SuppressWarnings("SameParameterValue") // Preserving copy/paste!
	private static Path loadEmbeddedNativeLibrary(String relativePath, String library) {
		String osName = System.getProperty("os.name").toLowerCase(US);
		String osArch = System.getProperty("os.arch").toLowerCase(US);
		StringBuilder nativeLibraryJarPathBuilder = new StringBuilder(50)
			.append(relativePath)
			.append('/')
			.append(osArch)
			.append('/');
		if (osName.contains("linux")) {
			nativeLibraryJarPathBuilder.append("lib")
				.append(library)
				.append(".so");
		} else if (osName.contains("mac")) {
			nativeLibraryJarPathBuilder.append("lib")
				.append(library)
				.append(".dylib");
		} else if (osName.contains("windows")) {
			nativeLibraryJarPathBuilder.append(library)
				.append(".dll");
		} else {
			throw new IllegalStateException("Unsupported OS: " + osName + ' ' + osArch);
		}
		String nativeLibraryJarPath = nativeLibraryJarPathBuilder.toString();
		URL nativeLibraryUrl = Jni.class.getResource(nativeLibraryJarPath);
		if (nativeLibraryUrl == null) {
			throw new IllegalStateException("Unable to read ".concat(nativeLibraryJarPath));
		}

		Path nativeLibraryFile;
		try {
			nativeLibraryFile = Files.createTempFile(library, null);

			// File-based deleteOnExit() uses a special internal shutdown hook that always runs last.
			nativeLibraryFile.toFile().deleteOnExit();
			try (InputStream nativeLibrary = nativeLibraryUrl.openStream()) {
				Files.copy(nativeLibrary, nativeLibraryFile, REPLACE_EXISTING);
			}
		} catch (IOException e) {
			throw new UncheckedIOException("Unable to extract native library from JAR", e);
		}
		System.load(nativeLibraryFile.toAbsolutePath().toString());

		return nativeLibraryFile;
	}

	private NativeLibrary() {}
}
