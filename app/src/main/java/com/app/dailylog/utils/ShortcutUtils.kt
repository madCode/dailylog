package com.app.dailylog.utils

import android.os.Build
import androidx.annotation.RequiresApi
import com.app.dailylog.repository.Shortcut
import com.app.dailylog.repository.ShortcutType
import java.time.Clock

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