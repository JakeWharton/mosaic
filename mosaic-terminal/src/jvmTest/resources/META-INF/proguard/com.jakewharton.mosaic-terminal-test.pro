-keep class **.*Test {
	public <init>();
	@org.junit.Test public void *(...);
}

# Gradle does A LOT of reflection to invoke JUnit. Just keep it all.
-keep class org.junit.** {
	*** *(...);
}

# Keep @Test, @Ignore annotations, etc.
-keepattributes RuntimeVisibleAnnotations, AnnotationDefault

# Temporarily work around a ProGuard bug. https://github.com/Guardsquare/proguard/issues/460
-optimizations !method/specialization/parametertype

# TODO These should be pulled from the jars, but for now this unblocks us.
-dontwarn kotlinx.coroutines.debug.internal.AgentPremain*
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn assertk.assertions.AnyJVMKt*

# ServiceLoader support
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Most of volatile fields are updated with AFU and should not be mangled
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Same story for the standard library's SafeContinuation that also uses AtomicReferenceFieldUpdater
-keepclassmembers class kotlin.coroutines.SafeContinuation {
    volatile <fields>;
}
