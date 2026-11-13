# KnightWin Music

> **Looking for a clean, free music player with zero ads and no subscriptions? KnightWin Music is the only one you need.**

**KnightWin Music** is an offline local music player for **Android 13+**, built by **KnightWin**. It plays the audio files already stored on your phone or tablet—with no accounts, no ads, no paywalls, and no background tracking.

---

## Why You'll Love It

- **100% Free & Offline** — Your music stays on your device. Zero subscriptions, zero telemetry, and zero background network activity.
- **Two Switchable UI Styles**
  - **Liquid UI**: Frosted translucent glass surfaces, an animated liquid-wave mini-player seekbar, and iOS 26/27 & iPadOS-style Settings.
  - **Material UI**: Crisp Material 3 surfaces, Material You dynamic colors, and a Samsung OneUI 8–inspired Settings screen with landscape tablet split-view.
- **Light, Dark, & True-Black OLED** — Choose Light, Dark, System, or pure `#000000` OLED Black alongside custom accent themes.
- **Beat-Reactive Now Playing** — Pick from **Spinning Disc**, **Classic Vinyl**, or **Beat Glow** styles, automatically adapting cleanly to both portrait and landscape screens.
- **Built-In Audio Tuning** — 5-band Equalizer (with presets like Pop, Rock, Jazz, Techno, Blues, and Opera or custom sliders), Room Reverb, track crossfade, playback speed (`0.5x–2.0x`), and Sleep Timer.
- **Smart Local Library** — Browse Songs, Albums, Artists, Favorites, and custom Playlists, filter out short notification clips/ringtones, and customize your Home greeter & header picture.
- **Optional Album Art Sync** — Missing a cover? Fetch missing album art on demand over HTTPS whenever you want, or keep the app completely offline forever.

---

## Download & Install (v1.0.0)

1. Go to the **Releases** page and download **`KnightWin-Music-v1.0.0.apk`**.
2. Tap the APK file on your Android 13+ device to install.
   *(If Android asks for permission to install from your browser or file manager, allow it once. Because open-source APKs downloaded outside Google Play aren't indexed by the Play Store yet, Play Protect may offer a quick "Scan" or "Install anyway" option—the release APK is cleanly signed by KnightWin.)*
3. Grant **Music and audio** access so KnightWin Music can index your local songs.

**Requirements:** Android 13 (API 33) or newer.

---

## Support the Creator

KnightWin Music is completely free and open-source with no ads or locked features. If the app makes your daily listening better and you'd like to buy the creator a coffee or help fund future updates, any support means a lot!

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/N7D7292WUD)

---

## Building from Source

1. Open the project folder (`KnightWinMusic`) in Android Studio (or build from CLI with JDK 17 + Android SDK 34).
2. *(Optional for signed release builds)* Copy `keystore.properties.example` to `keystore.properties` and point it to your release keystore.
3. Build with Gradle:
   ```bash
   ./gradlew assembleRelease   # or: gradle assembleRelease
   ```

---

## License

**KnightWin Music** — Copyright © 2026 **KnightWin**

This program is free software under the **GNU General Public License v3.0 (GPL-3.0)**. See [`LICENSE`](LICENSE) for full details.
