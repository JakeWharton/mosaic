# Keep native method names which are used by the consumer. Our JNI code only creates JDK types and
# only uses Java built-in types across the boundary.
-keepclasseswithmembernames class com.jakewharton.mosaic.terminal.** {
  native <methods>;
}
