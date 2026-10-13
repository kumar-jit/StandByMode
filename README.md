# OpenStandby

A free, open-source **StandBy mode for Android**, inspired by iPhone StandBy.
Put your phone on the charger and it becomes a bedside clock, calendar and notification display, right over the lock screen.

**100% offline.** The app has no internet permission (it is explicitly stripped from the manifest). Nothing you see on it ever leaves your phone.

## Features

- **Starts on charge while locked**, either as Android's built-in screen saver or through an optional auto-launch service
- **Pluggable clock widgets:** Digital, Analog, Flip, Minimal, Words ("quarter past ten"), plus any the community adds
- **Pluggable calendar widgets:** Month, Agenda, Big date, Week, with events from the phone's own calendar
- **Widget gallery** with live thumbnails and per-widget options (fonts, look-ahead, …)
- **Choose what to show:** clock + calendar, clock only, or calendar only
- **Important notifications:** messages, calls, alarms, reminders and high-priority alerts, plus any apps you mark as priority
- **Privacy by default:** on the lock screen only the app name is shown unless you enable message content
- **Night mode:** dim, red-only display for dark bedrooms
- **Burn-in protection:** content drifts a few pixels every minute on OLED screens
- 6 accent colours, 12/24-hour time, optional seconds
- Tap the clock or calendar on the standby screen to cycle styles
- Landscape: side-by-side layout. Portrait: stacked layout.

## How it starts on the charger

Android doesn't allow apps to just pop up whenever they like, so there are two ways in:

| Method | How | Reliability |
| --- | --- | --- |
| **Screen saver (recommended)** | Settings › Display › Screen saver → *OpenStandby*, and set *When to start* to **While charging** | Started by Android itself, so it works on every device |
| **Auto-launch** | In the app, turn on *Open automatically when charging* | Uses a small foreground service plus a full-screen notification. On Android 14+ you must allow *Full-screen alerts*. Some vendors (Xiaomi, Oppo, Vivo…) may also need battery optimisation turned off for the app. |

Standby closes itself when you unplug, and the back gesture closes it at any time.

## Permissions

| Permission | Why |
| --- | --- |
| Notification access | Read notifications to show the important ones (kept in memory only) |
| Calendar (read) | Show events from the on-device calendar |
| Notifications | Show the auto-launch notification |
| Full-screen intent | Open standby over the lock screen when auto-launch is on |
| Foreground service / boot | Keep auto-launch working, including after a reboot |

No internet, location, contacts, or storage permissions.

## Download

- **GitHub Releases:** grab the latest `OpenStandby-vX.Y.Z.apk` from the [Releases page](https://github.com/kumar-jit/StandByMode/releases)
- **F-Droid / IzzyOnDroid:** coming soon

Android may warn about installing apps from outside Google Play. Allow installs from your browser or file
manager, and if Play Protect shows a warning, tap *More details › Install anyway*.

Maintainers: see [docs/RELEASING.md](docs/RELEASING.md) for how releases are made.

## Building

Requirements: Android Studio (recent stable) or JDK 17+, and the Android SDK (compileSdk 37).

```bash
./gradlew assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`. To install it on a connected phone:

```bash
./gradlew installDebug
```

Run the checks that CI runs:

```bash
./gradlew lintDebug testDebugUnitTest
```

Stack: Kotlin, Jetpack Compose (Material 3), DataStore, AGP 9 / Gradle 9. minSdk 26 (Android 8.0).

## Project structure

```
app/src/main/java/io/github/kumarjit/openstandby/
├── widget/          ★ Widget API: StandbyWidget, ClockWidget, CalendarWidget, registries
│   ├── clock/       ClockState, ClockWidgets registry, builtin/ clock faces
│   ├── calendar/    CalendarState, CalendarWidgets registry, CalendarKit, builtin/ layouts
│   └── preview/     Android Studio preview helpers for widget authors
├── data/            Settings model + DataStore repository
├── calendar/        Reads the on-device CalendarContract provider
├── notifications/   NotificationListenerService + in-memory store
├── standby/         StandbyActivity, screen-saver DreamService, charging monitor, boot receiver
└── ui/
    ├── standby/     StandbyScreen layout, widget host, notification tray
    ├── settings/    Settings screen, widget gallery, permissions, priority-app picker
    └── theme/       Standby palette (incl. night mode)
```

Want to add a clock or calendar? See **[docs/WIDGETS.md](docs/WIDGETS.md)**. It's one file and one line.

## Contributing

Contributions are very welcome, whether that's new clock faces, calendar layouts, translations, bug fixes, or device testing.
See [CONTRIBUTING.md](CONTRIBUTING.md) to get started. Please follow the [Code of Conduct](CODE_OF_CONDUCT.md).

## License

[MIT](LICENSE)
