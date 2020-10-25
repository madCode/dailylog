package com.example.dailylog.ui.settings

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.TypedValue
import androidx.lifecycle.*
import com.example.dailylog.R
import com.example.dailylog.repository.Repository
import com.example.dailylog.repository.Shortcut
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

class SettingsViewModelFactory(private val application: Application, private var repository: Repository, private val context: Context?, private var editCallback: (Shortcut) -> Unit): ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SettingsViewModel(application, repository, context, editCallback) as T
    }

}

class SettingsViewModel(application: Application, private var repository: Repository, context: Context?, private var editCallback: (Shortcut) -> Unit) : AndroidViewModel(application) {

    var dateTimeFormat = repository.getDateTimeFormat()
    var filename = repository.getFilename()
    private val value = TypedValue()
    var shortcutListAdapter: ShortcutListAdapter

    init {
        context?.theme?.resolveAttribute(R.attr.colorAccent, value, true)
        shortcutListAdapter = ShortcutListAdapter(
            removeCallback = { label -> removeCallback(label) },
            updatePositionCallback = { label, pos ->
                updateShortcutPositionCallback(
                    label,
                    pos
                )
            },
            editCallback = { shortcut ->
                editCallback(shortcut)
            },
            if (value.type == TypedValue.TYPE_INT_COLOR_RGB8 || value.type == TypedValue.TYPE_INT_COLOR_RGB4  || value.type == TypedValue.TYPE_INT_COLOR_ARGB4   || value.type == TypedValue.TYPE_INT_COLOR_ARGB8) value.data else -0x10000
        )
    }

    private fun updateShortcutPositionCallback(label: String, pos: Int) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateShortcutPosition(
            label,
            pos
        )
    }

    private fun removeCallback(label: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.removeShortcut(label)
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

    fun updateShortcut(label: String, text: String, cursor: Int) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateShortcut(label,text,cursor)
    }

    fun bulkAddShortcuts(shortcutsData: List<List<String>>) = viewModelScope.launch(Dispatchers.IO) {
        repository.bulkAddShortcuts(shortcutsData)
    }

    fun addShortcut(label: String, text: String, cursor: Int) = viewModelScope.launch(Dispatchers.IO) {
        repository.addShortcut(label, text, cursor)
    }
}