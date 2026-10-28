#!/usr/bin/env sh
# One-command health check for the repository.
#   1. Appium module: offline checks (APK sanity, locator contract, config) - needs only JDK 17+ and Maven.
#   2. Android app: unit tests + debug APK build - needs the Android SDK (ANDROID_HOME or local.properties).
set -e
ROOT="$(cd "$(dirname "$0")/.." && pwd)"

echo "==> Appium test module (offline checks; device tests self-skip without an Appium server)"
(cd "$ROOT/appium-tests" && mvn -B -q test)
echo "    OK"

if [ -n "$ANDROID_HOME" ] || [ -n "$ANDROID_SDK_ROOT" ] || [ -f "$ROOT/local.properties" ]; then
  echo "==> Android app: unit tests and debug build"
  (cd "$ROOT" && ./gradlew --console=plain :app:testDebugUnitTest :app:assembleDebug)
  echo "    APK: app/build/outputs/apk/debug/app-debug.apk"
else
  echo "==> Android app: SKIPPED (no Android SDK found)"
  echo "    Install Android Studio or the SDK command-line tools, then set ANDROID_HOME and re-run."
fi
