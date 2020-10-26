package com.example.dailylog.ui.log

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.example.dailylog.R
import com.example.dailylog.repository.Repository
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class LogViewModel(var repository: Repository) : ViewModel() {
    var cursorIndex = repository.getCursorIndex()

    fun getLog(): String {
        return repository.readFile()
    }

    fun saveCursorIndex(index: Int) {
        repository.setCursorIndex(index)
        cursorIndex = index
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getDateString(): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter
            .ofPattern(repository.getDateTimeFormat())
            .withZone(ZoneId.systemDefault()) // once android has moved to JDK 9 we can remove this
        return current.format(formatter)+ "\t"
    }

    fun save(text: String) {
        repository.saveToFile(text)
    }
}