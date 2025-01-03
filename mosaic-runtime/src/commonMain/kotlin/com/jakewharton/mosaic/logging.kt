package com.jakewharton.mosaic

import com.github.ajalt.mordant.platform.MultiplatformSystem
import kotlin.jvm.JvmField

private const val MOSAIC_LOG_PATH = "MOSAIC_LOG_PATH"
private const val MOSAIC_LOG_LEVEL = "MOSAIC_LOG_LEVEL"

public enum class MosaicLogLevel(internal val envVarValue: String) {
	VERBOSE(envVarValue = "verbose"),
	DEBUG(envVarValue = "debug"),
}

public object MosaicLogger {

	@JvmField
	@PublishedApi
	internal var logWriter: MosaicLogWriter? = null

	@JvmField
	@PublishedApi
	internal var logLevel: MosaicLogLevel = MosaicLogLevel.DEBUG

	internal fun init() {
		logWriter = getDefaultLogWriter()
		logLevel = getDefaultLogLevel()
	}

	private fun getDefaultLogWriter(): MosaicLogWriter? {
		val logPath = MultiplatformSystem.readEnvironmentVariable(MOSAIC_LOG_PATH)
		if (logPath.isNullOrEmpty()) return null
		return createDefaultLogWriter(logPath)
	}

	private fun getDefaultLogLevel(): MosaicLogLevel {
		val logLevelValue = MultiplatformSystem.readEnvironmentVariable(MOSAIC_LOG_LEVEL)
		return MosaicLogLevel.entries.find { it.envVarValue == logLevelValue }
			?: MosaicLogLevel.DEBUG
	}

	internal fun finalize() {
		logWriter?.let {
			logWriter = null
			it.close()
		}
	}

	public inline fun log(level: MosaicLogLevel, message: () -> String) {
		val logWriter = logWriter
		if (logWriter != null && level >= logLevel) {
			logWriter.writeLine(message())
		}
	}

	public inline fun v(message: () -> String): Unit = log(MosaicLogLevel.VERBOSE, message)

	public inline fun d(message: () -> String): Unit = log(MosaicLogLevel.DEBUG, message)
}

@PublishedApi
internal interface MosaicLogWriter {
	fun writeLine(message: String)
	fun close()
}

internal expect fun createDefaultLogWriter(logPath: String): MosaicLogWriter?
