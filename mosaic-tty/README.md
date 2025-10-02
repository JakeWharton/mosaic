# Mosaic TTY

Low-level TTY manipulation library.


## Windows

The way consoles work on Windows is painful, especially for testing.
When testing this module locally, I build an `.exe` and then run:
```
> start /min cmd /k "Z:\test.exe & rundll32 user32.dll,MessageBeep"
```
The console window cannot be visible, or regular key and mouse input interferes.
Wait until you hear a beep, and then open it to see the results.
