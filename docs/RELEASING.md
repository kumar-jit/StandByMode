# Releasing OpenStandby

Releases are published in three places, all free:

| Where | How it gets the app |
| --- | --- |
| **GitHub Releases** | Built and signed automatically by `.github/workflows/release.yml` when a `v*` tag is pushed |
| **IzzyOnDroid** | Picks up the signed APK from each GitHub Release |
| **F-Droid** | Builds the app from source at the tag and signs it with F-Droid's key |

Store text and the icon live in `fastlane/metadata/android/en-US/`. F-Droid and IzzyOnDroid read them from there.

---

## One-time setup

### 1. Keep the signing key safe

The release key is **not** in this repository. The maintainer keeps it outside the repo in:

```
StandByMode-signing/
├── openstandby-release.jks        the key itself
├── keystore.properties.backup     passwords + alias
└── SIGNING_KEYSTORE_BASE64.txt    the key encoded for GitHub secrets
```

Back up that folder somewhere safe (a USB stick, a password manager). **If the key is lost, users can't
update the app; they would have to uninstall and reinstall.** Never commit it.

Local release builds read `keystore.properties` in the project root (git-ignored):

```properties
storeFile=C:/path/to/openstandby-release.jks
storePassword=...
keyAlias=openstandby
keyPassword=...
```

### 2. Add the signing secrets to GitHub

In the repository on GitHub: **Settings → Secrets and variables → Actions → New repository secret**. Add these four:

| Name | Value |
| --- | --- |
| `SIGNING_KEYSTORE_BASE64` | entire contents of `SIGNING_KEYSTORE_BASE64.txt` |
| `SIGNING_STORE_PASSWORD` | `storePassword` from `keystore.properties.backup` |
| `SIGNING_KEY_ALIAS` | `openstandby` |
| `SIGNING_KEY_PASSWORD` | `keyPassword` from `keystore.properties.backup` |

Release signing certificate (SHA-256), which is public and used by IzzyOnDroid to verify APKs:

```
726b28969e8e1194062fd2219a13d37cf94cf89b7f510e3261370b74ca7b5954
```

---

## Publishing a new version

1. In `app/build.gradle.kts`, raise `versionCode` by 1 and set `versionName` (e.g. `0.2.0`).
2. Add release notes in `fastlane/metadata/android/en-US/changelogs/<versionCode>.txt` (under 500 characters).
3. Commit and push to `main`.
4. Tag and push the tag (the tag **must** be `v` + `versionName`):

   ```bash
   git tag v0.2.0
   git push origin v0.2.0
   ```

5. Watch the **Actions** tab. When the *Release* workflow finishes, the signed APK is attached to a new
   release on the **Releases** page.

F-Droid and IzzyOnDroid notice the new tag/release automatically (it can take a few days).

---

## Getting listed (one time)

### IzzyOnDroid (usually listed within days)

1. Make sure at least one GitHub Release with an APK exists.
2. Open a new issue at <https://gitlab.com/IzzyOnDroid/repo/-/issues> using the *App inclusion request* template
   (a free GitLab account is needed).
3. Give the repository URL `https://github.com/kumar-jit/StandByMode` and mention:
   offline, MIT, no trackers, fastlane metadata included, signing certificate SHA-256 above.

### F-Droid (review takes a few weeks)

1. Create a free account on <https://gitlab.com> and fork <https://gitlab.com/fdroid/fdroiddata>.
2. In your fork, add the file `metadata/io.github.kumarjit.openstandby.yml` with the contents of
   [`docs/fdroid/io.github.kumarjit.openstandby.yml`](fdroid/io.github.kumarjit.openstandby.yml).
3. Open a merge request to `fdroid/fdroiddata`, filling in the *App inclusion* template.
4. Reply to reviewer questions on the merge request. Once merged, F-Droid builds new versions from tags automatically.

Notes for F-Droid:
- F-Droid signs the app with **its own key**, so the F-Droid build and the GitHub/IzzyOnDroid build can't update
  each other. Users should stick to one source.
- The build has no proprietary dependencies and the Google dependency-info blob is disabled
  (`dependenciesInfo` in `app/build.gradle.kts`), as F-Droid requires.

### Screenshots (recommended)

Put phone screenshots in `fastlane/metadata/android/en-US/images/phoneScreenshots/` as `1.png`, `2.png`, …
Good shots: standby in landscape, night mode, the widget gallery in settings.
