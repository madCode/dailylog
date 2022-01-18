package com.example.dailylog.utils

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.dailylog.repository.Shortcut
import com.example.dailylog.repository.ShortcutType
import java.time.Clock
import java.time.DateTimeException
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Objects.isNull

object DetermineBuild {
    fun isOreoOrGreater(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }
}

object ShortcutUtils {
    @RequiresApi(Build.VERSION_CODES.O)
    fun matchResultToDateString(clock: Clock?, matchResult: MatchResult): CharSequence {
        val format = matchResult.groupValues[1].trim()
        return getDateString(clock, format)
    }

    private fun replaceDateTimePatterns(value: String, clock: Clock? = null,
                                        determineBuild: DetermineBuild = DetermineBuild): String {
        return value.replace(Regex("\\{DATETIME: (.*)\\}"),
            transform = { matchResult: MatchResult ->
                if (determineBuild.isOreoOrGreater()) {
                    matchResultToDateString(clock, matchResult)
                } else {
                    "DATETIME shortcuts only supported in SDKs O and higher.\n$value"
                }
            })
    }

    fun getValueOfShortcut(shortcut: Shortcut, clock: Clock? = null,
                           determineBuild: DetermineBuild = DetermineBuild): String {
        val rawValue = shortcut.value

        when (shortcut.type) {
            ShortcutType.TEXT -> return rawValue
            ShortcutType.DATETIME -> return replaceDateTimePatterns(rawValue, clock, determineBuild)
        }
        return "type ${shortcut.type} not found. Value: $rawValue"
    }

    fun getAppliedShortcutCursorIndex(shortcut: Shortcut): Int {
        val defaultIndex = shortcut.cursorIndex
        when (shortcut.type) {
            ShortcutType.TEXT -> return defaultIndex
            ShortcutType.DATETIME -> {
                // Take the section the cursor is supposed to come after and calculate the length
                // of that once the DateTime patterns have been applies.
                return replaceDateTimePatterns(shortcut.value.slice(0 until defaultIndex)).length
            }
        }
        return defaultIndex
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