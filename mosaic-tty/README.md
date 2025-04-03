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


## Windows

The way consoles work on Windows is painful, especially for testing.
When testing this module locally, I build an `.exe` and then run:
```
> start /min cmd /k "Z:\test.exe"
```
The console window cannot be visible, or regular key and mouse input interferes.
So wait a few seconds, and then open it to see the results.
