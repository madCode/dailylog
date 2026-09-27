#!/usr/bin/env bash
# On API 37.0, SurfaceFlinger's RegionSampling thread (used by the gesture nav bar) aborts in
# GoldfishMapper::readFromHost, which restarts Android every few seconds. 3-button nav doesn't
# sample, so switch to it as soon as the overlay service is up. Retries across restarts.
nohup sh -c 'adb wait-for-device; until adb shell cmd overlay enable-exclusive --category com.android.internal.systemui.navbar.threebutton; do sleep 1; done' > /dev/null 2>&1 &
