package com.knightwin.music

import android.app.Application
import com.knightwin.music.data.media.AlbumArtFetcher
import com.knightwin.music.data.media.ArtworkLoader
import com.knightwin.music.data.media.LocalMusicRepository
import com.knightwin.music.data.prefs.SettingsRepository

class KnightWinApp : Application() {

    lateinit var settingsRepository: SettingsRepository
        private set

    lateinit var localMusicRepository: LocalMusicRepository
        private set

    lateinit var artworkLoader: ArtworkLoader
        private set

    lateinit var albumArtFetcher: AlbumArtFetcher
        private set

    override fun onCreate() {
        super.onCreate()
        settingsRepository = SettingsRepository(this)
        localMusicRepository = LocalMusicRepository(this)
        artworkLoader = ArtworkLoader(this, localMusicRepository)
        albumArtFetcher = AlbumArtFetcher(this, localMusicRepository, artworkLoader)
    }
}
