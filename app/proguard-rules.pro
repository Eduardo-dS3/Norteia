# Norteia — R8/ProGuard rules.
#
# The app has no reflection-based serialization and no dynamic class loading,
# so the defaults from proguard-android-optimize.txt cover almost everything.

# Keep source file + line numbers for readable crash reports in Play Console,
# then hide the original file name.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Coroutines (defensive; AGP usually bundles these).
-dontwarn kotlinx.coroutines.**

# Google Play services location.
-keep class com.google.android.gms.location.** { *; }
-dontwarn com.google.android.gms.**
