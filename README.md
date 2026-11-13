# KnightWin Music

> **Looking for a clean, free music player with zero ads and no subscriptions? KnightWin Music is the only one you need.**

## Screenshots 

<img width="2400" height="1080" alt="Screenshot_20261113-114807" src="https://github.com/user-attachments/assets/74ae8bac-25cd-432f-bfba-0edae6b433eb" />
<img width="2400" height="1080" alt="Screenshot_20261113-114755" src="https://github.com/user-attachments/assets/1eac4d76-6018-4f2b-9457-5c5c0da6f16a" />
<img width="2400" height="1080" alt="Screenshot_20261113-114747" src="https://github.com/user-attachments/assets/6da67975-dd09-4898-a8bb-f725c16c8952" />
<img width="2400" height="1080" alt="Screenshot_20261113-114738" src="https://github.com/user-attachments/assets/0d6504f1-2acb-4376-bea5-8c056efb4f3e" />
<img width="1080" height="2400" alt="Screenshot_20261113-114722" src="https://github.com/user-attachments/assets/45e69ea8-51c4-46ef-9832-91ad24ce0e16" />
<img width="1080" height="2400" alt="Screenshot_20261113-114717" src="https://github.com/user-attachments/assets/147fe090-bf06-4212-b401-64eaf1b653bc" />
<img width="1080" height="2400" alt="Screenshot_20261113-114715" src="https://github.com/user-attachments/assets/c35adec2-797a-4bbe-9d6c-ab2159f34754" />
<img width="1080" height="2400" alt="Screenshot_20261113-114705" src="https://github.com/user-attachments/assets/3f65d00b-7504-488c-b8d1-650b9d9c07d7" />
<img width="1080" height="2400" alt="Screenshot_20261113-114646" src="https://github.com/user-attachments/assets/9b0f8fab-8dbf-4e2a-9ccc-753397bf8761" />
<img width="1080" height="2400" alt="Screenshot_20261113-114627" src="https://github.com/user-attachments/assets/4853ff4a-435a-4643-9eb1-5d71751f21c7" />
<img width="1080" height="2400" alt="Screenshot_20261113-114557" src="https://github.com/user-attachments/assets/092a7891-264b-437a-a171-53baf90448ae" />
<img width="1080" height="2400" alt="Screenshot_20261113-114550" src="https://github.com/user-attachments/assets/ce7655ef-fb2e-4f61-ace6-084f18df2c5b" />
<img width="1080" height="2400" alt="Screenshot_20261113-114546" src="https://github.com/user-attachments/assets/53f8bb72-08cd-47e8-9b59-a3f3aee07b9e" />
<img width="1080" height="2400" alt="Screenshot_20261113-114544" src="https://github.com/user-attachments/assets/bb039a3a-7f40-4028-8d1c-ef5c91124b90" />
<img width="1080" height="2400" alt="Screenshot_20261113-114540" src="https://github.com/user-attachments/assets/58cad86b-0b38-4e8b-97ac-95098c96a270" />
<img width="1080" height="2400" alt="Screenshot_20261113-114532" src="https://github.com/user-attachments/assets/be1eb49e-a2a0-4146-bb0a-a11f260144a9" />
<img width="1080" height="2400" alt="Screenshot_20261113-114528" src="https://github.com/user-attachments/assets/e60ee0b5-3d3f-4412-bcb8-fbf8b34deecf" />
<img width="1080" height="2400" alt="Screenshot_20261113-114517" src="https://github.com/user-attachments/assets/333e7769-e7ad-41c8-8d48-16d7ea481337" />



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
