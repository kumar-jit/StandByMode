## What does this change?

<!-- One or two sentences. Link the issue: "Closes #123" -->

## Screenshots

<!-- Required for UI changes: normal + night mode, portrait + landscape if relevant -->

## Checklist

- [ ] `./gradlew assembleDebug lintDebug testDebugUnitTest` passes
- [ ] No network access, analytics or proprietary dependencies added
- [ ] Colours come from `state.colors` / `LocalStandbyColors` (night mode still works)
- [ ] New widget? Registered in `ClockWidgets`/`CalendarWidgets`, has an `@Preview`, follows [docs/WIDGETS.md](../docs/WIDGETS.md)
- [ ] Tested on a device or emulator (Android version: ___)
