package com.example.dailylog.ui.settings

import android.content.Context
import android.os.Build
import android.util.TypedValue
import android.view.View
import androidx.lifecycle.ViewModel
import com.example.dailylog.R
import com.example.dailylog.repository.Repository
import com.example.dailylog.repository.Shortcut
import kotlinx.android.synthetic.main.settings_view.view.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

class SettingsViewModel(private var repository: Repository, context: Context?, private var showInstructions: () -> Unit, private var updateCallback: (Shortcut) -> Unit) : ViewModel() {

    var dateTimeFormat = repository.getDateTimeFormat()
    var filename = repository.getFilename()
    private val value = TypedValue()
    var shortcutListAdapter: ShortcutListAdapter

    init {
        context?.theme?.resolveAttribute(R.attr.colorAccent, value, true)
        shortcutListAdapter = ShortcutListAdapter(
            items = repository.getAllShortcuts(),
            removeCallback = { label -> repository.removeShortcut(label); showInstructions() },
            updatePositionCallback = { label, pos ->
                repository.updateShortcutPosition(
                    label,
                    pos
                )
            },
            updateCallback = { shortcut ->
                updateCallback(shortcut)
            },
            if (value.type == TypedValue.TYPE_INT_COLOR_RGB8 || value.type == TypedValue.TYPE_INT_COLOR_RGB4  || value.type == TypedValue.TYPE_INT_COLOR_ARGB4   || value.type == TypedValue.TYPE_INT_COLOR_ARGB8) value.data else -0x10000
        )
    }

    fun updateAdapter() {
        shortcutListAdapter.updateItems(repository.getAllShortcuts())
        showInstructions()
    }

    fun showShortcutInstructions(): Boolean {
        return !repository.areShortcuts()
    }

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