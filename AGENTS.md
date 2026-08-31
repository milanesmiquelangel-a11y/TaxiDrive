# TaxiDrive — Base44 environment notes

## What this project is
Native **Android** app (Kotlin + Jetpack Compose + Firebase). There is NO web
frontend and NO web server — it cannot be shown in the Base44 web preview.
The deliverable is an **APK** built with Gradle.

Two near-identical Gradle modules live in the repo:
- `Taxidrive_v1/` — original
- `TaxiDrive_v2_beta/` — newer beta (this is the one we build and edit)

Application id / namespace: `kz.taxidrive.app`. Firebase project: `taxidrivekz`
(`google-services.json` is committed).

## Toolchain (very recent — 2026)
- JDK **21** (`gradle/gradle-daemon-jvm.properties` → toolchainVersion=21)
- Gradle **9.4.1** (wrapper)
- AGP **9.2.1**, Kotlin **2.2.10**, Compose BOM **2026.02.01**
- compileSdk = **36** (minorApiLevel = 1 — SDK extension model)
- minSdk 26, targetSdk 36

## How the APK is built (Base44)
`docker-compose.base44.yml` defines a one-shot `apk-builder` service built from
`TaxiDrive_v2_beta/Dockerfile.build` (eclipse-temurin:21-jdk + Android cmdline-tools,
platforms;android-36, build-tools;36.0.0). Source is bind-mounted at `/app`.

Build the debug APK:
```
docker compose -f docker-compose.base44.yml run --rm apk-builder
```
Output: `TaxiDrive_v2_beta/app/build/outputs/apk/debug/app-debug.apk`

Notes:
- The build downloads Gradle + all Maven/Google deps the first time; allow a few
  minutes. Gradle cache persists in the `apk-gradle-cache` named volume.
- The Kotlin compiler is strict: a missing `import` (e.g. `android.os.Bundle`)
  fails the build with "overrides nothing / Unresolved reference" — check imports.

## Push notifications (Firebase Cloud Messaging)
Added to `TaxiDrive_v2_beta`:
- `app/src/main/java/kz/taxidrive/app/notification/NotificationHelper.kt` — creates
  the notification channel and shows notifications.
- `app/src/main/java/kz/taxidrive/app/notification/TaxiDriveMessagingService.kt` —
  `FirebaseMessagingService` that displays incoming push messages and saves the
  registration token to the user's Firestore document (`usuarios/{uid}.fcmToken`).
- `AndroidManifest.xml` — `POST_NOTIFICATIONS` permission + the messaging service
  registered with the `com.google.firebase.MESSAGING_EVENT` intent filter.
- `MainActivity.kt` — creates the channel, requests the runtime
  `POST_NOTIFICATIONS` permission (Android 13+) and retrieves the FCM token on start.
- `model/User.kt` — added `fcmToken` field.
- `app/build.gradle.kts` — added `firebase-messaging:24.1.0` and `core-ktx:1.13.1`.

There is no backend server, so push messages are sent from the Firebase Console
(or Cloud Functions) to a device token. The client receives and displays them.

## Secrets
None required at boot. Firebase config is in the committed `google-services.json`.
No external API keys / managed services.
