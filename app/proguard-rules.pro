# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/.../Android/sdk/tools/proguard/proguard-android.txt

# Keep OneTap classes
-keep class com.onetap.** { *; }

# Keep Compose
-keep class androidx.compose.** { *; }
