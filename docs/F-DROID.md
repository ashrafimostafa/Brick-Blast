# F-Droid submission guide

This document lists what is already prepared in the repo and the exact steps to publish **Brick Blast** (`com.mostafa.brickblast`) on [F-Droid](https://f-droid.org/).

## Already prepared in this repository

| Item | Location |
|------|----------|
| MIT app license | [LICENSE](../LICENSE) |
| Vazirmatn font license (OFL 1.1) | [licenses/Vazirmatn-OFL.txt](../licenses/Vazirmatn-OFL.txt) |
| F-Droid build recipe | [metadata/com.mostafa.brickblast.yml](../metadata/com.mostafa.brickblast.yml) |
| Store listing text | [fastlane/metadata/android/en-US/](../fastlane/metadata/android/en-US/) |
| Phone screenshots | [fastlane/metadata/android/en-US/images/phoneScreenshots/](../fastlane/metadata/android/en-US/images/phoneScreenshots/) |
| FOSS `fdroid` product flavor | [app/build.gradle.kts](../app/build.gradle.kts) |
| No network / ad permissions in fdroid build | [app/src/fdroid/AndroidManifest.xml](../app/src/fdroid/AndroidManifest.xml) |
| Reproducible-build settings | `dependenciesInfo.includeInApk = false` in [app/build.gradle.kts](../app/build.gradle.kts) |
| Signed fdroid release APK on GitHub | [Releases](https://github.com/ashrafimostafa/Brick-Blast/releases) (`BrickBlast-<version>-fdroid.apk`) |
| CI release workflow | [.github/workflows/release.yml](../.github/workflows/release.yml) |

Current release targeted for F-Droid: **v1.3.0** (versionCode **6**).

## 1. Verify the fdroid APK builds locally (optional)

F-Droid build servers use official Gradle/Maven mirrors, not the Myket mirror in the committed sources. Simulate that locally:

```bash
cp settings.gradle.kts /tmp/settings.gradle.kts.bak
cp gradle/wrapper/gradle-wrapper.properties /tmp/gradle-wrapper.properties.bak

sed -i -e '/maven.myket.ir/d' settings.gradle.kts
sed -i -e 's@maven.myket.ir/gradle/distributions@services.gradle.org/distributions@' \
  gradle/wrapper/gradle-wrapper.properties

./gradlew :app:assembleFdroidRelease

mv /tmp/settings.gradle.kts.bak settings.gradle.kts
mv /tmp/gradle-wrapper.properties.bak gradle/wrapper/gradle-wrapper.properties
```

Output APK: `app/build/outputs/apk/fdroid/release/app-fdroid-release.apk`

## 2. Tag and publish a GitHub release

If not done yet for the version you want on F-Droid:

```bash
git tag -a v1.3.0 -m "v1.3.0"
git push origin main
git push origin v1.3.0
```

Pushing a `v*.*.*` tag triggers [.github/workflows/release.yml](../.github/workflows/release.yml), which uploads:

- `BrickBlast-1.3.0-fdroid.apk` (FOSS, no ads)
- `BrickBlast-1.3.0-store.apk` (store build with ads)

F-Droid uses the **fdroid** APK URL from the `Binaries:` field in metadata for reproducible-build verification.

Required GitHub Actions secrets: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.

## 3. Submit metadata to fdroiddata

F-Droid metadata lives in the separate [fdroiddata](https://gitlab.com/fdroid/fdroiddata) repository, not in your app repo.

1. Fork https://gitlab.com/fdroid/fdroiddata
2. Copy [metadata/com.mostafa.brickblast.yml](../metadata/com.mostafa.brickblast.yml) into your fork as `metadata/com.mostafa.brickblast.yml`
3. If this is the **first** submission, open a merge request titled: `New App: Brick Blast`
4. If the app is **already on F-Droid**, open an update MR with the new `1.3.0` build entry and updated `CurrentVersion` / `CurrentVersionCode`

Checklist for the MR description:

- [ ] App is FOSS (MIT) and builds with `fdroid` flavor only on F-Droid
- [ ] `SourceCode` and `IssueTracker` URLs are correct
- [ ] `commit: v1.3.0` tag exists on the default branch
- [ ] `Binaries` URL matches the GitHub release asset name
- [ ] `AllowedAPKSigningKeys` matches the developer-signed fdroid APK (for reproducible builds)
- [ ] Fastlane screenshots and descriptions are in the **app source repo** (F-Droid reads them from upstream)

## 4. After the merge request

- Join [#fdroid on Libera IRC](https://f-droid.org/en/docs/Connect_to_the_community/) or watch the MR for reviewer questions.
- First-time apps go through [inclusion](https://f-droid.org/en/docs/Inclusion_How-To/); updates are usually faster.
- Once merged, F-Droid builds from source. With `Binaries` + `AllowedAPKSigningKeys`, they can verify your GitHub APK matches their build.

## 5. Future updates

For each new version:

1. Bump `versionCode` / `versionName` in [app/build.gradle.kts](../app/build.gradle.kts)
2. Add a fastlane changelog: `fastlane/metadata/android/en-US/changelogs/<versionCode>.txt`
3. Update [CHANGELOG.md](../CHANGELOG.md)
4. Add a new `Builds:` entry in [metadata/com.mostafa.brickblast.yml](../metadata/com.mostafa.brickblast.yml) and set `CurrentVersion` / `CurrentVersionCode`
5. Tag `vX.Y.Z`, push, wait for GitHub release
6. Open an fdroiddata MR with the updated metadata

## Notes

- The **store** flavor (Tapsell ads) is never built by F-Droid; only `gradle: [fdroid]` is used.
- Committed Gradle files keep the Myket mirror first for developers in Iran; F-Droid `prebuild` strips it automatically.
- Do not commit `keystore.properties` or signing keys.
