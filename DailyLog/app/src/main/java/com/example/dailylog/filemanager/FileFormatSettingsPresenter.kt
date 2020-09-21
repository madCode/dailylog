package com.example.dailylog.filemanager

import android.os.Build
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

class FileFormatSettingsPresenter(var dateTimeSaveFunction: (String) -> Unit, var filenameSaveFunction: (String) -> Unit) {

     fun saveDateTimeFormat(format: String): Boolean {
        return if (isValidDateTimeFormat(format)) {
            dateTimeSaveFunction(format)
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

}
