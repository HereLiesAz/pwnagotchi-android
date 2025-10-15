# Add project specific ProGuard rules here.
# By default, the Kotlin metadata is kept by the ProGuard rules that ship with AGP.
# You can refer to the following link for more details:
# https://developer.android.com/studio/build/shrink-code

# Keep all @Serializable classes and their serializers
-keepclasseswithmembers public class * {
    @kotlinx.serialization.Serializable <methods>;
}
-keep class **$$serializer { *; }
