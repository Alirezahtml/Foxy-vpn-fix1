# Keep all FoxyVPN classes, JNI bridges, and kotlinx.serialization companions in R8 full mode
-keep class com.vauth.foxyvpn.** { *; }
-keepnames class com.vauth.foxyvpn.**

-keep class io.nekohasekai.libbox.** { *; }
-keep interface io.nekohasekai.libbox.** { *; }
-keep class go.** { *; }
-keep interface go.** { *; }
-keepclasseswithmembernames class * {
    native <methods>;
}

# OkHttp & Okio
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Kotlinx Serialization (R8 full mode safe rules)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.vauth.foxyvpn.**$$serializer { *; }
-keepclassmembers class com.vauth.foxyvpn.** {
    *** Companion;
}
-keepclasseswithmembers class com.vauth.foxyvpn.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-dontwarn com.google.errorprone.annotations.**
-dontwarn reactor.blockhound.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
