# Mosaic Animation

This is a Mosaic-adapted copy of
the [androidx.compose.animation-core](https://developer.android.com/reference/kotlin/androidx/compose/animation/core/package-summary)
library source code.

At the moment, there is also a color animation from
the [androidx.compose.animation](https://developer.android.com/reference/kotlin/androidx/compose/animation/package-summary)
library. Other things are difficult or impossible to adapt at the moment due to the specifics of
Mosaic.

Minimal changes were made during the adaptation. Deprecated methods have been removed, links to
samples from the kdoc have been removed, and links to Mosaic-specific methods, functions, and
classes have been changed. Also removed are things that cannot be adapted to Mosaic, such as Dp.
