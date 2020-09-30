package com.example.dailylog.ui.settings

import android.os.Build
import androidx.lifecycle.ViewModel
import com.example.dailylog.repository.Repository
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

class SettingsViewModel(private var repository: Repository) : ViewModel() {

    var dateTimeFormat = repository.getDateTimeFormat()
    var filename = repository.getFilename()
    var shortcutListAdapter = ShortcutListAdapter(
        items = repository.getAllShortcuts(),
        removeCallback = { label -> repository.removeShortcut(label) },
        updatePositionCallback = { label, pos -> repository.updateShortcutPosition(label, pos)})

    fun saveDateTimeFormat(format: String): Boolean {
        return if (isValidDateTimeFormat(format)) {
            repository.setDateTimeFormat(format)
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
        repository.setFilename(filename)
        this.filename = repository.getFilename()
    }
}