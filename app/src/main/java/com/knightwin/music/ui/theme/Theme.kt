package com.knightwin.music.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.knightwin.music.data.model.AccentTheme
import com.knightwin.music.data.model.AppSettings
import com.knightwin.music.data.model.ThemeMode

data class KnightExtraColors(
    val isDark: Boolean,
    val isOled: Boolean,
    val liquidGlassEnabled: Boolean,
    val oneUiCanvas: Color,
    val oneUiCard: Color,
    val oneUiDivider: Color
)

val LocalKnightExtras = staticCompositionLocalOf {
    KnightExtraColors(
        isDark = false,
        isOled = false,
        liquidGlassEnabled = true,
        oneUiCanvas = Color(0xFFF2F2F7),
        oneUiCard = Color(0xFFFFFFFF),
        oneUiDivider = Color(0x14000000)
    )
}

private val KnightTypography = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 42.sp, letterSpacing = (-0.5).sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 36.sp, letterSpacing = (-0.3).sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 24.sp, lineHeight = 30.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 14.sp)
)

@Composable
fun KnightWinTheme(
    settings: AppSettings,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (settings.themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val oled = darkTheme && settings.oledBlack
    val context = LocalContext.current

    val colorScheme = buildColorScheme(
        context = context,
        accentTheme = settings.accentTheme,
        darkTheme = darkTheme,
        oled = oled
    )

    // Authentic Samsung OneUI 8 Dark, OLED Pure Black & Light surface hierarchy following active accent
    val extras = if (darkTheme) {
        val oneUiDarkBg = colorScheme.background
        val oneUiDarkCard = colorScheme.surfaceContainerLow
        KnightExtraColors(
            isDark = true,
            isOled = oled,
            liquidGlassEnabled = settings.liquidGlassAccents,
            oneUiCanvas = if (oled) Color.Black else oneUiDarkBg,
            oneUiCard = if (oled) Color.Black else oneUiDarkCard,
            oneUiDivider = if (oled) Color(0x26FFFFFF) else Color(0x1EFFFFFF)
        )
    } else {
        KnightExtraColors(
            isDark = false,
            isOled = false,
            liquidGlassEnabled = settings.liquidGlassAccents,
            oneUiCanvas = colorScheme.background,
            oneUiCard = colorScheme.surfaceContainerLowest,
            oneUiDivider = Color(0x14000000)
        )
    }

    CompositionLocalProvider(LocalKnightExtras provides extras) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = KnightTypography
        ) {
            CompositionLocalProvider(
                LocalContentColor provides colorScheme.onBackground,
                content = content
            )
        }
    }
}

private fun buildColorScheme(
    context: Context,
    accentTheme: AccentTheme,
    darkTheme: Boolean,
    oled: Boolean
): ColorScheme {
    val isDynamic = accentTheme == AccentTheme.DYNAMIC
    val rawScheme = if (isDynamic) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        val primary = Color(accentTheme.primaryHex)
        val secondary = Color(accentTheme.secondaryHex)
        val tertiary = Color(accentTheme.tertiaryHex)
        if (darkTheme) {
            val lightPrimary = lerp(primary, Color.White, 0.22f)
            val container = lerp(primary, Color(0xFF0B0E14), 0.72f)
            darkColorScheme(
                primary = lightPrimary,
                onPrimary = Color(0xFF06131F),
                primaryContainer = container,
                onPrimaryContainer = lerp(primary, Color.White, 0.78f),
                secondary = lerp(secondary, Color.White, 0.25f),
                onSecondary = Color(0xFF08141E),
                secondaryContainer = lerp(secondary, Color(0xFF12151C), 0.75f),
                onSecondaryContainer = lerp(secondary, Color.White, 0.80f),
                tertiary = tertiary,
                onTertiary = Color(0xFF07131E),
                onBackground = Color(0xFFF4F6FA),
                onSurface = Color(0xFFF4F6FA),
                onSurfaceVariant = Color(0xFFA8B0BE)
            )
        } else {
            val darkPrimary = lerp(primary, Color.Black, 0.14f)
            val container = lerp(primary, Color.White, 0.82f)
            lightColorScheme(
                primary = darkPrimary,
                onPrimary = Color.White,
                primaryContainer = container,
                onPrimaryContainer = lerp(primary, Color.Black, 0.65f),
                secondary = lerp(secondary, Color.Black, 0.12f),
                onSecondary = Color.White,
                secondaryContainer = lerp(secondary, Color.White, 0.84f),
                onSecondaryContainer = lerp(secondary, Color.Black, 0.65f),
                tertiary = tertiary,
                onTertiary = Color.White,
                onBackground = Color(0xFF12151C),
                onSurface = Color(0xFF12151C),
                onSurfaceVariant = Color(0xFF565E6D)
            )
        }
    }

    if (oled && darkTheme) {
        return rawScheme.copy(
            background = Color.Black,
            surface = Color.Black,
            surfaceContainerLowest = Color.Black,
            surfaceContainerLow = Color.Black,
            surfaceContainer = Color.Black,
            surfaceContainerHigh = Color.Black,
            surfaceContainerHighest = Color.Black,
            surfaceVariant = Color.Black,
            outline = Color(0xFF4A4A56),
            outlineVariant = Color(0xFF2A2A32)
        )
    }

    val tintSource = if (isDynamic) rawScheme.primary else Color(accentTheme.primaryHex)

    return if (darkTheme) {
        val bgWeight = if (isDynamic) 0.045f else 0.06f
        val bg = lerp(Color(0xFF030406), tintSource, bgWeight)
        rawScheme.copy(
            background = bg,
            surface = bg,
            surfaceContainerLowest = lerp(Color(0xFF010203), tintSource, bgWeight * 0.65f),
            surfaceContainerLow = lerp(Color(0xFF07090D), tintSource, bgWeight * 1.15f),
            surfaceContainer = lerp(Color(0xFF0B0E14), tintSource, bgWeight * 1.35f),
            surfaceContainerHigh = lerp(Color(0xFF10141C), tintSource, bgWeight * 1.55f),
            surfaceContainerHighest = lerp(Color(0xFF161B26), tintSource, bgWeight * 1.75f),
            surfaceVariant = lerp(Color(0xFF0D1017), tintSource, bgWeight * 1.35f),
            outline = lerp(Color(0xFF3E4454), tintSource, bgWeight * 1.3f),
            outlineVariant = lerp(Color(0xFF1E232E), tintSource, bgWeight * 1.5f)
        )
    } else {
        val bgWeight = if (isDynamic) 0.055f else 0.045f
        val bg = lerp(Color(0xFFFAFBFD), tintSource, bgWeight)
        rawScheme.copy(
            background = bg,
            surface = bg,
            surfaceContainerLowest = lerp(Color(0xFFFFFFFF), tintSource, 0.015f),
            surfaceContainerLow = lerp(Color(0xFFF3F5F9), tintSource, bgWeight * 1.2f),
            surfaceContainer = lerp(Color(0xFFECEFF5), tintSource, bgWeight * 1.65f),
            surfaceContainerHigh = lerp(Color(0xFFE4E8F0), tintSource, bgWeight * 2.1f),
            surfaceContainerHighest = lerp(Color(0xFFDCE1EC), tintSource, bgWeight * 2.5f),
            surfaceVariant = lerp(Color(0xFFE7EAF2), tintSource, bgWeight * 1.75f),
            outline = lerp(Color(0xFF7B8394), tintSource, bgWeight * 2.0f),
            outlineVariant = lerp(Color(0xFFD0D5E0), tintSource, bgWeight * 2.8f)
        )
    }
}
