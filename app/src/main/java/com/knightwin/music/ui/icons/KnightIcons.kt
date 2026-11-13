package com.knightwin.music.ui.icons

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.knightwin.music.ui.theme.LocalKnightExtras

object KnightIcons {

    val Songs: ImageVector by lazy {
        vector("Songs") {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.9f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(9.5f, 17.5f)
                lineTo(9.5f, 6.2f)
                curveTo(9.5f, 5.4f, 10.0f, 4.8f, 10.8f, 4.6f)
                lineTo(18.8f, 3.2f)
                curveTo(19.6f, 3.0f, 20.2f, 3.5f, 20.2f, 4.3f)
                lineTo(20.2f, 15.5f)
            }
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.9f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(9.5f, 9.2f)
                lineTo(20.2f, 7.3f)
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(9.5f, 17.5f)
                arcTo(3.2f, 2.5f, -15f, isMoreThanHalf = true, isPositiveArc = true, 3.3f, 18.8f)
                arcTo(3.2f, 2.5f, -15f, isMoreThanHalf = true, isPositiveArc = true, 9.5f, 17.5f)
                close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(20.2f, 15.5f)
                arcTo(3.2f, 2.5f, -15f, isMoreThanHalf = true, isPositiveArc = true, 14.0f, 16.8f)
                arcTo(3.2f, 2.5f, -15f, isMoreThanHalf = true, isPositiveArc = true, 20.2f, 15.5f)
                close()
            }
        }
    }

    val Albums: ImageVector by lazy {
        vector("Albums") {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(7.0f, 3.5f)
                lineTo(17.0f, 3.5f)
                moveTo(5.2f, 6.3f)
                lineTo(18.8f, 6.3f)
            }
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.9f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(6.2f, 9.0f)
                lineTo(17.8f, 9.0f)
                quadTo(20.5f, 9.0f, 20.5f, 11.7f)
                lineTo(20.5f, 18.3f)
                quadTo(20.5f, 21.0f, 17.8f, 21.0f)
                lineTo(6.2f, 21.0f)
                quadTo(3.5f, 21.0f, 3.5f, 18.3f)
                lineTo(3.5f, 11.7f)
                quadTo(3.5f, 9.0f, 6.2f, 9.0f)
                close()
            }
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.6f
            ) {
                moveTo(14.5f, 15.0f)
                arcTo(2.5f, 2.5f, 0f, isMoreThanHalf = true, isPositiveArc = true, 9.5f, 15.0f)
                arcTo(2.5f, 2.5f, 0f, isMoreThanHalf = true, isPositiveArc = true, 14.5f, 15.0f)
                close()
            }
        }
    }

    val Artists: ImageVector by lazy {
        vector("Artists") {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.9f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12.0f, 4.0f)
                arcTo(3.5f, 3.5f, 0f, isMoreThanHalf = true, isPositiveArc = true, 11.99f, 4.0f)
                close()
                moveTo(4.8f, 20.0f)
                curveTo(5.2f, 16.0f, 8.0f, 14.0f, 12.0f, 14.0f)
                curveTo(16.0f, 14.0f, 18.8f, 16.0f, 19.2f, 20.0f)
            }
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.7f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(17.8f, 5.5f)
                curveTo(19.2f, 6.8f, 19.8f, 8.6f, 19.5f, 10.5f)
            }
        }
    }

    val Playlists: ImageVector by lazy {
        vector("Playlists") {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.9f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3.8f, 6.5f)
                lineTo(14.5f, 6.5f)
                moveTo(3.8f, 11.2f)
                lineTo(14.5f, 11.2f)
                moveTo(3.8f, 16.0f)
                lineTo(10.5f, 16.0f)
                moveTo(16.5f, 17.5f)
                lineTo(16.5f, 6.8f)
                curveTo(16.5f, 6.8f, 19.5f, 7.2f, 20.4f, 9.8f)
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(16.5f, 17.6f)
                arcTo(2.7f, 2.2f, -15f, isMoreThanHalf = true, isPositiveArc = true, 11.4f, 18.8f)
                arcTo(2.7f, 2.2f, -15f, isMoreThanHalf = true, isPositiveArc = true, 16.5f, 17.6f)
                close()
            }
        }
    }

    val Search: ImageVector by lazy {
        vector("Search") {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2.0f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(17.2f, 10.6f)
                arcTo(6.6f, 6.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 4.0f, 10.6f)
                arcTo(6.6f, 6.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 17.2f, 10.6f)
                close()
                moveTo(15.6f, 15.6f)
                lineTo(20.2f, 20.2f)
            }
        }
    }

    val SettingsGear: ImageVector by lazy {
        vector("Settings") {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.9f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(15.0f, 12.0f)
                arcTo(3.0f, 3.0f, 0f, isMoreThanHalf = true, isPositiveArc = true, 9.0f, 12.0f)
                arcTo(3.0f, 3.0f, 0f, isMoreThanHalf = true, isPositiveArc = true, 15.0f, 12.0f)
                close()
                moveTo(9.86f, 5.23f)
                lineTo(10.42f, 2.53f)
                arcTo(9.60f, 9.60f, 0f, isMoreThanHalf = false, isPositiveArc = true, 13.58f, 2.53f)
                lineTo(14.14f, 5.23f)
                arcTo(7.10f, 7.10f, 0f, isMoreThanHalf = false, isPositiveArc = true, 16.80f, 6.77f)
                lineTo(19.41f, 5.89f)
                arcTo(9.60f, 9.60f, 0f, isMoreThanHalf = false, isPositiveArc = true, 20.99f, 8.64f)
                lineTo(18.93f, 10.46f)
                arcTo(7.10f, 7.10f, 0f, isMoreThanHalf = false, isPositiveArc = true, 18.93f, 13.54f)
                lineTo(20.99f, 15.36f)
                arcTo(9.60f, 9.60f, 0f, isMoreThanHalf = false, isPositiveArc = true, 19.41f, 18.11f)
                lineTo(16.80f, 17.23f)
                arcTo(7.10f, 7.10f, 0f, isMoreThanHalf = false, isPositiveArc = true, 14.14f, 18.77f)
                lineTo(13.58f, 21.47f)
                arcTo(9.60f, 9.60f, 0f, isMoreThanHalf = false, isPositiveArc = true, 10.42f, 21.47f)
                lineTo(9.86f, 18.77f)
                arcTo(7.10f, 7.10f, 0f, isMoreThanHalf = false, isPositiveArc = true, 7.20f, 17.23f)
                lineTo(4.59f, 18.11f)
                arcTo(9.60f, 9.60f, 0f, isMoreThanHalf = false, isPositiveArc = true, 3.01f, 15.36f)
                lineTo(5.07f, 13.54f)
                arcTo(7.10f, 7.10f, 0f, isMoreThanHalf = false, isPositiveArc = true, 5.07f, 10.46f)
                lineTo(3.01f, 8.64f)
                arcTo(9.60f, 9.60f, 0f, isMoreThanHalf = false, isPositiveArc = true, 4.59f, 5.89f)
                lineTo(7.20f, 6.77f)
                arcTo(7.10f, 7.10f, 0f, isMoreThanHalf = false, isPositiveArc = true, 9.86f, 5.23f)
                close()
            }
        }
    }

    val Play: ImageVector by lazy {
        vector("Play") {
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.6f,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(7.5f, 5.2f)
                lineTo(18.6f, 12.0f)
                lineTo(7.5f, 18.8f)
                close()
            }
        }
    }

    val Pause: ImageVector by lazy {
        vector("Pause") {
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.2f,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(6.8f, 5.2f)
                lineTo(9.4f, 5.2f)
                lineTo(9.4f, 18.8f)
                lineTo(6.8f, 18.8f)
                close()
                moveTo(14.6f, 5.2f)
                lineTo(17.2f, 5.2f)
                lineTo(17.2f, 18.8f)
                lineTo(14.6f, 18.8f)
                close()
            }
        }
    }

    val SkipNext: ImageVector by lazy {
        vector("Next") {
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.4f,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3.8f, 7.2f)
                lineTo(11.6f, 12.0f)
                lineTo(3.8f, 16.8f)
                close()
                moveTo(12.2f, 7.2f)
                lineTo(20.0f, 12.0f)
                lineTo(12.2f, 16.8f)
                close()
            }
        }
    }

    val SkipPrevious: ImageVector by lazy {
        vector("Previous") {
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.4f,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(20.2f, 7.2f)
                lineTo(12.4f, 12.0f)
                lineTo(20.2f, 16.8f)
                close()
                moveTo(11.8f, 7.2f)
                lineTo(4.0f, 12.0f)
                lineTo(11.8f, 16.8f)
                close()
            }
        }
    }

    val Shuffle: ImageVector by lazy {
        vector("Shuffle") {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.95f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3.5f, 7.2f)
                lineTo(7.4f, 7.2f)
                curveTo(9.2f, 7.2f, 10.6f, 8.6f, 12.0f, 12.0f)
                curveTo(13.4f, 15.4f, 14.8f, 16.8f, 16.6f, 16.8f)
                lineTo(20.2f, 16.8f)
                moveTo(17.8f, 14.4f)
                lineTo(20.4f, 16.8f)
                lineTo(17.8f, 19.2f)

                moveTo(3.5f, 16.8f)
                lineTo(7.4f, 16.8f)
                curveTo(8.8f, 16.8f, 9.8f, 16.0f, 10.7f, 14.5f)
                moveTo(13.3f, 9.5f)
                curveTo(14.2f, 8.0f, 15.2f, 7.2f, 16.6f, 7.2f)
                lineTo(20.2f, 7.2f)
                moveTo(17.8f, 4.8f)
                lineTo(20.4f, 7.2f)
                lineTo(17.8f, 9.6f)
            }
        }
    }

    val Repeat: ImageVector by lazy {
        vector("Repeat") {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.95f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(17.2f, 4.4f)
                lineTo(19.8f, 6.8f)
                lineTo(17.2f, 9.2f)
                moveTo(4.2f, 12.0f)
                lineTo(4.2f, 9.8f)
                curveTo(4.2f, 8.0f, 5.5f, 6.8f, 7.4f, 6.8f)
                lineTo(19.5f, 6.8f)

                moveTo(6.8f, 19.6f)
                lineTo(4.2f, 17.2f)
                lineTo(6.8f, 14.8f)
                moveTo(19.8f, 12.0f)
                lineTo(19.8f, 14.2f)
                curveTo(19.8f, 16.0f, 18.5f, 17.2f, 16.6f, 17.2f)
                lineTo(4.5f, 17.2f)
            }
        }
    }

    val RepeatOne: ImageVector by lazy {
        vector("RepeatOne") {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.95f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(17.2f, 4.4f)
                lineTo(19.8f, 6.8f)
                lineTo(17.2f, 9.2f)
                moveTo(4.2f, 12.0f)
                lineTo(4.2f, 9.8f)
                curveTo(4.2f, 8.0f, 5.5f, 6.8f, 7.4f, 6.8f)
                lineTo(19.5f, 6.8f)

                moveTo(6.8f, 19.6f)
                lineTo(4.2f, 17.2f)
                lineTo(6.8f, 14.8f)
                moveTo(19.8f, 12.0f)
                lineTo(19.8f, 14.2f)
                curveTo(19.8f, 16.0f, 18.5f, 17.2f, 16.6f, 17.2f)
                lineTo(4.5f, 17.2f)

                moveTo(11.1f, 10.6f)
                lineTo(12.2f, 9.8f)
                lineTo(12.2f, 14.5f)
            }
        }
    }

    val HeartOutline: ImageVector by lazy {
        vector("HeartOutline") {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.9f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12.0f, 20.2f)
                curveTo(12.0f, 20.2f, 4.0f, 15.1f, 4.0f, 9.2f)
                curveTo(4.0f, 6.4f, 6.2f, 4.4f, 8.8f, 4.4f)
                curveTo(10.3f, 4.4f, 11.4f, 5.2f, 12.0f, 6.2f)
                curveTo(12.6f, 5.2f, 13.7f, 4.4f, 15.2f, 4.4f)
                curveTo(17.8f, 4.4f, 20.0f, 6.4f, 20.0f, 9.2f)
                curveTo(20.0f, 15.1f, 12.0f, 20.2f, 12.0f, 20.2f)
                close()
            }
        }
    }

    val HeartFilled: ImageVector by lazy {
        vector("HeartFilled") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(12.0f, 20.5f)
                curveTo(12.0f, 20.5f, 3.6f, 15.2f, 3.6f, 9.2f)
                curveTo(3.6f, 6.2f, 6.0f, 4.0f, 8.8f, 4.0f)
                curveTo(10.3f, 4.0f, 11.5f, 4.8f, 12.0f, 5.9f)
                curveTo(12.5f, 4.8f, 13.7f, 4.0f, 15.2f, 4.0f)
                curveTo(18.0f, 4.0f, 20.4f, 6.2f, 20.4f, 9.2f)
                curveTo(20.4f, 15.2f, 12.0f, 20.5f, 12.0f, 20.5f)
                close()
            }
        }
    }

    val Queue: ImageVector by lazy {
        vector("Queue") {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.95f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(4.0f, 6.2f)
                lineTo(15.0f, 6.2f)
                moveTo(4.0f, 11.2f)
                lineTo(15.0f, 11.2f)
                moveTo(4.0f, 16.2f)
                lineTo(11.0f, 16.2f)
                moveTo(15.5f, 16.2f)
                lineTo(20.0f, 16.2f)
                moveTo(17.75f, 14.0f)
                lineTo(17.75f, 18.4f)
            }
        }
    }

    val Equalizer: ImageVector by lazy {
        vector("Equalizer") {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.9f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(6.0f, 4.0f); lineTo(6.0f, 9.0f)
                moveTo(6.0f, 13.5f); lineTo(6.0f, 20.0f)
                moveTo(12.0f, 4.0f); lineTo(12.0f, 14.0f)
                moveTo(12.0f, 18.5f); lineTo(12.0f, 20.0f)
                moveTo(18.0f, 4.0f); lineTo(18.0f, 6.5f)
                moveTo(18.0f, 11.0f); lineTo(18.0f, 20.0f)
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(8.0f, 11.2f); arcTo(2.0f, 2.0f, 0f, true, true, 4.0f, 11.2f); arcTo(2.0f, 2.0f, 0f, true, true, 8.0f, 11.2f); close()
                moveTo(14.0f, 16.2f); arcTo(2.0f, 2.0f, 0f, true, true, 10.0f, 16.2f); arcTo(2.0f, 2.0f, 0f, true, true, 14.0f, 16.2f); close()
                moveTo(20.0f, 8.8f); arcTo(2.0f, 2.0f, 0f, true, true, 16.0f, 8.8f); arcTo(2.0f, 2.0f, 0f, true, true, 20.0f, 8.8f); close()
            }
        }
    }

    val SleepMoon: ImageVector by lazy {
        vector("SleepMoon") {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.9f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(19.2f, 14.6f)
                arcTo(7.8f, 7.8f, 0f, isMoreThanHalf = true, isPositiveArc = true, 9.4f, 4.8f)
                arcTo(6.2f, 6.2f, 0f, isMoreThanHalf = false, isPositiveArc = false, 19.2f, 14.6f)
                close()
            }
        }
    }

    val BackChevron: ImageVector by lazy {
        vector("Back") {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2.2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(15.0f, 5.2f)
                lineTo(8.2f, 12.0f)
                lineTo(15.0f, 18.8f)
            }
        }
    }

    val ChevronRight: ImageVector by lazy {
        vector("ChevronRight") {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2.2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(9.0f, 5.2f)
                lineTo(15.8f, 12.0f)
                lineTo(9.0f, 18.8f)
            }
        }
    }

    val ChevronDown: ImageVector by lazy {
        vector("ChevronDown") {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2.2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(6.0f, 9.0f)
                lineTo(12.0f, 15.0f)
                lineTo(18.0f, 9.0f)
            }
        }
    }

    val MoreDots: ImageVector by lazy {
        vector("More") {
            path(fill = SolidColor(Color.Black)) {
                moveTo(6.5f, 12.0f); arcTo(1.6f, 1.6f, 0f, true, true, 3.3f, 12.0f); arcTo(1.6f, 1.6f, 0f, true, true, 6.5f, 12.0f); close()
                moveTo(13.6f, 12.0f); arcTo(1.6f, 1.6f, 0f, true, true, 10.4f, 12.0f); arcTo(1.6f, 1.6f, 0f, true, true, 13.6f, 12.0f); close()
                moveTo(20.7f, 12.0f); arcTo(1.6f, 1.6f, 0f, true, true, 17.5f, 12.0f); arcTo(1.6f, 1.6f, 0f, true, true, 20.7f, 12.0f); close()
            }
        }
    }

    val Add: ImageVector by lazy {
        vector("Add") {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2.1f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(12.0f, 4.8f); lineTo(12.0f, 19.2f)
                moveTo(4.8f, 12.0f); lineTo(19.2f, 12.0f)
            }
        }
    }

    val Close: ImageVector by lazy {
        vector("Close") {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2.1f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(6.0f, 6.0f); lineTo(18.0f, 18.0f)
                moveTo(18.0f, 6.0f); lineTo(6.0f, 18.0f)
            }
        }
    }

    val Check: ImageVector by lazy {
        vector("Check") {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2.2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(5.0f, 12.5f)
                lineTo(9.8f, 17.2f)
                lineTo(19.2f, 7.2f)
            }
        }
    }

    val Palette: ImageVector by lazy {
        vector("Palette") {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.9f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12.0f, 3.2f)
                curveTo(7.1f, 3.2f, 3.2f, 7.1f, 3.2f, 12.0f)
                curveTo(3.2f, 16.9f, 7.1f, 20.8f, 11.4f, 20.8f)
                curveTo(12.8f, 20.8f, 13.7f, 19.9f, 13.7f, 18.7f)
                curveTo(13.7f, 18.1f, 13.5f, 17.5f, 13.1f, 17.1f)
                curveTo(12.7f, 16.6f, 13.1f, 15.8f, 13.8f, 15.8f)
                lineTo(16.0f, 15.8f)
                curveTo(18.7f, 15.8f, 20.8f, 13.8f, 20.8f, 11.1f)
                curveTo(20.8f, 6.7f, 16.9f, 3.2f, 12.0f, 3.2f)
                close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(8.5f, 10.2f); arcTo(1.2f, 1.2f, 0f, true, true, 6.1f, 10.2f); arcTo(1.2f, 1.2f, 0f, true, true, 8.5f, 10.2f); close()
                moveTo(11.3f, 7.3f); arcTo(1.2f, 1.2f, 0f, true, true, 8.9f, 7.3f); arcTo(1.2f, 1.2f, 0f, true, true, 11.3f, 7.3f); close()
                moveTo(15.5f, 7.8f); arcTo(1.2f, 1.2f, 0f, true, true, 13.1f, 7.8f); arcTo(1.2f, 1.2f, 0f, true, true, 15.5f, 7.8f); close()
                moveTo(17.7f, 11.0f); arcTo(1.2f, 1.2f, 0f, true, true, 15.3f, 11.0f); arcTo(1.2f, 1.2f, 0f, true, true, 17.7f, 11.0f); close()
            }
        }
    }

    val ArtSync: ImageVector by lazy {
        vector("ArtSync") {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.9f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(6.2f, 4.2f)
                lineTo(17.8f, 4.2f)
                quadTo(19.8f, 4.2f, 19.8f, 6.2f)
                lineTo(19.8f, 17.8f)
                quadTo(19.8f, 19.8f, 17.8f, 19.8f)
                lineTo(6.2f, 19.8f)
                quadTo(4.2f, 19.8f, 4.2f, 17.8f)
                lineTo(4.2f, 6.2f)
                quadTo(4.2f, 4.2f, 6.2f, 4.2f)
                close()
                moveTo(4.5f, 16.5f)
                lineTo(9.8f, 11.4f)
                lineTo(13.6f, 15.0f)
                lineTo(16.2f, 12.6f)
                lineTo(19.6f, 16.0f)
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(10.0f, 8.6f); arcTo(1.3f, 1.3f, 0f, true, true, 7.4f, 8.6f); arcTo(1.3f, 1.3f, 0f, true, true, 10.0f, 8.6f); close()
            }
        }
    }

    val OfflineShield: ImageVector by lazy {
        vector("OfflineShield") {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.9f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12.0f, 3.2f)
                lineTo(19.0f, 6.2f)
                lineTo(19.0f, 11.6f)
                curveTo(19.0f, 16.2f, 15.8f, 19.8f, 12.0f, 21.0f)
                curveTo(8.2f, 19.8f, 5.0f, 16.2f, 5.0f, 11.6f)
                lineTo(5.0f, 6.2f)
                close()
                moveTo(8.8f, 12.0f)
                lineTo(11.0f, 14.2f)
                lineTo(15.4f, 9.6f)
            }
        }
    }

    val FolderScan: ImageVector by lazy {
        vector("FolderScan") {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.9f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3.6f, 6.6f)
                quadTo(3.6f, 5.0f, 5.2f, 5.0f)
                lineTo(9.4f, 5.0f)
                lineTo(11.4f, 7.2f)
                lineTo(18.8f, 7.2f)
                quadTo(20.4f, 7.2f, 20.4f, 8.8f)
                lineTo(20.4f, 17.4f)
                quadTo(20.4f, 19.0f, 18.8f, 19.0f)
                lineTo(5.2f, 19.0f)
                quadTo(3.6f, 19.0f, 3.6f, 17.4f)
                close()
            }
        }
    }

    val SpeedDial: ImageVector by lazy {
        vector("SpeedDial") {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.9f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(4.6f, 16.5f)
                arcTo(8.2f, 8.2f, 0f, true, true, 19.4f, 16.5f)
                moveTo(12.0f, 15.0f)
                lineTo(15.8f, 10.2f)
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(13.4f, 15.4f); arcTo(1.4f, 1.4f, 0f, true, true, 10.6f, 15.4f); arcTo(1.4f, 1.4f, 0f, true, true, 13.4f, 15.4f); close()
            }
        }
    }

    val Sparkles: ImageVector by lazy {
        vector("Sparkles") {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.85f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12.0f, 3.5f)
                lineTo(13.8f, 9.2f)
                lineTo(19.5f, 11.0f)
                lineTo(13.8f, 12.8f)
                lineTo(12.0f, 18.5f)
                lineTo(10.2f, 12.8f)
                lineTo(4.5f, 11.0f)
                lineTo(10.2f, 9.2f)
                close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(18.5f, 16.0f)
                lineTo(19.3f, 18.2f)
                lineTo(21.5f, 19.0f)
                lineTo(19.3f, 19.8f)
                lineTo(18.5f, 22.0f)
                lineTo(17.7f, 19.8f)
                lineTo(15.5f, 19.0f)
                lineTo(17.7f, 18.2f)
                close()
            }
        }
    }

    val Trash: ImageVector by lazy {
        vector("Trash") {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.9f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(4.5f, 6.8f); lineTo(19.5f, 6.8f)
                moveTo(9.2f, 6.8f); lineTo(9.8f, 4.4f); lineTo(14.2f, 4.4f); lineTo(14.8f, 6.8f)
                moveTo(6.8f, 6.8f); lineTo(7.6f, 18.6f)
                quadTo(7.7f, 20.0f, 9.1f, 20.0f)
                lineTo(14.9f, 20.0f)
                quadTo(16.3f, 20.0f, 16.4f, 18.6f)
                lineTo(17.2f, 6.8f)
                moveTo(10.3f, 10.2f); lineTo(10.5f, 16.5f)
                moveTo(13.7f, 10.2f); lineTo(13.5f, 16.5f)
            }
        }
    }

    val SortArrows: ImageVector by lazy {
        vector("Sort") {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.95f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(4.5f, 6.8f); lineTo(19.5f, 6.8f)
                moveTo(7.0f, 12.0f); lineTo(17.0f, 12.0f)
                moveTo(10.0f, 17.2f); lineTo(14.0f, 17.2f)
            }
        }
    }

    val InfoCircle: ImageVector by lazy {
        vector("Info") {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.9f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(20.5f, 12.0f)
                arcTo(8.5f, 8.5f, 0f, true, true, 3.5f, 12.0f)
                arcTo(8.5f, 8.5f, 0f, true, true, 20.5f, 12.0f)
                close()
                moveTo(12.0f, 11.2f); lineTo(12.0f, 16.2f)
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(13.1f, 8.1f); arcTo(1.1f, 1.1f, 0f, true, true, 10.9f, 8.1f); arcTo(1.1f, 1.1f, 0f, true, true, 13.1f, 8.1f); close()
            }
        }
    }

    val VinylDisc: ImageVector by lazy {
        vector("VinylDisc") {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.85f
            ) {
                moveTo(20.5f, 12.0f); arcTo(8.5f, 8.5f, 0f, true, true, 3.5f, 12.0f); arcTo(8.5f, 8.5f, 0f, true, true, 20.5f, 12.0f); close()
                moveTo(15.0f, 12.0f); arcTo(3.0f, 3.0f, 0f, true, true, 9.0f, 12.0f); arcTo(3.0f, 3.0f, 0f, true, true, 15.0f, 12.0f); close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(12.9f, 12.0f); arcTo(0.9f, 0.9f, 0f, true, true, 11.1f, 12.0f); arcTo(0.9f, 0.9f, 0f, true, true, 12.9f, 12.0f); close()
            }
        }
    }

    val HomeOutline: ImageVector by lazy {
        vector("HomeOutline") {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.9f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(3.8f, 10.6f)
                lineTo(12.0f, 3.8f)
                lineTo(20.2f, 10.6f)
                moveTo(5.8f, 9.4f)
                lineTo(5.8f, 19.2f)
                quadTo(5.8f, 20.4f, 7.0f, 20.4f)
                lineTo(17.0f, 20.4f)
                quadTo(18.2f, 20.4f, 18.2f, 19.2f)
                lineTo(18.2f, 9.4f)
                moveTo(10.0f, 20.4f)
                lineTo(10.0f, 14.4f)
                quadTo(10.0f, 13.4f, 11.0f, 13.4f)
                lineTo(13.0f, 13.4f)
                quadTo(14.0f, 13.4f, 14.0f, 14.4f)
                lineTo(14.0f, 20.4f)
            }
        }
    }

    val HomeFilled: ImageVector by lazy {
        vector("HomeFilled") {
            path(
                fill = SolidColor(Color.Black),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.1f,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(12.0f, 3.6f)
                lineTo(20.4f, 10.5f)
                lineTo(18.4f, 10.5f)
                lineTo(18.4f, 19.6f)
                lineTo(14.3f, 19.6f)
                lineTo(14.3f, 14.2f)
                quadTo(14.3f, 13.0f, 13.1f, 13.0f)
                lineTo(10.9f, 13.0f)
                quadTo(9.7f, 13.0f, 9.7f, 14.2f)
                lineTo(9.7f, 19.6f)
                lineTo(5.6f, 19.6f)
                lineTo(5.6f, 10.5f)
                lineTo(3.6f, 10.5f)
                close()
            }
        }
    }

    val Library: ImageVector by lazy {
        vector("Library") {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.9f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                // Stacked back frame at bottom-left
                moveTo(4.0f, 8.0f)
                lineTo(4.0f, 18.0f)
                quadTo(4.0f, 20.2f, 6.2f, 20.2f)
                lineTo(16.2f, 20.2f)
                // Main rounded music square
                moveTo(9.6f, 3.8f)
                lineTo(18.0f, 3.8f)
                quadTo(20.2f, 3.8f, 20.2f, 6.0f)
                lineTo(20.2f, 14.4f)
                quadTo(20.2f, 16.6f, 18.0f, 16.6f)
                lineTo(9.6f, 16.6f)
                quadTo(7.4f, 16.6f, 7.4f, 14.4f)
                lineTo(7.4f, 6.0f)
                quadTo(7.4f, 3.8f, 9.6f, 3.8f)
                close()
                // Musical note inside square
                moveTo(14.4f, 12.6f)
                lineTo(14.4f, 7.2f)
                curveTo(14.4f, 7.2f, 16.5f, 7.6f, 17.0f, 9.0f)
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(14.5f, 12.8f)
                arcTo(1.75f, 1.4f, -15f, isMoreThanHalf = true, isPositiveArc = true, 11.1f, 13.6f)
                arcTo(1.75f, 1.4f, -15f, isMoreThanHalf = true, isPositiveArc = true, 14.5f, 12.8f)
                close()
            }
        }
    }

    val Refresh: ImageVector by lazy {
        vector("Refresh") {
            path(
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.95f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(19.6f, 12.0f)
                arcTo(7.6f, 7.6f, 0f, isMoreThanHalf = true, isPositiveArc = true, 17.5f, 6.7f)
                moveTo(19.5f, 4.2f)
                lineTo(19.5f, 8.2f)
                lineTo(15.5f, 8.2f)
            }
        }
    }

    private inline fun vector(
        name: String,
        block: ImageVector.Builder.() -> Unit
    ): ImageVector {
        return ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply(block).build()
    }
}

@Composable
fun GlassMusicIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: Dp = 24.dp,
    glassHalo: Boolean = false
) {
    val extras = LocalKnightExtras.current
    if (!glassHalo || !extras.liquidGlassEnabled) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = tint,
            modifier = modifier.size(size)
        )
        return
    }

    val primary = MaterialTheme.colorScheme.primary
    Box(
        modifier = modifier
            .size(size + 10.dp)
            .clip(CircleShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        primary.copy(alpha = 0.22f),
                        primary.copy(alpha = 0.08f)
                    )
                )
            )
            .border(
                width = 0.8.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (extras.isDark) 0.35f else 0.75f),
                        primary.copy(alpha = 0.25f)
                    )
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(size)
        )
    }
}
