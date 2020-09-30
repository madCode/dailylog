package com.example.dailylog.ui.main

import androidx.lifecycle.ViewModel

const val DATE_TIME_DEFAULT_FORMAT: String = "yyyy-LL-dd E HH:mm:ss"

class SettingsViewModel : ViewModel() {
    var filename: String = ""
    var dateTimeFormat: String = ""

    fun saveDateTimeFormat(dtFormat: String): Boolean {
        dateTimeFormat = dtFormat
        return true
    }

    fun saveFilename(string: String) {
        filename = string
    }
}