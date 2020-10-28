package com.example.dailylog.utils

import android.os.Build

interface DetermineBuild {
    fun isOreoOrGreater(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }
}