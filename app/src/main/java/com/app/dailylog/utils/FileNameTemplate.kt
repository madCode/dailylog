package com.app.dailylog.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Clock
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object FileNameTemplate {
    // Non-greedy, so two tokens on one line stay two patterns.
    val DATETIME_TOKEN = Regex("\\{DATETIME: (.*?)\\}")

    const val DEFAULT = "{DATETIME: yyyy-MM-dd}-journal.md"
    private const val DEFAULT_NAME = "-journal.md"
    private const val INVALID_CHARACTERS = "\\:*?\"<>|"

    val PRESET_DATE_PARTS = listOf(
        "{DATETIME: yyyy-MM-dd}",
        "{DATETIME: dd-MM-yyyy}",
        "{DATETIME: MM-dd-yyyy}",
        "{DATETIME: yyyy}/{DATETIME: MM-dd}",
    )

    /** Today's path relative to the log folder, e.g. `2026/10-03-journal.md`. */
    @RequiresApi(Build.VERSION_CODES.O)
    fun resolve(template: String, clock: Clock? = null): String {
        val now = if (clock == null) LocalDateTime.now() else LocalDateTime.now(clock)
        return DATETIME_TOKEN.replace(template.trim()) {
            now.format(DateTimeFormatter.ofPattern(it.groupValues[1].trim()))
        }
    }

    /** Why [template] can't name a file, or null when it can. */
    @RequiresApi(Build.VERSION_CODES.O)
    fun error(template: String, clock: Clock? = null): String? {
        val path = try {
            resolve(template, clock)
        } catch (e: IllegalArgumentException) {
            return "Invalid date pattern: ${e.message}"
        } catch (e: java.time.DateTimeException) {
            return "Invalid date pattern: ${e.message}"
        }
        val segments = path.split("/")
        return when {
            path.isEmpty() -> "Enter a file name"
            DATETIME_TOKEN.replace(template, "").contains('{') -> "Write dates as {DATETIME: pattern}"
            segments.any { it.isBlank() } -> "Folder and file names can't be empty"
            segments.any { it == "." || it == ".." } -> "\".\" and \"..\" can't be used as names"
            // Most phone storage is FAT-style and renames these, so the file could never be found again.
            segments.any { name -> name.any { it in INVALID_CHARACTERS || it.isISOControl() } } ->
                "Names can't contain $INVALID_CHARACTERS or line breaks"
            else -> null
        }
    }

    /** Replaces the dates in [template] with [datePart], keeping the text around them. */
    fun withDatePart(template: String, datePart: String): String {
        val tokens = DATETIME_TOKEN.findAll(template).toList()
        if (tokens.isEmpty()) {
            return datePart + if (template.isBlank()) DEFAULT_NAME else "-" + template.trim()
        }
        val before = template.substring(0, tokens.first().range.first)
        val after = template.substring(tokens.last().range.last + 1)
        return before + datePart + after.ifEmpty { DEFAULT_NAME }
    }
}
