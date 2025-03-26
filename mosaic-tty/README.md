# Mosaic TTY

Low-level TTY manipulation library.


## Prerequisites

The JVM target requires native libraries which are built outside Gradle using Zig 0.14.0.

First, generate the JNI headers:
```
./gradlew compileJvmMainJava
```

Then, after downloading or installing Zig, in the `mosaic-tty/` directory run:
```
zig build -p src/jvmMain/resources
```
