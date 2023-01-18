# Change log

## [Unreleased]


## [0.3.0] - 2023-01-17

 - Support Kotlin 1.8.0 via Compose compiler 1.4.0.
 - New: `Static` composable for rendering permanent output.
 - Fix: Correct line calculation to prevent output from drifting downward over time when its height changes.


## [0.2.0] - 2022-08-12

 - Support Kotlin 1.7.10 via Compose compiler 1.3.0.
 - Migrate from custom build of Compose compiler and Compose runtime to Google's Compose compiler and JetBrains' multiplatform Compose runtime. Note that this will require you have the Google Maven repositories in your Gradle repositories (`google()`).


## [0.1.0] - 2021-06-25

Initial release!


[Unreleased]: https://github.com/JakeWharton/mosaic/compare/0.3.0...HEAD
[0.3.0]: https://github.com/JakeWharton/mosaic/releases/tag/0.3.0
[0.2.0]: https://github.com/JakeWharton/mosaic/releases/tag/0.2.0
[0.1.0]: https://github.com/JakeWharton/mosaic/releases/tag/0.1.0
