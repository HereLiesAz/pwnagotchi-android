# Kotlin
-dontwarn kotlin.**

# Kotlinx Serialization
-keepclassmembers class kotlinx.serialization.internal.* {
    *;
}
-keep class kotlinx.serialization.internal.* {
    *;
}
-keepclassmembers class **.serializer {
    *;
}

# Ktor
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
-dontwarn org.slf4j.**

# libsu
-keep class com.topjohnwu.superuser.** { *; }

# Jetpack Compose
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Jetpack Glance
-keep class androidx.glance.** { *; }
-keepclassmembers class androidx.glance.** { *; }
-dontwarn androidx.glance.**
