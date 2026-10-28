#!/usr/bin/env sh
# Usage: ./run-tests.sh          -> offline checks + e2e tests (e2e self-skip without Appium)
#        ./run-tests.sh e2e      -> only the device tests; requires a running Appium server
set -e
cd "$(dirname "$0")"
APPIUM_URL="${APPIUM_URL:-http://127.0.0.1:4723}"

if [ "$1" = "e2e" ]; then
  if ! curl -fsS -m 3 "$APPIUM_URL/status" >/dev/null 2>&1; then
    echo "Appium server not reachable at $APPIUM_URL"
    echo "  npm install -g appium && appium driver install uiautomator2 && appium"
    echo "  then start an Android emulator (adb devices should list it)"
    exit 1
  fi
  exec mvn -q test -Dgroups=e2e -Dappium.url="$APPIUM_URL"
fi

exec mvn -q test
