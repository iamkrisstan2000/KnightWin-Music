package com.knightwin.music.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

enum class GlassVariant {
    CARD,
    PILL,
    DOCK,
    PROMINENT
}

/**
 * LiquidUI & Material 3 surface engine:
 *  - When LiquidUI is OFF: renders clean, pure Material 3 surfaces (pure #000000 + crisp hairlines in OLED).
 *  - When LiquidUI is ON + OLED: renders pure #000000 black glass with luminous accent-tinted specular rims.
 *  - When LiquidUI is ON + Light/Dark: renders frosted Liquid Glass surfaces tinted with active M3 accents.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    tintColor: Color = MaterialTheme.colorScheme.primary,
    elevation: Dp = 6.dp,
    variant: GlassVariant = GlassVariant.DOCK,
    pureM3Color: Color? = null,
    minOpacity: Float = 0f,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val extras = LocalKnightExtras.current
    val colorScheme = MaterialTheme.colorScheme
    val interactionModifier = when {
        onClick != null && onLongClick != null -> Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)
        onClick != null -> Modifier.clickable(onClick = onClick)
        else -> Modifier
    }
    val resolvedContentColor = if (variant == GlassVariant.PROMINENT) {
        colorScheme.onPrimary
    } else {
        colorScheme.onSurface
    }

    CompositionLocalProvider(LocalContentColor provides resolvedContentColor) {
        if (!extras.liquidGlassEnabled) {
            val m3Bg = if (extras.isOled) {
                pureM3Color ?: if (variant == GlassVariant.PROMINENT) tintColor else Color.Black
            } else {
                pureM3Color ?: when (variant) {
                    GlassVariant.CARD -> colorScheme.surfaceContainerLow
                    GlassVariant.PILL -> colorScheme.surfaceContainerHigh
                    GlassVariant.DOCK -> colorScheme.surfaceContainerHigh
                    GlassVariant.PROMINENT -> tintColor
                }
            }
            val m3Border = if (extras.isOled) {
                when (variant) {
                    GlassVariant.PROMINENT -> null
                    GlassVariant.CARD -> BorderStroke(0.85.dp, colorScheme.outlineVariant)
                    GlassVariant.PILL -> BorderStroke(0.95.dp, colorScheme.outlineVariant)
                    GlassVariant.DOCK -> BorderStroke(0.95.dp, colorScheme.outlineVariant)
                }
            } else {
                when (variant) {
                    GlassVariant.PROMINENT -> null
                    GlassVariant.CARD -> BorderStroke(0.6.dp, colorScheme.outlineVariant.copy(alpha = 0.28f))
                    GlassVariant.PILL -> BorderStroke(0.75.dp, colorScheme.outlineVariant.copy(alpha = 0.45f))
                    GlassVariant.DOCK -> BorderStroke(0.75.dp, colorScheme.outlineVariant.copy(alpha = 0.40f))
                }
            }
            val m3Shadow = if (extras.isOled) {
                0.dp
            } else {
                when (variant) {
                    GlassVariant.CARD -> 0.dp
                    GlassVariant.PILL -> 0.dp
                    GlassVariant.DOCK -> (elevation / 2).coerceAtMost(4.dp)
                    GlassVariant.PROMINENT -> 2.dp
                }
            }
            Box(
                modifier = modifier
                    .then(if (m3Shadow > 0.dp) Modifier.shadow(m3Shadow, shape, clip = false) else Modifier)
                    .clip(shape)
                    .background(m3Bg, shape)
                    .then(interactionModifier)
                    .then(if (m3Border != null) Modifier.border(m3Border, shape) else Modifier),
                content = content
            )
            return@CompositionLocalProvider
        }

        if (variant == GlassVariant.PROMINENT) {
            val glossyFill = Brush.verticalGradient(
                colors = listOf(
                    lerp(tintColor, Color.White, if (extras.isDark) 0.16f else 0.22f),
                    tintColor,
                    lerp(tintColor, Color.Black, if (extras.isDark) 0.14f else 0.10f)
                )
            )
            val specularTop = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = if (extras.isDark) 0.22f else 0.28f),
                    Color.Transparent
                )
            )
            val prominentRim = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = if (extras.isDark) 0.45f else 0.62f),
                    tintColor.copy(alpha = 0.28f),
                    Color.White.copy(alpha = 0.18f)
                )
            )
            Box(
                modifier = modifier
                    .shadow(
                        elevation = if (extras.isOled) 2.dp else elevation.coerceAtLeast(6.dp),
                        shape = shape,
                        ambientColor = tintColor.copy(alpha = if (extras.isOled) 0.22f else 0.35f),
                        spotColor = tintColor.copy(alpha = if (extras.isOled) 0.24f else 0.42f),
                        clip = false
                    )
                    .clip(shape)
                    .background(glossyFill, shape)
                    .background(specularTop, shape)
                    .then(interactionModifier)
                    .border(BorderStroke(0.95.dp, prominentRim), shape),
                content = content
            )
            return@CompositionLocalProvider
        }

        if (extras.isOled) {
            // Pure Black OLED LiquidUI: frosted translucent 85% opaque #000000 glass with crisp specular lip & accent rim
            val oledAlpha = 0.85f.coerceAtLeast(minOpacity)
            val oledBase = Color.Black.copy(alpha = oledAlpha)
            val topLipSheen = Brush.verticalGradient(
                0.0f to Color.White.copy(alpha = if (variant == GlassVariant.CARD) 0.08f else 0.115f),
                0.14f to tintColor.copy(alpha = if (variant == GlassVariant.CARD) 0.04f else 0.06f),
                0.55f to Color.Transparent,
                1.0f to Color.White.copy(alpha = 0.025f)
            )
            val oledGlassRim = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = if (variant == GlassVariant.CARD) 0.24f else 0.32f),
                    tintColor.copy(alpha = if (variant == GlassVariant.CARD) 0.34f else 0.44f),
                    colorScheme.tertiary.copy(alpha = 0.18f),
                    Color.White.copy(alpha = 0.10f)
                )
            )
            Box(
                modifier = modifier
                    .clip(shape)
                    .background(oledBase, shape)
                    .background(topLipSheen, shape)
                    .then(interactionModifier)
                    .border(BorderStroke(0.95.dp, oledGlassRim), shape),
                content = content
            )
            return@CompositionLocalProvider
        }

        val tintWeightTop = when (variant) {
            GlassVariant.CARD -> if (extras.isDark) 0.10f else 0.07f
            GlassVariant.PILL -> if (extras.isDark) 0.14f else 0.10f
            GlassVariant.DOCK -> if (extras.isDark) 0.16f else 0.11f
            GlassVariant.PROMINENT -> 1f
        }
        val tintWeightBottom = when (variant) {
            GlassVariant.CARD -> if (extras.isDark) 0.06f else 0.10f
            GlassVariant.PILL -> if (extras.isDark) 0.09f else 0.13f
            GlassVariant.DOCK -> if (extras.isDark) 0.11f else 0.15f
            GlassVariant.PROMINENT -> 1f
        }

        val baseFill = if (extras.isDark) {
            val darkTopBase = colorScheme.surfaceContainerHigh
            val darkBotBase = colorScheme.surfaceContainerLow
            val darkAlpha = 0.85f.coerceAtLeast(minOpacity)
            Brush.verticalGradient(
                colors = listOf(
                    lerp(darkTopBase, tintColor, tintWeightTop).copy(alpha = darkAlpha),
                    lerp(darkBotBase, colorScheme.secondary, tintWeightBottom).copy(alpha = darkAlpha)
                )
            )
        } else {
            val lightTopBase = colorScheme.surfaceContainerLowest
            val lightBotBase = colorScheme.surfaceContainerLow
            val lightAlpha = 0.90f.coerceAtLeast(minOpacity)
            Brush.verticalGradient(
                colors = listOf(
                    lerp(lightTopBase, tintColor, tintWeightTop).copy(alpha = lightAlpha),
                    lerp(lightBotBase, colorScheme.secondary, tintWeightBottom).copy(alpha = lightAlpha)
                )
            )
        }

        val specularSheen = if (extras.isDark) {
            Brush.verticalGradient(
                0.0f to Color.White.copy(alpha = if (variant == GlassVariant.CARD) 0.09f else 0.135f),
                0.24f to tintColor.copy(alpha = 0.055f),
                0.70f to Color.Transparent,
                1.0f to Color.White.copy(alpha = 0.03f)
            )
        } else {
            Brush.verticalGradient(
                0.0f to Color.White.copy(alpha = if (variant == GlassVariant.CARD) 0.56f else 0.66f),
                0.28f to tintColor.copy(alpha = 0.05f),
                0.75f to Color.Transparent,
                1.0f to Color.White.copy(alpha = 0.14f)
            )
        }

        val rimBorder = if (extras.isDark) {
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.32f),
                    tintColor.copy(alpha = 0.34f),
                    colorScheme.tertiary.copy(alpha = 0.18f),
                    Color.White.copy(alpha = 0.12f)
                )
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.96f),
                    tintColor.copy(alpha = 0.28f),
                    Color(0x14000000),
                    Color.White.copy(alpha = 0.55f)
                )
            )
        }

        val resolvedShadow = when (variant) {
            GlassVariant.CARD -> 0.dp
            GlassVariant.PILL -> 0.dp
            GlassVariant.DOCK -> if (minOpacity > 0f) elevation.coerceAtMost(8.dp) else 2.5.dp
            GlassVariant.PROMINENT -> elevation
        }

        Box(
            modifier = modifier
                .then(
                    if (resolvedShadow > 0.dp) {
                        Modifier.shadow(
                            elevation = resolvedShadow,
                            shape = shape,
                            ambientColor = tintColor.copy(alpha = if (extras.isDark) 0.18f else 0.14f),
                            spotColor = Color.Black.copy(alpha = if (extras.isDark) 0.22f else 0.12f),
                            clip = false
                        )
                    } else {
                        Modifier
                    }
                )
                .clip(shape)
                .background(baseFill, shape)
                .background(specularSheen, shape)
                .then(interactionModifier)
                .border(BorderStroke(0.95.dp, rimBorder), shape),
            content = content
        )
    }
}

/**
 * Renders a single row inside a virtualized LazyColumn so consecutive items share ONE unified
 * LiquidUI glass box (with outer rounded corners, continuous side/top/bottom rim border,
 * and clean hairline dividers between rows) without collapsing lazy virtualization.
 */
