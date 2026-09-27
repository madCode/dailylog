#!/usr/bin/env bash
nohup sh -c 'adb wait-for-device && adb logcat -b all > emulator-logcat.txt' > /dev/null 2>&1 &
