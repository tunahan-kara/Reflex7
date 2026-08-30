# Reflex7 Google Play Release Checklist

## Local release preparation

- [x] Set `applicationId` to `com.tunahankara.reflex7`.
- [x] Set version to `versionCode 1` / `versionName 1.0.0`.
- [x] Build against API 36 and target API 36.
- [x] Confirm there are no declared permissions.
- [x] Enable release R8 optimization and resource shrinking.
- [x] Configure an untracked upload key.
- [ ] Back up the upload keystore and its credentials in a secure location.
- [ ] Install and smoke-test the signed release APK on physical devices.
- [ ] Review the generated AAB with Play Console's internal testing track.

## Play Console setup

- [ ] Create the Reflex7 app using the exact package name `com.tunahankara.reflex7`.
- [ ] Enroll in Play App Signing and use the local Reflex7 key as the upload key.
- [ ] Complete app name, descriptions, category, tags, and developer contact details.
- [ ] Upload the 512×512 high-resolution icon.
- [ ] Upload compliant phone screenshots.
- [ ] Create and upload the feature graphic.
- [ ] Add a public privacy-policy URL if required by the selected declarations. Verify in Play Console.
- [ ] Complete Data Safety using the behavior documented in `PRIVACY_POLICY.md`.
- [ ] Complete Content Rating.
- [ ] Complete Target audience and content. Verify any Families Policy obligations in Play Console.
- [ ] Declare that the app contains no ads.
- [ ] Declare that the app has no restricted app-access flow or login.
- [ ] Complete the app-content, government-app, financial-features, and health declarations shown for the account. Verify in Play Console.
- [ ] Upload `app-release.aab` to Internal testing.
- [ ] Review automated pre-launch reports and device compatibility results.
- [ ] Complete closed testing if required for this developer account. Verify in Play Console.
- [ ] Promote the verified build to production and use a staged rollout where appropriate.

## Before every later release

- [ ] Increment `versionCode`; never reuse or decrease it.
- [ ] Update `versionName` and release notes.
- [ ] Run `gradlew.bat clean test lint assembleRelease bundleRelease`.
- [ ] Run `git diff --check` and review the release diff.
- [ ] Recheck permissions, SDKs, dependencies, Data Safety, privacy policy, and content declarations.
- [ ] Preserve the upload key and never commit signing credentials.
