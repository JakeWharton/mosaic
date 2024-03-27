# Change log

## [Unreleased]

New:
-

Changed:
- Disable klib signature clash checks for JS compilations. These occasionally occur as a result of Compose compiler behavior, and are safe to disable (the first-party JetBrains Compose Gradle plugin also disables them).

Fixed:
- Use CRLF line endings to fix rendering when a terminal is in raw mode.


## [0.11.0] - 2023-02-27

New:
- Support Kotlin 1.9.22 via JetBrains Compose compiler 1.5.10.
- `Filler` composable is like a `Spacer` but fills its area with a character instead of a space.
- `Box` without content provides the ability to render using drawing modifiers without needing an empty chidlren lambda.
- `Modifier.aspectRatio` attempts to constrain a composable to an aspect ratio in either the vertical or horizontal direction.
- `Modifier.offset` offsets the composable in its parent by the given coordinates.
- `Modifier.fillMaxWidth`, `Modifier.fillMaxHeight`, `Modifier.fillMaxSize`, `Modifier.wrapContentWidth`, `Modifier.wrapContentHeight`, `Modifier.wrapContentSize`, and `Modifier.defaultMinSize` help size composable measurement in relation to their parent.
- `Modifier.weight` allows sizing a composable proportionally to others within the same parent.
- `Row` and `Column` each feature an arrangement parameter which controls the placement of children on the main axis of the container.

Changed:
- `Modifier` parameter is now universally called `modifier` in the API.
- Disable decoy generation for JS target to make compatible with JetBrains Compose 1.6. This is an ABI-breaking change, so all Compose-based libraries targeting JS will also need to have been recompiled.

Fix:
- Ensure ANSI control sequences are written properly to Windows terminals.
- Robot sample now correctly moves on Windows.

This version works with Kotlin 1.9.22 by default.


## [0.10.0] - 2023-11-13

New:
- Support Kotlin 1.9.20 via JetBrains Compose compiler 1.5.3.

- `@MosaicComposable` annotation restricts composable function usage to those meant for Mosaic
  (e.g., our `Text`) or general-purpose (e.g., Compose's `remember`). In most cases the Compose
  compiler can infer this automatically, but it's available for explicit use if needed.

- `LocalTerminal` composition local provides the size of the terminal if it can be detected.
  If the size changes, your function will automatically be recomposed with the new values.

    ```kotlin
    val size = LocalTerminal.current.size
    Text("Terminal(w=${size.width}, h=${size.height})")
    ```

- `Row`, `Column`, and `Box` now support horizontal and vertical alignment of their children.

    ```kotlin
    Column {
      Text("This is very long")
      Text(
        "On the right",
        modifier = Modifier.align(End),
      )
    }
    ```

- Add `AnnotatedString` with `SpanStyle` for string customization. Instead of writing a series of
  `Text` functions in a `Row`, emit a single `Text` with formatting changes within the string.

    ```kotlin
    Text(buildAnnotatedString {
      append("Plain! ")
      withStyle(SpanStyle(color = BrightGreen)) {
        append("Green! ")
      }
      withStyle(SpanStyle(color = BrightBlue)) {
        append("Blue!")
      }
    })
    ```

- `Spacer` node for occupying space within layouts.

- Constraints and intrinsics are now available in the layout system.

This version works with Kotlin 1.9.20 by default.


## [0.9.1] - 2023-09-14

New:
- Support Kotlin 1.9.10 via JetBrains Compose compiler 1.5.2.

This version works with Kotlin 1.9.10 by default.


## [0.9.0] - 2023-08-09

New:
- Support for specifying custom Compose compiler versions. This will allow you to use the latest
  version of Molecule with newer versions of Kotlin than it explicitly supports.

  See [the README](https://github.com/JakeWharton/mosaic/#custom-compose-compiler) for more information.


## [0.8.0] - 2023-07-20

New:
- Support Kotlin 1.9.0 via JetBrains Compose compiler 1.5.0.


## [0.7.1] - 2023-06-30

New:
- Support Kotlin 1.8.22 via JetBrains Compose compiler 1.4.8.


## [0.7.0] - 2023-06-26

New:
- Support Kotlin 1.8.21 via JetBrains Compose compiler 1.4.7.
- Add support for modifiers on layouts and built-in components.
  There are two types which are built-in: `DrawModifier` and `LayoutModifier`.
  The built-in functions are `drawBehind`, `background`, `layout`, and `padding`.
- Add Box component.

Changed:
- Frames are now emitted slightly differently relying on Compose for signaling when a change has happened. Please report any problems.


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


[Unreleased]: https://github.com/JakeWharton/mosaic/compare/0.11.0...HEAD
[0.11.0]: https://github.com/JakeWharton/mosaic/releases/tag/0.11.0
[0.10.0]: https://github.com/JakeWharton/mosaic/releases/tag/0.10.0
[0.9.1]: https://github.com/JakeWharton/mosaic/releases/tag/0.9.1
[0.9.0]: https://github.com/JakeWharton/mosaic/releases/tag/0.9.0
[0.8.0]: https://github.com/JakeWharton/mosaic/releases/tag/0.8.0
[0.7.1]: https://github.com/JakeWharton/mosaic/releases/tag/0.7.1
[0.7.0]: https://github.com/JakeWharton/mosaic/releases/tag/0.7.0
[0.6.0]: https://github.com/JakeWharton/mosaic/releases/tag/0.6.0
[0.5.0]: https://github.com/JakeWharton/mosaic/releases/tag/0.5.0
[0.4.0]: https://github.com/JakeWharton/mosaic/releases/tag/0.4.0
[0.3.0]: https://github.com/JakeWharton/mosaic/releases/tag/0.3.0
[0.2.0]: https://github.com/JakeWharton/mosaic/releases/tag/0.2.0
[0.1.0]: https://github.com/JakeWharton/mosaic/releases/tag/0.1.0
