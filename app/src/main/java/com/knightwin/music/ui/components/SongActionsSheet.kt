package com.knightwin.music.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.knightwin.music.data.media.ArtworkLoader
import com.knightwin.music.data.model.Playlist
import com.knightwin.music.data.model.Song
import com.knightwin.music.ui.icons.KnightIcons
import com.knightwin.music.ui.theme.KnightDialog
import com.knightwin.music.ui.theme.KnightModalSheet

@Composable
fun SongOptionsBottomSheet(
    song: Song,
    isFavorite: Boolean,
    artworkLoader: ArtworkLoader,
    preferEmbeddedArt: Boolean,
    artCacheEpoch: Int,
    onDismiss: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onShowDetails: () -> Unit,
    extraRemoveFromPlaylistLabel: String? = null,
    onExtraRemoveFromPlaylist: (() -> Unit)? = null
) {
    KnightModalSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ArtworkImage(
                    albumId = song.albumId,
                    artistName = song.artist,
                    albumName = song.album,
                    artUri = song.albumArtUri,
                    sampleSongUri = song.contentUri,
                    artworkLoader = artworkLoader,
                    preferEmbedded = preferEmbeddedArt,
                    cacheEpoch = artCacheEpoch,
                    shape = RoundedCornerShape(12.dp),
                    targetSizePx = 180,
                    modifier = Modifier.size(52.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${song.artist} • ${song.album}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SheetActionRow(
                icon = KnightIcons.SkipNext,
                label = "Play Next",
                onClick = {
                    onPlayNext()
                    onDismiss()
                }
            )
            SheetActionRow(
                icon = KnightIcons.Queue,
                label = "Add to Queue",
                onClick = {
                    onAddToQueue()
                    onDismiss()
                }
            )
            SheetActionRow(
                icon = if (isFavorite) KnightIcons.HeartFilled else KnightIcons.HeartOutline,
                label = if (isFavorite) "Remove from Favorites" else "Add to Favorites",
                onClick = {
                    onToggleFavorite()
                    onDismiss()
                }
            )
            SheetActionRow(
                icon = KnightIcons.Playlists,
                label = "Add to Playlist",
                onClick = {
                    onDismiss()
                    onAddToPlaylist()
                }
            )
            if (extraRemoveFromPlaylistLabel != null && onExtraRemoveFromPlaylist != null) {
                SheetActionRow(
                    icon = KnightIcons.Trash,
                    label = extraRemoveFromPlaylistLabel,
                    onClick = {
                        onExtraRemoveFromPlaylist()
                        onDismiss()
                    }
                )
            }
            SheetActionRow(
                icon = KnightIcons.InfoCircle,
                label = "Track Info & File Path",
                onClick = {
                    onDismiss()
                    onShowDetails()
                }
            )
        }
    }
}

@Composable
private fun SheetActionRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(18.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun AddToPlaylistDialog(
    song: Song,
    playlists: List<Playlist>,
    onDismiss: () -> Unit,
    onSelectPlaylist: (Playlist) -> Unit,
    onCreateNewPlaylist: (String) -> Unit
) {
    var creatingNew by remember { mutableStateOf(playlists.isEmpty()) }
    var newName by remember { mutableStateOf("") }

    KnightDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (creatingNew) "New Playlist" else "Add to Playlist") },
        text = {
            if (creatingNew) {
                Column {
                    Text(
                        text = "Adding \"${song.title}\"",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Playlist name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    item {
                        ListItem(
                            headlineContent = { Text("Create new playlist", fontWeight = FontWeight.SemiBold) },
                            leadingContent = {
                                Icon(
                                    imageVector = KnightIcons.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier.clickable { creatingNew = true }
                        )
                    }
                    items(playlists, key = { it.id }) { pl ->
                        ListItem(
                            headlineContent = { Text(pl.name) },
                            supportingContent = { Text("${pl.songIds.size} songs") },
                            leadingContent = {
                                Icon(
                                    imageVector = KnightIcons.Playlists,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            modifier = Modifier.clickable {
                                onSelectPlaylist(pl)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (creatingNew) {
                TextButton(
                    onClick = {
                        if (newName.isNotBlank()) {
                            onCreateNewPlaylist(newName.trim())
                            onDismiss()
                        }
                    },
                    enabled = newName.isNotBlank()
                ) {
                    Text("Create & Add")
                }
            } else {
                TextButton(onClick = onDismiss) { Text("Close") }
            }
        },
        dismissButton = {
            if (creatingNew && playlists.isNotEmpty()) {
                TextButton(onClick = { creatingNew = false }) { Text("Back") }
            } else if (creatingNew) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}

@Composable
fun SongDetailsDialog(
    song: Song,
    onDismiss: () -> Unit
) {
    KnightDialog(
        onDismissRequest = onDismiss,
        title = { Text("Track Info") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                DetailRow("Title", song.title)
                DetailRow("Artist", song.artist)
                DetailRow("Album", song.album)
                if (song.year > 0) DetailRow("Year", song.year.toString())
                if (song.trackNumber > 0) DetailRow("Track", song.trackNumber.toString())
                DetailRow("Duration", formatDuration(song.durationMs))
                DetailRow("Format", song.mimeType)
                DetailRow("Size", formatBytes(song.sizeBytes))
                DetailRow("Folder", song.folderName)
                if (song.filePath.isNotBlank()) {
                    DetailRow("Path", song.filePath)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
