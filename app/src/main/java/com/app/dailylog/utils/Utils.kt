package com.app.dailylog.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Clock
import java.time.DateTimeException
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

interface DetermineBuildInterface {
    fun isOreoOrGreater(): Boolean
    fun isROrGreater(): Boolean
}
object DetermineBuild: DetermineBuildInterface {
    override fun isOreoOrGreater(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }
    override fun isROrGreater(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun getDateString(clock: Clock?, pattern: String): String {
    return try {
        val current: LocalDateTime = if (clock == null) {
            LocalDateTime.now()
        } else {
            LocalDateTime.now(clock)
        }
        val formatter = DateTimeFormatter
            .ofPattern(pattern)
            .withZone(ZoneId.systemDefault()) // once android has moved to JDK 9 we can remove this
        current.format(formatter)
    } catch (e: IllegalArgumentException) {
        "Issue with date time string. Please change Date Format in settings screen. Error message: " + e.message
    } catch (e: DateTimeException) {
        "Issue with date time string. Please change Date Format in settings screen. Error message: " + e.message
    }
}