# Mosaic Terminal

Low-level TTY manipulation and parsing library.


## Prerequisites

The JVM target requires native libraries which are built outside Gradle using Zig 0.13.0.

After downloading or installing Zig, in the `mosaic-terminal/` directory run:
```
zig build -p src/jvmMain/resources/jni
```
to create them.
