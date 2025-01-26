# Change log

## [Unreleased]
[Unreleased]: https://github.com/JakeWharton/mosaic/compare/0.15.0...HEAD

New:
- Add `setContentAndSnapshot` to 'mosaic-testing' which returns the initial composition snapshot. This avoids a potential problem with calling `setContent` and then `awaitSnapshot` since the latter will trigger a subsequent recomposition (if needed), preventing observation of the initial state.

Changed:
- Nothing yet!

Fixed:
- Nothing yet!


## [0.15.0] - 2025-01-07
[0.15.0]: https://github.com/JakeWharton/mosaic/releases/tag/0.15.0

New:
- Create `mosaic-animation` library, that provides various possibilities for animating Mosaic. An analog of [androidx.compose.animation-core](https://developer.android.com/reference/kotlin/androidx/compose/animation/core/package-summary).
- Add `IntrinsicSize` and related `Modifier.width`/`height`/`requiredWidth`/`requiredHeight`.
- New `mosaic-testing` library for deterministically rendering Mosaic composables under test.
- Add `Mosaic.layoutId` which allows identifying the element within its parent during layout.
- Add `Modifier.composed` that allows creating custom reusable modifiers with access to Compose functionality inside.

Changed:
- Rendering now occurs as fast as possible, although still only when necessary. Previously the maximum FPS was capped to 20, which could cause minor visual delays when processing events.

Fixed:
- Fix the handling of custom `Modifier`s that have multiple parents from `LayoutModifier`, `DrawModifier`, `KeyModifier`, `ParentDataModifier`.


## [0.14.0] - 2024-10-07
[0.14.0]: https://github.com/JakeWharton/mosaic/releases/tag/0.14.0

New:
- Support synchronized terminal update for rendering. This should eliminate tearing when rendering updates for terminals that support this feature.
- The terminal cursor is now automatically hidden during rendering and restored afterward.
- Added `Modifier.onKeyEvent` or `Modifier.onPreKeyEvent` to listen to keyboard events.
- Send real frame times into Compose which can be used for things like animations.

Changed:
- The entrypoints (`runMosaic` and `runMosaicBlocking`) have been changed to directly accept a composable lambda. Asynchronous work should now be performed inside Compose's effect system (e.g., `LaunchedEffect`), and rendering will complete when all effects have completed. Check out our samples for more information about how to do this.
- Сhange `DrawScope#drawRect` API with the ability to draw with text characters and specify `DrawStyle` (`Fill` or `Stroke`).
- Drop support for JS target. If you were using this, please file an issue so we can discuss support.


## [0.13.0] - 2024-05-28
[0.13.0]: https://github.com/JakeWharton/mosaic/releases/tag/0.13.0

New:
- Support Kotlin 2.0.0!

Changed:
- Remove our Gradle plugin in favor of JetBrains' (see below for more).

Note: Version 0.12.0 was also released today, but it still supports Kotlin 1.9.24.
Check out [its release entry](https://github.com/JakeWharton/mosaic/releases/tag/0.12.0) for more on what's new.


### Gradle plugin removed

This version of Mosaic removes the custom Gradle plugin in favor of [the official JetBrains Compose compiler plugin](https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-compiler.html) which ships as part of Kotlin itself.
Each module in which you had previously applied the `com.jakewharton.mosaic` plugin should be changed to apply `org.jetbrains.kotlin.plugin.compose` instead.
The Mosaic runtime will no longer be added as a result of the plugin change, and so any module which references Mosaic APIs should apply the `com.jakewharton.mosaic:mosaic-runtime` dependency.

For posterity, the Kotlin version compatibility table and compiler version customization for our old Mosaic Gradle plugin will be archived here:

<details>
<summary>Mosaic 0.12.0 Gradle plugin Kotlin compatibility table</summary>
<p>

Since Kotlin compiler plugins are an unstable API, certain versions of Mosaic only work with
certain versions of Kotlin.

| Kotlin | Mosaic        |
|--------|---------------|
| 1.9.24 | 0.12.0        |
| 1.9.22 | 0.11.0        |
| 1.9.20 | 0.10.0        |
| 1.9.10 | 0.9.1         |
| 1.9.0  | 0.8.0 - 0.9.0 |
| 1.8.22 | 0.7.1         |
| 1.8.21 | 0.7.0         |
| 1.8.20 | 0.6.0         |
| 1.8.10 | 0.5.0         |
| 1.8.0  | 0.3.0 - 0.4.0 |
| 1.7.10 | 0.2.0         |
| 1.5.10 | 0.1.0         |

</p>
</details>

<details>
<summary>Mosaic 0.12.0 Gradle plugin Compose compiler customization instructions</summary>
<p>

Each version of Mosaic ships with a specific JetBrains Compose compiler version which works with
a single version of Kotlin (see [version table](#usage) above). Newer versions of the Compose
compiler or alternate Compose compilers can be specified using the Gradle extension.

To use a new version of the JetBrains Compose compiler version:
```kotlin
mosaic {
  kotlinCompilerPlugin.set("1.4.8")
}
```

To use an alternate Compose compiler dependency:
```kotlin
mosaic {
  kotlinCompilerPlugin.set("com.example:custom-compose-compiler:1.0.0")
}
```

</p>
</details>


## [0.12.0] - 2024-05-28
[0.12.0]: https://github.com/JakeWharton/mosaic/releases/tag/0.12.0

New:
- Add `linuxArm64` target.
- Add `rrtop` sample.
- Support true color palette.

Changed:
- Disable klib signature clash checks for JS compilations. These occasionally occur as a result of Compose compiler behavior, and are safe to disable (the first-party JetBrains Compose Gradle plugin also disables them).
- Remove `Terminal$Size` and use `IntSize` instead in `Terminal#size` for optimization purposes.
- Remove `Color.Bright*` constants. Use `Color` function to create the desired color.
- Replace nullable `Color` and `TextStyle` with `Color.Unspecified` and `TextStyle.Unspecified` respectively. Also make `TextStyle` an inline class.

Fixed:
- Use CRLF line endings to fix rendering when a terminal is in raw mode.


## [0.11.0] - 2024-02-27
[0.11.0]: https://github.com/JakeWharton/mosaic/releases/tag/0.11.0

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
[0.10.0]: https://github.com/JakeWharton/mosaic/releases/tag/0.10.0

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
[0.9.1]: https://github.com/JakeWharton/mosaic/releases/tag/0.9.1

New:
- Support Kotlin 1.9.10 via JetBrains Compose compiler 1.5.2.

This version works with Kotlin 1.9.10 by default.


## [0.9.0] - 2023-08-09
[0.9.0]: https://github.com/JakeWharton/mosaic/releases/tag/0.9.0

New:
- Support for specifying custom Compose compiler versions. This will allow you to use the latest
  version of Molecule with newer versions of Kotlin than it explicitly supports.

  See [the README](https://github.com/JakeWharton/mosaic/#custom-compose-compiler) for more information.


## [0.8.0] - 2023-07-20
[0.8.0]: https://github.com/JakeWharton/mosaic/releases/tag/0.8.0

New:
- Support Kotlin 1.9.0 via JetBrains Compose compiler 1.5.0.


## [0.7.1] - 2023-06-30
[0.7.1]: https://github.com/JakeWharton/mosaic/releases/tag/0.7.1

New:
- Support Kotlin 1.8.22 via JetBrains Compose compiler 1.4.8.


## [0.7.0] - 2023-06-26
[0.7.0]: https://github.com/JakeWharton/mosaic/releases/tag/0.7.0

New:
- Support Kotlin 1.8.21 via JetBrains Compose compiler 1.4.7.
- Add support for modifiers on layouts and built-in components.
  There are two types which are built-in: `DrawModifier` and `LayoutModifier`.
  The built-in functions are `drawBehind`, `background`, `layout`, and `padding`.
- Add Box component.

Changed:
- Frames are now emitted slightly differently relying on Compose for signaling when a change has happened. Please report any problems.


## [0.6.0] - 2023-04-17
[0.6.0]: https://github.com/JakeWharton/mosaic/releases/tag/0.6.0

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
[0.5.0]: https://github.com/JakeWharton/mosaic/releases/tag/0.5.0

 - Support Kotlin 1.8.10 via JetBrains Compose compiler 1.4.2.
 - New: `renderMosaic` function returns a single string of the composition for tools that require only static output.
 - New: Expose a custom `Layout` composable similar to Compose UI. This is just the beginning of a larger change to expose more powerful primitives.
 - Implicit root node is no longer a `Row`. Multiple children at the root will now draw on top of each other. Choose a `Row` or `Column` as the root composable yourself.
 - Each `Static` content is no longer wrapped in a `Row`. Multiple children in a single `Static` composable will draw on top of each other. Choose a `Row` or `Column` if you have multiple items. Multiple `Static` composables will still render in `Column`-like behavior


## [0.4.0] - 2023-02-19
[0.4.0]: https://github.com/JakeWharton/mosaic/releases/tag/0.4.0

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
[0.3.0]: https://github.com/JakeWharton/mosaic/releases/tag/0.3.0

 - Support Kotlin 1.8.0 via Compose compiler 1.4.0.
 - New: `Static` composable for rendering permanent output.
 - Fix: Correct line calculation to prevent output from drifting downward over time when its height changes.


## [0.2.0] - 2022-08-12
[0.2.0]: https://github.com/JakeWharton/mosaic/releases/tag/0.2.0

 - Support Kotlin 1.7.10 via Compose compiler 1.3.0.
 - Migrate from custom build of Compose compiler and Compose runtime to Google's Compose compiler and JetBrains' multiplatform Compose runtime. Note that this will require you have the Google Maven repositories in your Gradle repositories (`google()`).


## [0.1.0] - 2021-06-25
[0.1.0]: https://github.com/JakeWharton/mosaic/releases/tag/0.1.0

Initial release!
