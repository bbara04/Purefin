
# Purefin

An open-source Jellyfin client for Android and Android TV, built with modern Android development practices.

## Features

- **Video Playback & Queue Management** - Smooth playback experience with queue management powered by Media3/ExoPlayer
- **Continue Watching** - Resume your content right where you left off
- **User Authentication** - Secure login and user account management
- **Rich Media Presentation** - Beautiful artwork and thumbnails for your media library
- **Smart Downloads** - Download your favorite content for offline viewing
- **Centralized Subtitles Management** - Advanced subtitle handling with Jellyfin plugin integration

## Screenshots

_Coming soon_

## Tech Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose
- **Media Playback:** Media3 (ExoPlayer)
- **Networking:** Jellyfin Core SDK, OkHttp
- **Dependency Injection:** Hilt
- **Local Storage:** Room Database, DataStore
- **Image Loading:** Coil
- **Navigation:** AndroidX Navigation 3
- **Serialization:** Kotlin Serialization

## Requirements

- Android 10 (API 29) or higher
- Android Studio Ladybug or newer
- JDK 11 or higher
- A Jellyfin server instance

## Installation

### Building from Source

1. Clone the repository:
```bash
git clone https://github.com/yourusername/purefin.git
cd purefin
```

2. Open the project in Android Studio

3. Sync the project with Gradle files

4. Build and run the app:
   - For Android devices: Select your device/emulator and click Run
   - For Android TV: Select an Android TV emulator or device

### APK Repository

APKs are distributed via the [APK Version Manager](https://apks.t.bbara.hu/docs) at `https://apks.t.bbara.hu`.

#### App names

| Variant | App name | Update URL |
|---------|----------|------------|
| Release (phone) | `purefin-app` | `https://apks.t.bbara.hu/apks/purefin-app/update.json` |
| Debug (phone) | `purefin-app-debug` | `https://apks.t.bbara.hu/apks/purefin-app-debug/update.json` |
| Release (TV) | `purefin-tv` | `https://apks.t.bbara.hu/apks/purefin-tv/update.json` |

#### Building

```bash
# Phone
./gradlew :app:assembleDebug           # debug APK
./gradlew :app:assembleRelease         # release APK (unsigned)

# Android TV
./gradlew :app-tv:assembleDebug        # debug APK
./gradlew :app-tv:assembleRelease      # release APK (unsigned)
```

APK outputs go to `app/build/outputs/apk/<variant>/` and `app-tv/build/outputs/apk/<variant>/`.

#### Uploading

Upload via the API at `POST /api/apps/{app_name}/upload`:

```bash
curl -X POST "https://apks.t.bbara.hu/api/apps/purefin-app-debug/upload" \
  -F "apk_file=@app/build/outputs/apk/debug/app-debug.apk" \
  -F "version_code=$(grep purefinVersionCode gradle.properties | cut -d= -f2)" \
  -F "version_name=0.1-debug"
```

The `version_code` is defined in `gradle.properties` (`purefinVersionCode` for phone, `purefinTvVersionCode` for TV). The `version_name` is `0.1` for release or `0.1-debug` for debug. Both fields are required — the server cannot auto-extract them from the APK.

## Usage

1. Launch Purefin on your Android device or Android TV
2. Enter your Jellyfin server URL
3. Log in with your Jellyfin credentials
4. Browse and enjoy your media library

## Configuration

Connect to your Jellyfin server by providing:
- Server URL (e.g., `http://192.168.1.100:8096` or `https://jellyfin.example.com`)
- Username
- Password

## Contributing

We welcome contributions! Here's how you can help:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Contribution Guidelines

- Follow Kotlin coding conventions
- Use Jetpack Compose for UI components
- Write meaningful commit messages
- Update documentation as needed

## Roadmap

- [ ] Enhanced subtitle customization
- [ ] Chromecast support
- [ ] Picture-in-Picture mode
- [ ] Live TV support
- [ ] Offline library sync
- [ ] Multi-user profiles

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- [Jellyfin](https://jellyfin.org/) - The free software media system
- All contributors who help make Purefin better

## Support

If you encounter any issues or have questions:
- Open an [issue](https://github.com/yourusername/purefin/issues)
- Check existing issues for solutions

## Disclaimer

This is an unofficial Jellyfin client and is not affiliated with or endorsed by the Jellyfin project.