@Composable
fun GroupedLiquidGlassItem(
    isFirst: Boolean,
    isLast: Boolean,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 14.dp,
    cornerRadius: Dp = 22.dp,
    dividerStartPadding: Dp = 74.dp,
    showDivider: Boolean = !isLast,
    content: @Composable BoxScope.() -> Unit
) {
    val extras = LocalKnightExtras.current
    val colorScheme = MaterialTheme.colorScheme
    val tintColor = colorScheme.primary

    val itemShape = RoundedCornerShape(
        topStart = if (isFirst) cornerRadius else 0.dp,
        topEnd = if (isFirst) cornerRadius else 0.dp,
        bottomStart = if (isLast) cornerRadius else 0.dp,
        bottomEnd = if (isLast) cornerRadius else 0.dp
    )

    val baseBrush = when {
        extras.isOled -> SolidColor(Color.Black.copy(alpha = 0.85f))
        extras.isDark -> Brush.verticalGradient(
            colors = listOf(
                lerp(colorScheme.surfaceContainerHigh, tintColor, 0.08f).copy(alpha = 0.85f),
                lerp(colorScheme.surfaceContainerLow, colorScheme.secondary, 0.06f).copy(alpha = 0.85f)
            )
        )
        else -> Brush.verticalGradient(
            colors = listOf(
                lerp(colorScheme.surfaceContainerLowest, tintColor, 0.07f).copy(alpha = 0.90f),
                lerp(colorScheme.surfaceContainerLow, colorScheme.secondary, 0.10f).copy(alpha = 0.90f)
            )
        )
    }

    val rimBrush = if (extras.isDark) {
        Brush.horizontalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.28f),
                tintColor.copy(alpha = 0.26f),
                Color.White.copy(alpha = 0.16f)
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.92f),
                tintColor.copy(alpha = 0.24f),
                Color.White.copy(alpha = 0.60f)
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding)
            .clip(itemShape)
            .background(baseBrush, itemShape)
            .drawWithContent {
                drawContent()
                val sw = 0.95.dp.toPx()
                val hw = sw / 2f
                val w = size.width
                val h = size.height
                if (w <= sw || h <= sw) return@drawWithContent
                val stroke = Stroke(width = sw)
                when {
                    isFirst && isLast -> {
                        val r = minOf(cornerRadius.toPx(), h / 2f - hw, w / 2f - hw).coerceAtLeast(1f)
                        drawRoundRect(
                            brush = rimBrush,
                            topLeft = Offset(hw, hw),
                            size = androidx.compose.ui.geometry.Size(w - sw, h - sw),
                            cornerRadius = CornerRadius(r, r),
                            style = stroke
                        )
                    }
                    isFirst -> {
                        val r = minOf(cornerRadius.toPx(), h - hw, w / 2f - hw).coerceAtLeast(1f)
                        val d = r * 2f
                        val path = Path().apply {
                            moveTo(hw, h)
                            lineTo(hw, hw + r)
                            arcTo(Rect(hw, hw, hw + d, hw + d), 180f, 90f, false)
                            lineTo(w - hw - r, hw)
                            arcTo(Rect(w - hw - d, hw, w - hw, hw + d), 270f, 90f, false)
                            lineTo(w - hw, h)
                        }
                        drawPath(path = path, brush = rimBrush, style = stroke)
                    }
                    isLast -> {
                        val r = minOf(cornerRadius.toPx(), h - hw, w / 2f - hw).coerceAtLeast(1f)
                        val d = r * 2f
                        val path = Path().apply {
                            moveTo(hw, 0f)
                            lineTo(hw, h - hw - r)
                            arcTo(Rect(hw, h - hw - d, hw + d, h - hw), 180f, -90f, false)
                            lineTo(w - hw - r, h - hw)
                            arcTo(Rect(w - hw - d, h - hw - d, w - hw, h - hw), 90f, -90f, false)
                            lineTo(w - hw, 0f)
                        }
                        drawPath(path = path, brush = rimBrush, style = stroke)
                    }
                    else -> {
                        drawLine(brush = rimBrush, start = Offset(hw, 0f), end = Offset(hw, h), strokeWidth = sw)
                        drawLine(brush = rimBrush, start = Offset(w - hw, 0f), end = Offset(w - hw, h), strokeWidth = sw)
                    }
                }
            }
    ) {
        Box(modifier = Modifier.fillMaxWidth(), content = content)
        if (showDivider) {
            HorizontalDivider(
                thickness = 0.6.dp,
                color = extras.oneUiDivider,
                modifier = Modifier.padding(start = dividerStartPadding, end = 16.dp)
            )
        }
    }
}

