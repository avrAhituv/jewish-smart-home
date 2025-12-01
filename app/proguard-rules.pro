# Add project specific ProGuard rules here.

# Keep KosherJava classes
-keep class com.kosherjava.** { *; }

# Keep Moshi classes
-keepclassmembers class * {
    @com.squareup.moshi.FromJson <methods>;
    @com.squareup.moshi.ToJson <methods>;
}

# Keep Google API classes
-keep class com.google.api.** { *; }

# Keep model classes
-keep class com.jewishhome.app.domain.model.** { *; }
-keep class com.jewishhome.app.data.remote.dto.** { *; }

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
