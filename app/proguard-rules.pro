# Keep Kotlin metadata
-keepattributes *Annotation*, InnerClasses, Signature, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Hilt
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }

# Room entities and DAOs
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *

# kotlinx.serialization
-keepclassmembers,allowobfuscation class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keep class kotlinx.serialization.** { *; }
-keep @kotlinx.serialization.Serializable class *

# Ktor / OkHttp
-dontwarn okhttp3.**
-keep class io.ktor.** { *; }

# Supabase
-keep class io.github.jan.supabase.** { *; }

# Coil
-keep class coil.** { *; }

# Remote model classes (keep field names for JSON deserialization)
-keep class org.aetherassembly.beforeitsgone.data.remote.** { *; }

# FreeDroidWarn (foss flavor only — dialog must survive R8 shrinking)
-keep class org.woheller69.freeDroidWarn.** { *; }