/**
 * Applies a subtle vertical alpha fade at the top and/or bottom viewport edges so scrolled
 * list items dissolve gently beneath headers and above bottom navigation / mini player bars.
 */
fun Modifier.verticalEdgeFade(
    topFade: Dp = 14.dp,
    bottomFade: Dp = 16.dp
): Modifier = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        val h = size.height
        if (h <= 1f) return@drawWithContent
        val topPx = topFade.toPx().coerceAtLeast(0f)
        val botPx = bottomFade.toPx().coerceAtLeast(0f)
        val totalFade = topPx + botPx
        val scale = if (totalFade > h * 0.45f && totalFade > 0f) (h * 0.45f) / totalFade else 1f
        val topStop = ((topPx * scale) / h).coerceIn(0f, 0.25f)
        val botStop = (1f - ((botPx * scale) / h)).coerceIn(0.75f, 1f)

        val stops = buildList {
            if (topStop > 0.001f) {
                add(0f to Color.Transparent)
                add(topStop to Color.Black)
            } else {
                add(0f to Color.Black)
            }
            if (botStop < 0.999f) {
                add(botStop to Color.Black)
                add(1f to Color.Transparent)
            } else {
                add(1f to Color.Black)
            }
        }.toTypedArray()

        drawRect(
            brush = Brush.verticalGradient(*stops),
            blendMode = BlendMode.DstIn
        )
    }

