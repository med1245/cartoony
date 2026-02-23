# ProGuard rules for Cartoony Provider
-keep class com.lagradost.** { *; }
-keep interface com.lagradost.** { *; }
-keepclassmembers class com.lagradost.** { *; }

# Keep all public classes that might be used for reflection
-keepclasseswithmembernames class * {
    native <methods>;
}

# Preserve line numbers for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Kotlin metadata
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses
