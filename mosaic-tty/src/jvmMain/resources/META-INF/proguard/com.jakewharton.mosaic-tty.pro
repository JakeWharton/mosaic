# Note: Our JNI code only creates JDK types and only uses Java built-in types across the boundary.
-keep class com.jakewharton.mosaic.tty.Jni {
  native <methods>;
}

-keepdirectories jni

# These members are interacted with through native code.
-keep interface com.jakewharton.mosaic.tty.Tty$Callback {
	<methods>;
}
