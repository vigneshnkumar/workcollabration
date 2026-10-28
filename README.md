# Work Collaboration

[![CI](https://github.com/vigneshnkumar/workcollabration/actions/workflows/ci.yml/badge.svg)](https://github.com/vigneshnkumar/workcollabration/actions/workflows/ci.yml)

An Android group-collaboration app (peer and group chat, shared notes, chat reminders) with an Appium end-to-end test suite. The app was originally built in 2015–16 as a university project with a PHP/MySQL backend.

> **Backend status:** the original PHP backend was hosted on NJIT student web space and is **offline** (every endpoint now returns HTTP 404). The app builds and launches, but sign-in, sign-up and chat sync need a backend. All server URLs live in one file, [`ApiConfig.java`](app/src/main/java/com/WorkCollabration/App/net/ApiConfig.java), so you can point the app at a new deployment.

## Features

- One-to-one and group chat, with messages cached locally in SQLite
- Contacts: add, invite by email, delete
- Group creation from your contacts
- A shared notes workspace for each chat room
- Chat reminders: a message is posted to the group at a set date and time
- Profile status updates

## Repository layout

```
app/                 Android app (Gradle, Java)
  src/main/java/     Activities, sync and networking code (net/ApiClient, net/ApiConfig)
  src/test/java/     JVM unit tests (HTTP client, URL handling, reminder parsing)
appium-tests/        Appium + JUnit 5 end-to-end suite (Maven)
apk/                 Prebuilt APK from 2016 (targetSdk 21)
scripts/verify.*     Runs every check in one command
docs/uml/            Original class diagrams (ObjectAid .ucls)
```

## Quick start

**Requirements:** JDK 17+ (tested with 21) and Maven 3.9+. To build the Android app you also need the Android SDK (Android Studio installs it).

```bash
# Run everything (macOS / Linux / Git Bash)
./scripts/verify.sh

# Windows PowerShell
powershell -ExecutionPolicy Bypass -File scripts\verify.ps1
```

`verify` does the following:

1. Compiles the Appium suite and runs the checks that need no device: APK sanity, the locator contract test (every UI locator must exist in the app's layouts) and config tests. Device tests are **skipped**, not failed, when no Appium server is running.
2. If an Android SDK is found (`ANDROID_HOME` or `local.properties`), it runs the app's unit tests and builds a debug APK.

### Build the app

```bash
./gradlew :app:testDebugUnitTest   # JVM unit tests
./gradlew :app:assembleDebug       # -> app/build/outputs/apk/debug/app-debug.apk
```

Or open the project root in Android Studio and run the `app` configuration.

### Run the Appium end-to-end tests

1. Install Appium 2+ and the Android driver:
   `npm install -g appium && appium driver install uiautomator2`
2. Start an emulator (`adb devices` should list it) and run `appium`.
3. Run the device tests:

```bash
cd appium-tests
./run-tests.sh e2e            # or: powershell -File run-tests.ps1 -E2E
```

You can override any setting with `-Dkey=value` or the matching environment variable (for example `APPIUM_URL`):

| Property | Default |
| --- | --- |
| `appium.url` | `http://127.0.0.1:4723` |
| `app.path` | `app/build/outputs/apk/debug/app-debug.apk` if it has been built, otherwise `apk/WorkCollaboration.apk` |
| `test.email` / `test.password` | `test` / `test` |
| `device.name`, `platform.version`, `wait.seconds` | see `TestConfig.java` |

Tests that log in need a working backend and a real test account.

## Technologies

| Area | Stack |
| --- | --- |
| App | Java, Android SDK (min 23, target 34), SQLite, `HttpURLConnection` |
| Backend (original, offline) | PHP, MySQL |
| Build | Gradle 9 (wrapper included), Android Gradle Plugin 9 |
| Testing | JUnit 4 (unit), JUnit 5 + Appium java-client 11 + Selenium 4 (E2E), GitHub Actions CI |

## Known limitations

- **Backend offline:** see the note at the top.
- **Network calls on the UI thread:** most screens still make blocking requests on the UI thread, allowed through a `StrictMode` override. The background sync was moved to an executor, but a full move to coroutines or WorkManager is still to do.
- **Reminders:** they only fire while the app process is alive (a polling `Service`). `AlarmManager` or `WorkManager` would make them reliable.
- **Fragments:** the app uses the deprecated platform `android.app.Fragment`. Migrating to AndroidX would modernise this.
- **Image and audio sending:** the original UI never connected these (the buttons were hidden), so the unreachable upload code was removed. Media received from the server still displays.
