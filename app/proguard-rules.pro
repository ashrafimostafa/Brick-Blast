# Hilt
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# kotlinx.serialization
# Without these, R8 strips the generated serializers and the release build crashes
# with "Serializer for class 'X' is not found" (e.g. type-safe Navigation routes).
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

# Keep the serialization runtime.
-keep class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**

# Keep every @Serializable class together with its generated $serializer.
-keepclasseswithmembers @kotlinx.serialization.Serializable class * {
    *;
}
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
    static **$* *;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class **$$serializer {
    *;
}

# Keep the app's serializable Navigation routes and data models by name so the
# serializer lookup used by Navigation-Compose works in the minified build.
-keep,includedescriptorclasses class com.mostafa.brickblast.navigation.** { *; }
-keep,includedescriptorclasses class com.mostafa.brickblast.domain.model.** { *; }
