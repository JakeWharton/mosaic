const std = @import("std");

pub fn build(b: *std.Build) !void {
	// The Windows builds create a .lib file in the lib/ directory which we don't need.
	const deleteLib = b.addRemoveDirTree(b.getInstallPath(.prefix, "lib"));
	b.getInstallStep().dependOn(&deleteLib.step);

	setupMosaicTarget(b, &deleteLib.step, .linux, .aarch64, "aarch64");
	setupMosaicTarget(b, &deleteLib.step, .linux, .x86_64, "amd64");
	setupMosaicTarget(b, &deleteLib.step, .macos, .aarch64, "aarch64");
	setupMosaicTarget(b, &deleteLib.step, .macos, .x86_64, "x86_64");
	setupMosaicTarget(b, &deleteLib.step, .windows, .aarch64, "aarch64");
	setupMosaicTarget(b, &deleteLib.step, .windows, .x86_64, "amd64");
}

fn setupMosaicTarget(b: *std.Build, step: *std.Build.Step, tag: std.Target.Os.Tag, arch: std.Target.Cpu.Arch, dir: []const u8) void {
	const libTty = b.addSharedLibrary(.{
		.name = "mosaic-tty",
		.target = b.resolveTargetQuery(.{
			.cpu_arch = arch,
			.os_tag = tag,
			// We need to explicitly specify gnu for linux, as otherwise it defaults to musl.
			// See https://github.com/ziglang/zig/issues/16624#issuecomment-1801175600.
			.abi = if (tag == .linux) .gnu else null,
		}),
		.optimize = .ReleaseSmall,
	});
	const libTestTty = b.addSharedLibrary(.{
		.name = "mosaic-test-tty",
		.target = b.resolveTargetQuery(.{
			.cpu_arch = arch,
			.os_tag = tag,
			// We need to explicitly specify gnu for linux, as otherwise it defaults to musl.
			// See https://github.com/ziglang/zig/issues/16624#issuecomment-1801175600.
			.abi = if (tag == .linux) .gnu else null,
		}),
		.optimize = .ReleaseSmall,
	});

	libTty.addIncludePath(b.path("src/commonMain/c"));
	libTty.addIncludePath(b.path("src/jvmMain/c"));
	libTty.addIncludePath(b.path("src/jvmMain/include/share"));
	libTty.addIncludePath(
		switch (tag) {
			.windows => b.path("src/jvmMain/include/windows"),
			else => b.path("src/jvmMain/include/unix"),
		}
	);

	libTestTty.addIncludePath(b.path("src/commonMain/c"));
	libTestTty.addIncludePath(b.path("src/jvmMain/c"));
	libTestTty.addIncludePath(b.path("src/jvmMain/include/share"));
	libTestTty.addIncludePath(
		switch (tag) {
			.windows => b.path("src/jvmMain/include/windows"),
			else => b.path("src/jvmMain/include/unix"),
		}
	);

	libTty.addCSourceFiles(.{
		.files = &.{
			"src/commonMain/c/mosaic-tty-posix.c",
			"src/commonMain/c/mosaic-tty-windows.c",
			"src/jvmMain/c/mosaic-tty-jni.c",
		},
		.flags = &.{
			"-std=gnu99",
		},
	});
	libTestTty.addCSourceFiles(.{
		.files = &.{
			"src/commonMain/c/mosaic-test-tty-posix.c",
			"src/commonMain/c/mosaic-test-tty-windows.c",
			"src/jvmMain/c/mosaic-test-tty-jni.c",
		},
		.flags = &.{
			"-std=gnu99",
		},
	});

	libTty.linkLibC();
	libTestTty.linkLibC();
	libTestTty.linkLibrary(libTty);

	const installTty = b.addInstallArtifact(libTty, .{
		.dest_dir = .{
			.override = .{
				.custom = dir,
			},
		},
	});
	const installTestTty = b.addInstallArtifact(libTestTty, .{
		.dest_dir = .{
			.override = .{
				.custom = dir,
			},
		},
	});

	step.dependOn(&installTty.step);
	step.dependOn(&installTestTty.step);
}
