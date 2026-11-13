package com.knightwin.music.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.knightwin.music.R
import com.knightwin.music.ui.icons.KnightIcons
import com.knightwin.music.ui.theme.GlassVariant
import com.knightwin.music.ui.theme.KnightDialog
import com.knightwin.music.ui.theme.LiquidAppBackdrop
import com.knightwin.music.ui.theme.LiquidGlassSurface

@Composable
fun AudioPermissionScreen(
    showSettingsFallback: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSystemSettings: () -> Unit
) {
    LiquidAppBackdrop(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LiquidGlassSurface(
                    shape = RoundedCornerShape(28.dp),
                    variant = GlassVariant.CARD,
                    elevation = 6.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.knightwin_emblem),
                            contentDescription = "KnightWin Music",
                            modifier = Modifier.size(104.dp).clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "KnightWin Music",
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Your purely local, offline-first music player for Android 13+.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = KnightIcons.OfflineShield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Allow audio access to scan your local songs",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onRequestPermission,
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Allow Local Music Access", modifier = Modifier.padding(vertical = 4.dp))
                        }
                        if (showSettingsFallback) {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = onOpenSystemSettings,
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Open App Permissions", modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FirstRunArtConsentDialog(
    albumCount: Int,
    onFetchOnce: () -> Unit,
    onStayOffline: () -> Unit
) {
    KnightDialog(
        onDismissRequest = onStayOffline,
        icon = {
            Icon(
                imageVector = KnightIcons.ArtSync,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(34.dp)
            )
        },
        title = {
            Text(
                text = "Fetch Missing Album Art?",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Text(
                text = "KnightWin Music scanned $albumCount local ${if (albumCount == 1) "album" else "albums"}.\n\n" +
                    "By design, this app works 100% offline. Would you like to run a one-time online fetch now to download missing album covers?\n\n" +
                    "After this, KnightWin Music stays completely offline unless you manually tap Fetch / Refresh Album Art in Settings.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(onClick = onFetchOnce, shape = RoundedCornerShape(14.dp)) {
                Text("Fetch Missing Art")
            }
        },
        dismissButton = {
            TextButton(onClick = onStayOffline) { Text("Stay 100% Offline") }
        }
    )
}
