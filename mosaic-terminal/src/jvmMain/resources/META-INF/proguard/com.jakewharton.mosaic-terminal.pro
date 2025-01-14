# Note: Our JNI code only creates JDK types and only uses Java built-in types across the boundary.
-keep,allowoptimization class com.jakewharton.mosaic.terminal.Jni {
  native <methods>;
}
