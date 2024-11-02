const std = @import("std");

pub fn build(b: *std.Build) !void {
	// The Windows builds create a .lib file in the lib/ directory which we don't need.
	const deleteLib = b.addRemoveDirTree(b.getInstallPath(.prefix, "lib"));
	b.getInstallStep().dependOn(&deleteLib.step);

	setupMosaicTarget(b, &deleteLib.step, .linux, .aarch64, "arm64");
	setupMosaicTarget(b, &deleteLib.step, .linux, .x86_64, "amd64");
	setupMosaicTarget(b, &deleteLib.step, .macos, .aarch64, "aarch64");
	setupMosaicTarget(b, &deleteLib.step, .macos, .x86_64, "x86_64");
	setupMosaicTarget(b, &deleteLib.step, .windows, .aarch64, "arm64");
	setupMosaicTarget(b, &deleteLib.step, .windows, .x86_64, "amd64");
}

fn setupMosaicTarget(b: *std.Build, step: *std.Build.Step, tag: std.Target.Os.Tag, arch: std.Target.Cpu.Arch, dir: []const u8) void {
	const lib = b.addSharedLibrary(.{
		.name = "mosaic",
		.target = b.resolveTargetQuery(.{
			.cpu_arch = arch,
			.os_tag = tag,
			.abi = if (tag == .linux) .gnu else null,
		}),
		.optimize = .ReleaseSmall,
	});

	lib.linkLibC();

	lib.addIncludePath(b.path("src/c"));
	lib.addIncludePath(b.path("src/jvmMain/include/share"));
	lib.addIncludePath(
		switch (tag) {
			.windows => b.path("src/jvmMain/include/windows"),
			else => b.path("src/jvmMain/include/unix"),
		}
	);

	// TODO Tree-walk these two dirs for all C files.
	lib.addCSourceFiles(.{
		.files = &.{
			"src/c/mosaic-rawMode-posix.c",
			"src/c/mosaic-rawMode-windows.c",
			"src/c/mosaic-stdin-posix.c",
			"src/c/mosaic-stdin-windows.c",
			"src/jvmMain/jni/mosaic-jni.c",
		},
		.flags = &.{
			"-std=gnu99",
		},
	});

	const install = b.addInstallArtifact(lib, .{
		.dest_dir = .{
			.override = .{
				.custom = dir,
			},
		},
	});

	step.dependOn(&install.step);
}