/**
 * Ambient Material 3 accent backdrop used behind screens when LiquidUI is ON (non-OLED).
 * In OLED Pure Black mode or when LiquidUI is OFF, renders pure flat #000000 / M3 background.
 */
@Composable
fun LiquidAppBackdrop(
    modifier: Modifier = Modifier,
    useOneUiCanvas: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val extras = LocalKnightExtras.current
    val colorScheme = MaterialTheme.colorScheme
    val baseBg = when {
        extras.isOled -> Color.Black
        useOneUiCanvas -> extras.oneUiCanvas
        else -> colorScheme.background
    }

    if (!extras.liquidGlassEnabled || extras.isOled) {
        CompositionLocalProvider(LocalContentColor provides colorScheme.onBackground) {
            Box(
                modifier = modifier.background(baseBg),
                content = content
            )
        }
        return
    }

    val topGlow = colorScheme.primary.copy(alpha = if (extras.isDark) 0.11f else 0.12f)
    val midGlow = colorScheme.tertiary.copy(alpha = if (extras.isDark) 0.075f else 0.09f)
    val bottomGlow = colorScheme.secondary.copy(alpha = if (extras.isDark) 0.08f else 0.09f)

    CompositionLocalProvider(LocalContentColor provides colorScheme.onBackground) {
        Box(
            modifier = modifier
                .background(baseBg)
                .background(
                    Brush.radialGradient(
                        colors = listOf(topGlow, Color.Transparent),
                        center = Offset(180f, 180f),
                        radius = 950f
                    )
                )
                .background(
                    Brush.radialGradient(
                        colors = listOf(midGlow, Color.Transparent),
                        center = Offset(920f, 820f),
                        radius = 850f
                    )
                )
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            bottomGlow
                        )
                    )
                ),
            content = content
        )
    }
}

