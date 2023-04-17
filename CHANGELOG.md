# Change log

## [Unreleased]


## [0.6.0] - 2023-04-17

New:
- Support Kotlin 1.8.20 via JetBrains Compose compiler 1.4.5.

Changed:
- Upgrade JetBrains Compose runtime to 1.4.0.
- Reduce string copies and string allocation required to perform a single frame render.
- Only split text strings on newline when its value changes by caching layout information across recomposition.
- Canvas is no longer clipped for each node. If you notice any overlapping drawing, please report a bug.

Breaking:
- Composables were moved into `ui` subpackage.
- `Layout` and related interfaces were moved into `layout` subpackage.
- `Static` content is now based on `SnapshotStateList` instead of `Flow`.


## [0.5.0] - 2023-03-09

 - Support Kotlin 1.8.10 via JetBrains Compose compiler 1.4.2.
 - New: `renderMosaic` function returns a single string of the composition for tools that require only static output.
 - New: Expose a custom `Layout` composable similar to Compose UI. This is just the beginning of a larger change to expose more powerful primitives.
 - Implicit root node is no longer a `Row`. Multiple children at the root will now draw on top of each other. Choose a `Row` or `Column` as the root composable yourself.
 - Each `Static` content is no longer wrapped in a `Row`. Multiple children in a single `Static` composable will draw on top of each other. Choose a `Row` or `Column` if you have multiple items. Multiple `Static` composables will still render in `Column`-like behavior


## [0.4.0] - 2023-02-19

 - Mosaic is now multiplatform!

   The following targets are now supported in addition to the JVM:
     - Linux (X64)
     - MacOS (ARM & X64)
     - Windows (X64)
     - JS (experimental)

   Additionally, the JetBrains Compose compiler is now used instead of AndroidX which
   should offer better support for native and JS targets.

 - `runMosaic` is now a suspending function which will return when the composition ends.
   For the previous behavior, a `runMosaicBlocking` function is provided (JVM + native).


## [0.3.0] - 2023-01-17

 - Support Kotlin 1.8.0 via Compose compiler 1.4.0.
 - New: `Static` composable for rendering permanent output.
 - Fix: Correct line calculation to prevent output from drifting downward over time when its height changes.


## [0.2.0] - 2022-08-12

 - Support Kotlin 1.7.10 via Compose compiler 1.3.0.
 - Migrate from custom build of Compose compiler and Compose runtime to Google's Compose compiler and JetBrains' multiplatform Compose runtime. Note that this will require you have the Google Maven repositories in your Gradle repositories (`google()`).


## [0.1.0] - 2021-06-25

Initial release!


[Unreleased]: https://github.com/JakeWharton/mosaic/compare/0.5.0...HEAD
[0.5.0]: https://github.com/JakeWharton/mosaic/releases/tag/0.5.0
[0.4.0]: https://github.com/JakeWharton/mosaic/releases/tag/0.4.0
[0.3.0]: https://github.com/JakeWharton/mosaic/releases/tag/0.3.0
[0.2.0]: https://github.com/JakeWharton/mosaic/releases/tag/0.2.0
[0.1.0]: https://github.com/JakeWharton/mosaic/releases/tag/0.1.0
