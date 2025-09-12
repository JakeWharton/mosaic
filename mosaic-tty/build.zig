const std = @import("std");

pub fn build(b: *std.Build) !void {
	const javaHeaders = b.option([]const u8, "javah", "Path to generated Java headers") orelse {
		std.log.err("javah option is required", .{});
		b.invalid_user_input = true;
		return;
	};

	// The Windows builds create a .lib file in the lib/ directory which we don't need.
	const deleteLib = b.addRemoveDirTree(.{ .cwd_relative = b.getInstallPath(.prefix, "lib") });
	b.getInstallStep().dependOn(&deleteLib.step);

	setupMosaicTarget(b, &deleteLib.step, javaHeaders, .linux, .aarch64, "com/jakewharton/mosaic/tty/jni/aarch64");
	setupMosaicTarget(b, &deleteLib.step, javaHeaders, .linux, .x86_64, "com/jakewharton/mosaic/tty/jni/amd64");
	setupMosaicTarget(b, &deleteLib.step, javaHeaders, .macos, .aarch64, "com/jakewharton/mosaic/tty/jni/aarch64");
	setupMosaicTarget(b, &deleteLib.step, javaHeaders, .macos, .x86_64, "com/jakewharton/mosaic/tty/jni/x86_64");
	setupMosaicTarget(b, &deleteLib.step, javaHeaders, .windows, .aarch64, "com/jakewharton/mosaic/tty/jni/aarch64");
	setupMosaicTarget(b, &deleteLib.step, javaHeaders, .windows, .x86_64, "com/jakewharton/mosaic/tty/jni/amd64");
}

fn setupMosaicTarget(b: *std.Build, step: *std.Build.Step, headers: []const u8, tag: std.Target.Os.Tag, arch: std.Target.Cpu.Arch, dir: []const u8) void {
	const lib = b.addLibrary(.{
		.name = "mosaic",
		.linkage = .dynamic,
		.root_module = b.createModule(.{
			.target = b.resolveTargetQuery(.{
				.cpu_arch = arch,
				.os_tag = tag,
				// We need to explicitly specify gnu for linux, as otherwise it defaults to musl.
				// See https://github.com/ziglang/zig/issues/16624#issuecomment-1801175600.
			.abi = if (tag == .linux) .gnu else null,
			}),
			.optimize = .ReleaseSmall,
		}),
	});

	lib.linkLibC();

	lib.addIncludePath(b.path("src/commonMain/c"));
	lib.addIncludePath(b.path(headers));
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
			"src/commonMain/c/mosaic-streams-posix.c",
			"src/commonMain/c/mosaic-streams-windows.c",
			"src/commonMain/c/mosaic-tty-posix.c",
			"src/commonMain/c/mosaic-tty-windows.c",
			"src/commonMain/c/mosaic-test-tty-posix.c",
			"src/commonMain/c/mosaic-test-tty-windows.c",
			"src/jvmMain/c/com_jakewharton_mosaic_tty_Jni.c",
		},
		.flags = &.{
			"-std=gnu11",
			"-Wall",
			"-Wextra",
			"-Werror",
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