@Composable
fun liquidDialogContainerColor(): Color {
    val extras = LocalKnightExtras.current
    val colorScheme = MaterialTheme.colorScheme
    if (extras.isOled) return Color.Black
    if (!extras.liquidGlassEnabled) return colorScheme.surfaceContainerHigh
    return if (extras.isDark) {
        lerp(colorScheme.surfaceContainerHigh, colorScheme.primary, 0.10f)
    } else {
        lerp(colorScheme.surfaceContainerLowest, colorScheme.primary, 0.06f)
    }
}

/**
 * Theme-aware dialog that uses pure #000000 with crisp hairline / glass-rim borders in OLED mode,
 * frosted Liquid Glass when LiquidUI is ON, and standard Material 3 AlertDialog when OFF (non-OLED).
 */
@Composable
fun KnightDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
    confirmButton: @Composable () -> Unit,
    dismissButton: (@Composable () -> Unit)? = null
) {
    val extras = LocalKnightExtras.current
    if (!extras.liquidGlassEnabled && !extras.isOled) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            shape = RoundedCornerShape(28.dp),
            containerColor = liquidDialogContainerColor(),
            icon = icon,
            title = title,
            text = text,
            confirmButton = confirmButton,
            dismissButton = dismissButton
        )
        return
    }

    val colorScheme = MaterialTheme.colorScheme
    Dialog(onDismissRequest = onDismissRequest) {
        LiquidGlassSurface(
            shape = RoundedCornerShape(28.dp),
            variant = GlassVariant.DOCK,
            pureM3Color = Color.Black,
            minOpacity = 0.72f,
            elevation = 12.dp,
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                if (icon != null) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CompositionLocalProvider(LocalContentColor provides colorScheme.primary) {
                            icon()
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }
                if (title != null) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = if (icon != null) Alignment.Center else Alignment.CenterStart
                    ) {
                        CompositionLocalProvider(LocalContentColor provides colorScheme.onSurface) {
                            ProvideTextStyle(MaterialTheme.typography.headlineSmall) {
                                title()
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }
                if (text != null) {
                    Box(modifier = Modifier.weight(1f, fill = false)) {
                        CompositionLocalProvider(LocalContentColor provides colorScheme.onSurfaceVariant) {
                            ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                                text()
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    dismissButton?.invoke()
                    confirmButton()
                }
            }
        }
    }
}

/**
 * Theme-aware ModalBottomSheet that renders pure #000000 with crisp top hairline / glass-rim in OLED mode,
 * frosted Liquid Glass when LiquidUI is ON, and standard Material 3 ModalBottomSheet otherwise.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnightModalSheet(
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val extras = LocalKnightExtras.current
    if (!extras.liquidGlassEnabled && !extras.isOled) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            containerColor = liquidDialogContainerColor(),
            content = content
        )
        return
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Color.Transparent,
        dragHandle = null
    ) {
        LiquidGlassSurface(
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            variant = GlassVariant.DOCK,
            pureM3Color = if (extras.isOled) Color.Black else null,
            minOpacity = 0.72f,
            elevation = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp, bottom = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 36.dp, height = 4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f))
                    )
                }
                content()
            }
        }
    }
}
