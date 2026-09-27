#!/usr/bin/env bash
# Streams the emulator's logcat to emulator-logcat.txt from boot until the emulator exits.
nohup sh -c 'adb wait-for-device && adb logcat -b all > emulator-logcat.txt' > /dev/null 2>&1 &
