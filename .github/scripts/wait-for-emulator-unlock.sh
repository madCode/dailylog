#!/usr/bin/env bash
# "Boot completed" can be reported before user 0 is unlocked and shared storage is
# mounted; tests that touch SharedPreferences or /sdcard then fail. Wait for all three
# signals, up to 5 minutes, logging progress.
set -uo pipefail
state() {
  ce=$(adb shell getprop sys.user.0.ce_available | tr -d '\r')
  user=$(adb shell dumpsys user | grep -m1 -oE 'State: [A-Z_]+' | tr -d '\r')
  if adb shell ls /sdcard/ >/dev/null 2>&1; then sdcard=ok; else sdcard=unavailable; fi
}
for i in $(seq 150); do
  state
  if [ "$ce" = "true" ] && [ "$user" = "State: RUNNING_UNLOCKED" ] && [ "$sdcard" = "ok" ]; then
    echo "Emulator ready after $((i * 2))s ($user, ce_available=$ce, sdcard=$sdcard)"
    adb shell wm dismiss-keyguard || true
    exit 0
  fi
  if [ $((i % 5)) -eq 1 ]; then echo "Waiting for emulator: $user, ce_available=$ce, sdcard=$sdcard"; fi
  sleep 2
done
echo "Emulator not ready after 5 minutes: $user, ce_available=$ce, sdcard=$sdcard" >&2
exit 1
