package com.example.dailylog.ui.settings

import android.app.Application
import androidx.lifecycle.*
import com.example.dailylog.repository.Repository
import com.example.dailylog.repository.Shortcut
import com.example.dailylog.utils.DetermineBuild
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.*

class SettingsViewModelFactory(private val application: Application, private var repository: Repository, private var build: DetermineBuild): ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SettingsViewModel(application, repository, build) as T
    }

}

class SettingsViewModel(application: Application, private var repository: Repository, private var build: DetermineBuild) : AndroidViewModel(application) {

    var dateTimeFormat = repository.getDateTimeFormat()

    fun removeCallback(label: String) = viewModelScope.launch(Dispatchers.IO) {
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
            if (build.isOreoOrGreater()) {
                DateTimeFormatter.ofPattern(format)
            } else {
                val formatter = SimpleDateFormat(format, Locale.getDefault())
                formatter.format(Date())
            }
        } catch (e: ParseException) {
            return false
        } catch (e: IllegalArgumentException) {
            return false
        }
        return true
    }

    fun saveFilename(filename: String) {
        repository.storeFilename(filename)
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

    fun getFilename(): String {
        return repository.retrieveFilename()
    }

    fun getAllShortcuts(): LiveData<List<Shortcut>> {
        return repository.getAllShortcuts()
    }

    fun updateShortcutList(it: List<Shortcut>?) {
        repository.updateShortcutList(it)
    }

    fun labelIsUnique(label: String): Boolean {
        return repository.labelIsUnique(label)
    }

    fun updateShortcutPositions(shortcuts: List<Shortcut>) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateShortcutPositions(shortcuts)
    }
}