package com.example.dailylog.ui.main

import androidx.lifecycle.ViewModel

class LogViewModel : ViewModel() {
    var cursorIndex = 0
    var logValue = "TEST LOG"
    fun getLog(): String {
        return logValue
    }

    fun saveCursorIndex(selectionStart: Int) {
        cursorIndex = selectionStart
    }

    fun getDateString(): String {
        return "DATE"
    }

    fun save(text: String) {
        logValue = text
    }
}