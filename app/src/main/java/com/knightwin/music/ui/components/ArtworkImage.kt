package com.knightwin.music.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.knightwin.music.R
import com.knightwin.music.data.media.ArtworkLoader
import com.knightwin.music.data.media.ArtworkResult
import com.knightwin.music.ui.icons.KnightIcons
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.absoluteValue

@Composable
fun ArtworkImage(
    albumId: Long,
    artistName: String,
    albumName: String,
    artUri: Uri,
    sampleSongUri: Uri,
    artworkLoader: ArtworkLoader,
    preferEmbedded: Boolean,
    cacheEpoch: Int,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    targetSizePx: Int = 240,
    iconSize: Dp = 24.dp,
    onDominantColor: ((Color) -> Unit)? = null
) {
    var artResult by remember(albumId, artistName, albumName, preferEmbedded, cacheEpoch) {
        mutableStateOf<ArtworkResult?>(null)
    }

    LaunchedEffect(albumId, artistName, albumName, preferEmbedded, cacheEpoch) {
        val loaded = artworkLoader.loadArtwork(
            albumId = albumId,
            artistName = artistName,
            albumName = albumName,
            artUri = artUri,
            sampleSongUri = sampleSongUri,
            preferEmbedded = preferEmbedded,
            targetSizePx = targetSizePx
        )
        artResult = loaded
        if (loaded != null && onDominantColor != null) {
            onDominantColor(Color(loaded.dominantColorArgb))
        }
    }

    val bitmap = artResult?.bitmap
    Box(
        modifier = modifier.clip(shape),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = albumName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            PlaceholderArt(
                seedKey = "$artistName|$albumName|$albumId",
                iconSize = iconSize,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun PlaceholderArt(
    seedKey: String,
    iconSize: Dp,
    modifier: Modifier = Modifier
) {
    val palettes = remember {
        listOf(
            Color(0xFF152438) to Color(0xFF27B1FE),
            Color(0xFF26173A) to Color(0xFF8B5CF6),
            Color(0xFF331422) to Color(0xFFFA2D48),
            Color(0xFF102A24) to Color(0xFF10B981),
            Color(0xFF2F2110) to Color(0xFFF59E0B),
            Color(0xFF132A33) to Color(0xFF06B6D4)
        )
    }
    val idx = (seedKey.hashCode().absoluteValue) % palettes.size
    val (bgDark, accent) = palettes[idx]
    val primary = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier.background(
            Brush.linearGradient(
                colors = listOf(
                    bgDark,
                    accent.copy(alpha = 0.45f),
                    primary.copy(alpha = 0.25f)
                )
            )
        ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = KnightIcons.Songs,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.size(iconSize)
        )
    }
}

@Composable
fun HeaderLogoAvatar(
    customLogoPath: String,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    var customBitmap by remember(customLogoPath) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(customLogoPath) {
        customBitmap = if (customLogoPath.isNotBlank()) {
            withContext(Dispatchers.IO) {
                runCatching {
                    val file = File(customLogoPath)
                    if (file.exists()) BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap() else null
                }.getOrNull()
            }
        } else {
            null
        }
    }

    if (customBitmap != null) {
        Image(
            bitmap = customBitmap!!,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Image(
            painter = painterResource(id = R.drawable.knightwin_emblem),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    }
}
