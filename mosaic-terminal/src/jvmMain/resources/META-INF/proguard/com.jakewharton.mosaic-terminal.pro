# Note: Our JNI code only creates JDK types and only uses Java built-in types across the boundary.
-keep class com.jakewharton.mosaic.terminal.Jni {
  native <methods>;
}

# These members are interacted with through native code.
-keep class com.jakewharton.mosaic.terminal.PlatformInput$Callback {
	void onFocus(...);
	void onKey(...);
	void onMouse(...);
	void onResize(...);
}
