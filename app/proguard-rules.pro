# Add project specific ProGuard rules here.

# Keep line numbers for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ---- Gson ----
# Keep data classes used for JSON serialization (songs.json seed)
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
# Keep domain model fields used by Gson
-keepclassmembers class com.guitarapp.songsbook.domain.model.** { *; }
-keepclassmembers class com.guitarapp.songsbook.data.local.** { *; }
-keepclassmembers class com.guitarapp.songsbook.data.repository.SongbookResponse { *; }

# ---- Room ----
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ---- AdMob ----
-keep public class com.google.android.gms.ads.** { public *; }
-keep public class com.google.ads.** { public *; }

# ---- Firebase ----
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**

# ---- Kotlin coroutines ----
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
