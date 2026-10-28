#!/usr/bin/env bash
# Drives the debug APK on a running emulator and captures README demo media:
# numbered screenshots plus a screen recording (demo.mp4).
# Usage: scripts/demo-capture.sh <output-dir>
# Needs adb on PATH and one booted device. Used by .github/workflows/demo.yml.
set -euo pipefail

OUT="${1:-demo-output}"
APK="${APK:-app/build/outputs/apk/debug/app-debug.apk}"
PKG="com.sheikbro.onlinechat"
mkdir -p "$OUT"

# --- helpers ------------------------------------------------------------------------------

ui_dump() {
  adb shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1 || true
  adb exec-out cat /sdcard/ui.xml 2>/dev/null || true
}

# Prints "x y" for the centre of the view with the given resource id, or nothing.
center_of() {
  ui_dump | grep -o "<node [^>]*resource-id=\"$PKG:id/$1\"[^>]*>" | head -1 \
    | grep -o 'bounds="\[[0-9]*,[0-9]*\]\[[0-9]*,[0-9]*\]"' \
    | sed -E 's/bounds="\[([0-9]+),([0-9]+)\]\[([0-9]+),([0-9]+)\]"/\1 \2 \3 \4/' \
    | awk '{ printf "%d %d\n", ($1+$3)/2, ($2+$4)/2 }'
}

wait_for() {
  for _ in $(seq 1 40); do
    if [ -n "$(center_of "$1")" ]; then return 0; fi
    sleep 0.5
  done
  echo "Timed out waiting for view id/$1" >&2
  adb exec-out screencap -p > "$OUT/timeout-$1.png" || true
  exit 1
}

tap_id() {
  wait_for "$1"
  # shellcheck disable=SC2046
  adb shell input tap $(center_of "$1")
}

type_into() {
  tap_id "$1"
  adb shell input text "$2"
}

keyboard_shown() {
  adb shell dumpsys input_method | grep -q "mInputShown=true"
}

# BACK closes the soft keyboard; only send it while the keyboard is up so it never leaves the screen.
hide_keyboard() {
  for _ in 1 2 3; do
    keyboard_shown || return 0
    adb shell input keyevent 4
    sleep 0.6
  done
}

shot() {
  adb exec-out screencap -p > "$OUT/$1.png"
  echo "captured $1.png"
}

# --- setup --------------------------------------------------------------------------------

adb install -r -g "$APK"
# Clean status bar (fixed clock, full battery, no notifications) for nicer captures.
adb shell settings put global sysui_demo_allowed 1
adb shell am broadcast -a com.android.systemui.demo -e command enter >/dev/null
adb shell am broadcast -a com.android.systemui.demo -e command clock -e hhmm 1200 >/dev/null
adb shell am broadcast -a com.android.systemui.demo -e command battery -e level 100 -e plugged false >/dev/null
adb shell am broadcast -a com.android.systemui.demo -e command notifications -e visible false >/dev/null

adb shell rm -f /sdcard/demo.mp4
adb shell screenrecord --bit-rate 6000000 /sdcard/demo.mp4 &
RECORDER=$!
sleep 1

# --- flow ---------------------------------------------------------------------------------

# 1. Splash screen (shown for one second)
# -W waits until the activity has drawn, so this captures the app's own splash layout
# rather than the system launch screen.
adb shell am start -W -n "$PKG/com.WorkCollabration.App.MainActivity" >/dev/null
sleep 0.3
shot 01-splash

# 2. Login screen
wait_for editEmail
sleep 0.5
shot 02-login

# 3. Submitting an empty form is rejected before any network call
tap_id signin
sleep 1.2 # toasts are drawn by System UI and appear with a short delay
shot 03-login-validation
sleep 2

# 4. Sign-up screen with inline validation
tap_id signup
wait_for editname
sleep 0.8
type_into editname "Alex"
type_into editemail "not-an-email"
type_into editpassword "secret12"
hide_keyboard
tap_id signup
sleep 0.5
# The error message is only shown while the field has focus.
tap_id editemail
sleep 0.5
hide_keyboard
sleep 0.5
shot 04-signup-validation
sleep 1.5

# 5. Back to login; a real sign-in attempt shows the offline-backend message instead of crashing
tap_id cancel
wait_for editEmail
sleep 0.6
type_into editEmail "demo@example.com"
type_into editPassword "demo1234"
hide_keyboard
tap_id signin
sleep 1.5
shot 05-login-offline
sleep 3

# --- finish -------------------------------------------------------------------------------

adb shell pkill -INT screenrecord || true
wait "$RECORDER" 2>/dev/null || true
sleep 2
adb pull /sdcard/demo.mp4 "$OUT/demo.mp4"
adb shell am broadcast -a com.android.systemui.demo -e command exit >/dev/null
ls -la "$OUT"
