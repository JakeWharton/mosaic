# Note: Our JNI code only creates JDK types and only uses Java built-in types across the boundary.
-keep,allowoptimization class com.jakewharton.mosaic.terminal.Jni {
  native <methods>;
}

# These members are interacted with through native code.
-keep,allowoptimization class com.jakewharton.mosaic.terminal.PlatformEventHandler {
	void onFocus(...);
	void onKey(...);
	void onMouse(...);
	void onResize(...);
}
