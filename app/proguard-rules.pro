# Keep Media3 session, player & audio processors
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Keep KnightWin data models and persisted enum names
-keepclassmembers enum com.knightwin.music.data.model.** { *; }
-keep class com.knightwin.music.data.model.** { *; }

# Keep Android components
-keep class com.knightwin.music.KnightWinApp
-keep class com.knightwin.music.MainActivity
-keep class com.knightwin.music.playback.KnightPlaybackService
