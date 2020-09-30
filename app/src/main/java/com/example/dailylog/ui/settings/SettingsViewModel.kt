package com.example.dailylog.ui.settings

import android.os.Build
import androidx.lifecycle.ViewModel
import com.example.dailylog.repository.FileManager
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

class SettingsViewModel(private var fileManager: FileManager) : ViewModel() {

    var dateTimeFormat = fileManager.getDateTimeFormat()
    var filename = fileManager.getFilename()

    fun saveDateTimeFormat(format: String): Boolean {
        return if (isValidDateTimeFormat(format)) {
            fileManager.setDateTimeFormat(format)
            dateTimeFormat = format
            true
        } else {
            false
        }
    }

    private fun isValidDateTimeFormat(format: String): Boolean {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                DateTimeFormatter.ofPattern(format)
            } else {
                val formatter = SimpleDateFormat(format, Locale.getDefault())
                formatter.parse(format)
            }
        } catch (e: ParseException) {
            return false
        } catch (e: IllegalArgumentException) {
            return false
        }
        return true
    }

    fun saveFilename(filename: String) {
        fileManager.setFilename(filename)
        this.filename = fileManager.getFilename()
    }
}