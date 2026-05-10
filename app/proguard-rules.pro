# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve line number information for debugging stack traces
-keepattributes SourceFile,LineNumberTable

# Preserve original source file name
-renamesourcefileattribute SourceFile

# Firebase Realtime Database Models
# Keep the classes and all members (including no-arg constructors)
-keep class com.quick.bite.model.** { *; }

# Also keep classes used for serialization (Moshi/Firebase)
-keep @androidx.annotation.Keep class *
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}

# If you use Moshi's @JsonClass
-keep class com.quick.bite.model.**JsonAdapter { *; }
