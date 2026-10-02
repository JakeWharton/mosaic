package com.jakewharton.mosaic.buildsupport

import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations

/** Runs `zig build` with the Zig release supplied by [ZigBuildService]. */
public abstract class ZigBuildTask : DefaultTask() {
	@get:Internal
	public abstract val zig: Property<ZigBuildService>

	/** Zig release used, so that changing it rebuilds rather than reporting up-to-date. */
	@get:Input
	public abstract val zigVersion: Property<String>

	@get:Internal
	public abstract val workingDir: DirectoryProperty

	@get:Input
	public abstract val args: ListProperty<String>

	@get:Inject
	protected abstract val execOperations: ExecOperations

	@TaskAction
	public fun run() {
		execOperations.exec { spec ->
			spec.executable(zig.get().zigExecutable)
			spec.workingDir(workingDir.get().asFile)
			spec.args(args.get())
		}
	}
}
