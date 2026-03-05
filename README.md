# OneTap - Accessibility App Launcher

A simple, language-independent app launcher with voice control.

## Features

- 📱 **Icon Grid Launcher** - Large, customizable icons (no text needed)
- 👆 **Floating Button** - Trigger from any screen
- 📳 **Shake to Open** - Shake your phone to launch
- 🔴 **Emergency Button** - Quick access to emergency contacts
- 🔒 **Privacy First** - All local, no data collection

## Screenshots

[Screenshots coming soon]

## Build

### Local Build
```bash
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

### GitHub Actions (Cloud Build)
1. Push code to GitHub
2. Go to Actions tab
3. Download APK from Artifacts

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Min SDK:** Android 7.0 (API 24)
- **Target SDK:** Android 14 (API 34)

## Permissions

- `SYSTEM_ALERT_WINDOW` - For floating button
- `QUERY_ALL_PACKAGES` - To list installed apps
- `VIBRATE` - For shake feedback
- `FOREGROUND_SERVICE` - For background triggers
- `POST_NOTIFICATIONS` - For service notification

## License

MIT License
