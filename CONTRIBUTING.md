# Contributing to OpenStandby

Thanks for helping out! This guide covers everything you need to send your first pull request.

## Ground rules

1. **Offline only.** OpenStandby must never use the network. PRs that add the `INTERNET` permission, analytics, ads, crash reporters, or any network library will not be merged.
2. **No proprietary dependencies** (Google Play Services, Firebase, …), so the app stays buildable for F-Droid.
3. **Privacy first.** Notification and calendar data stay in memory/on-device and are never logged or written to disk.
4. Be kind. See the [Code of Conduct](CODE_OF_CONDUCT.md).

## Getting set up

1. Fork the repo and clone your fork.
2. Open the folder in Android Studio (recent stable). It will download the right Gradle and SDK.
3. Run the app on a phone or emulator (API 26+).
4. Before pushing, run the same checks as CI:

   ```bash
   ./gradlew assembleDebug lintDebug testDebugUnitTest
   ```

## Workflow

1. Open (or comment on) an issue first for anything bigger than a small fix, so work isn't duplicated.
2. Create a branch: `feature/neon-clock`, `fix/flip-clock-12h`, …
3. Keep PRs focused, with one feature or fix per PR.
4. Include a screenshot or short recording for any UI change (light/night mode, portrait/landscape).
5. Fill in the pull-request template. CI must be green.

Commit messages: short imperative summary (`Add neon clock style`), with an optional body explaining *why*.

## Adding a clock or calendar widget

Clock faces and calendar layouts are **widgets** built on one standard interface. Extend
`ClockWidget` or `CalendarWidget`, implement `Content(state, modifier)`, and add one line to the
registry. The settings gallery, live thumbnail and your widget's own options all appear automatically.

👉 **Full guide with a copy-paste example: [docs/WIDGETS.md](docs/WIDGETS.md)**

Quick version:

```kotlin
object NeonClock : ClockWidget() {
    override val id = "neon"
    override val displayName = "Neon"
    override val author = "your-github-handle"

    @Composable
    override fun Content(state: ClockState, modifier: Modifier) {
        FitText("${state.hourText}:${state.minuteText}", state.colors.accent, modifier)
    }
}
```

…then add `NeonClock` to the end of the list in [`ClockWidgets.kt`](app/src/main/java/io/github/kumarjit/openstandby/widget/clock/ClockWidgets.kt).

## Code style

- Kotlin official style (`kotlin.code.style=official`), 4-space indent, trailing commas. See `.editorconfig`.
- Pure logic (formatting, calculations) goes in plain Kotlin objects with unit tests in `app/src/test`.

## Good first issues

- Move hard-coded UI strings into `res/values/strings.xml` and add translations
- New clock widgets (neon, binary, retro LCD, sunrise arc, …)
- New calendar widgets (year view, countdown to next event, …)
- Weather-free "world clock" widget using time zones (still offline!)
- Optional landscape-only trigger using the orientation sensor
- Tablet/foldable layout polish

## Reporting bugs

Use the bug report template and include the phone model, Android version, and how standby was started (screen saver or auto-launch).
