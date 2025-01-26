-keep class **.*Test {
	public <init>();
	@org.junit.Test public void *(...);
}

# Gradle does A LOT of reflection to invoke JUnit. Just keep it all.
-keep,includedescriptorclasses class org.junit.** {
	*** *(...);
}

# Keep @Test, @Ignore annotations, etc.
-keepattributes RuntimeVisibleAnnotations, AnnotationDefault

# Temporarily work around a ProGuard bug. https://github.com/Guardsquare/proguard/issues/460
-optimizations !method/specialization/parametertype

# TODO These should be pulled from the jars, but for now this unblocks us.

####################################################################################
# AssertK
####################################################################################

-dontwarn assertk.assertions.AnyJVMKt*

####################################################################################
# kotlinx.coroutines
####################################################################################

# ServiceLoader support
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# TODO Needed upstream
-keepnames class * implements kotlinx.coroutines.CoroutineExceptionHandler

# Most of volatile fields are updated with AFU and should not be mangled
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Same story for the standard library's SafeContinuation that also uses AtomicReferenceFieldUpdater
-keepclassmembers class kotlin.coroutines.SafeContinuation {
    volatile <fields>;
}

# These classes are only required by kotlinx.coroutines.debug.internal.AgentPremain, which is only loaded when
# kotlinx-coroutines-core is used as a Java agent, so these are not needed in contexts where ProGuard is used.
# TODO Needed upstream
-dontwarn kotlinx.coroutines.debug.internal.AgentPremain*

# Only used in `kotlinx.coroutines.internal.ExceptionsConstructor`.
# The case when it is not available is hidden in a `try`-`catch`, as well as a check for Android.
-dontwarn java.lang.ClassValue

# An annotation used for build tooling, won't be directly accessed.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
