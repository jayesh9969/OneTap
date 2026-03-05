# OneTap - Accessibility App Launcher

## 1. Overview

**App Name:** OneTap  
**Tagline:** Your phone, one tap away  
**Purpose:** A simple, language-independent app launcher for people who struggle with standard phone interfaces — especially non-English speakers, elderly users, or those with limited tech literacy.

---

## 2. Core Features

### 2.1 Icon Grid Launcher
- Large, customizable grid of icons (2x3 to 4x4)
- Icons represent actions: Call, WhatsApp, Camera, Calculator, UPI apps, etc.
- **No text required** — pure visual interface
- Users can rearrange icons by drag-and-drop
- Add/remove icons via long-press menu

### 2.2 Custom Trigger Mechanisms
Primary triggers (user selects one):
| Trigger | How it works |
|---------|--------------|
| **Shake** | Shake phone to open OneTap overlay |
| **Double-tap** | Double-tap anywhere on screen |
| **Floating button** | Persistent floating bubble (like Facebook chat heads) |
| **Hardware button** | Volume button combo (e.g., hold both volume keys 1s) |

*Note: Microphone always-on is NOT included to protect privacy.*

### 2.3 Voice Activation (Optional, On-Demand)
- Activates only when user explicitly opens the app
- Uses Android's built-in offline speech recognition
- **No cloud, no data leaves device**
- Supports multiple offline languages
- Simple command structure: "Open [icon name]" or "Call [contact name]"

### 2.4 Emergency Mode
- One-tap to trigger: Open camera / Call emergency contact / Flash light
- Configure in settings (which emergency actions appear)
- Large, red "SOS" button option

---

## 3. User Experience

### First Launch
1. Welcome screen with 3-step visual guide (icons only)
2. Ask to select trigger mechanism
3. Show default icon grid
4. Let user customize

### Main Interface
- Full-screen icon grid
- Swipe right → recent apps
- Swipe left → emergency panel
- Long-press any icon → edit/replace
- Pull down from top → settings

### Accessibility
- Minimum icon size: 80dp
- High contrast mode
- Supports system font scaling
- Works with TalkBack

---

## 4. Security Model

### ✅ What We Do
- All processing **local only** — no network calls
- No microphone access by default
- No data collection or analytics
- Open-source (MIT license)
- Android permissions requested: `android.permission.SYSTEM_ALERT_WINDOW` (overlay), `android.permission.BIND_ACCESSIBILITY_SERVICE` (optional, for voice), `android.permission.RECEIVE_BOOT_COMPLETED`

### ❌ What We DON'T Do
- Never collect, store, or transmit user data
- No cloud speech recognition
- No ads, no tracking
- No background microphone listening
- No overlay on top of password/banking screens

---

## 5. Technical Stack

| Component | Technology |
|-----------|------------|
| **Framework** | Android (Kotlin) |
| **Min SDK** | Android 7.0 (API 24) |
| **UI** | Jetpack Compose |
| **Architecture** | MVVM + Clean Architecture |
| **Speech** | Android SpeechRecognizer (offline) |
| **Storage** | SharedPreferences + Room (for custom icons) |
| **Build** | Gradle (Kotlin DSL) |

### Key Dependencies
- Jetpack Compose + Material 3
- AndroidX Lifecycle
- Room Database
- Hilt (dependency injection)
- Coil (icon loading)

---

## 6. File Structure (Draft)

```
app/
├── src/main/
│   ├── java/com/onetap/
│   │   ├── ui/
│   │   │   ├── launcher/      # Main icon grid screen
│   │   │   ├── settings/      # Configuration screen
│   │   │   └── components/    # Reusable UI components
│   │   ├── domain/
│   │   │   ├── model/         # Data classes
│   │   │   └── usecase/       # Business logic
│   │   ├── data/
│   │   │   └── repository/    # Local storage
│   │   └── service/
│   │       ├── TriggerService # Handles shake/double-tap
│   │       └── OverlayService  # Floating button service
│   └── res/
│       └── drawable/          # Default icon set
```

---

## 7. MVP Milestones

### Phase 1 (Week 1-2) - The Core
- [ ] Project setup with Kotlin + Compose
- [ ] Icon grid UI with default icons
- [ ] Floating button trigger (simplest to implement)
- [ ] Open apps from grid

### Phase 2 (Week 3-4) - Triggers + Customization
- [ ] Shake-to-open trigger
- [ ] Double-tap trigger
- [ ] Icon add/remove/rearrange
- [ ] Persistent settings

### Phase 3 (Week 5-6) - Voice (Optional)
- [ ] On-demand voice input
- [ ] Offline language packs
- [ ] Map voice commands to icons

### Phase 4 (Week 7) - Polish
- [ ] Emergency mode
- [ ] High contrast / accessibility modes
- [ ] First beta test

---

## 8. Future Ideas (Post-MVP)

- **Physical button accessory** — Bluetooth/b USB button
- **IR remote integration** — Use TV remote to trigger
- **Wearable support** — Open from smartwatch
- **Multi-user profiles** — Different icon sets for different people
- **Widget support** — Put OneTap on home screen

---

## 9. Naming Options

- **OneTap** — Simple, descriptive
- **Tapni** — "Tap" + "ni" (Hindi for "this")
- **IconFlow** — Flow with icons
- **Aap** — Hindi for "you/your" (also short for app)

*Recommend: **OneTap** — universal, easy to say in any language*

---

## 10. Open Questions

1. Should we support iOS? (Would need SwiftUI version later)
2. Include pre-installed icon packs for popular Indian/Asian apps?
3. How to handle app icons that aren't standard? (Allow camera icon as custom photo)
4. Localization — should UI itself be translated later, or stay icon-only?

---

*Last Updated: 2026-03-05*  
*Version: 1.0 (Draft)*
