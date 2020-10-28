package com.example.dailylog.ui.log

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.example.dailylog.repository.Repository
import java.time.Clock
import java.time.DateTimeException
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class LogViewModel(var repository: Repository) : ViewModel() {
    var cursorIndex = repository.getCursorIndex()

    var clock: Clock? = null // allow passing in of clock for testing purposes

    fun getLog(): String {
        return repository.readFile()
    }

    fun saveCursorIndex(index: Int) {
        repository.setCursorIndex(index)
        cursorIndex = index
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getDateString(): String {
        return try {
            if (clock == null) {
                clock = Clock.systemDefaultZone()
            }
            val current = LocalDateTime.now(clock)
            val formatter = DateTimeFormatter
                .ofPattern(repository.getDateTimeFormat())
                .withZone(ZoneId.systemDefault()) // once android has moved to JDK 9 we can remove this
            current.format(formatter) + System.lineSeparator()
        } catch (e: IllegalArgumentException) {
            "Issue with date time string. Please change Date Format in settings screen. Error message: " + e.message
        }catch (e: DateTimeException) {
            "Issue with date time string. Please change Date Format in settings screen. Error message: " + e.message
        }
    }

    fun save(text: String) {
        repository.saveToFile(text)
    }
}