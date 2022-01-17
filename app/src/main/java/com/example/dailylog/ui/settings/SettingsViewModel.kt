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

    fun removeCallback(label: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.removeShortcut(label)
    }

    fun saveFilename(filename: String) {
        repository.storeFilename(filename)
    }

    fun updateShortcut(label: String, text: String, cursor: Int, position: Int, type:String) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateShortcut(label,text,cursor, position, type)
    }

    fun bulkAddShortcuts(shortcutsData: List<List<String>>) = viewModelScope.launch(Dispatchers.IO) {
        repository.bulkAddShortcuts(shortcutsData)
    }

    fun addShortcut(label: String, text: String, cursor: Int, type: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.addShortcut(label, text, cursor, type)
    }

    fun getFilename(): String {
        return repository.retrieveFilename()
    }

    fun getAllShortcuts(): LiveData<List<Shortcut>> {
        return repository.getAllShortcuts()
    }

    fun labelExists(label: String): LiveData<Boolean> {
        return repository.labelExists(label)
    }

    fun updateShortcutPositions(shortcuts: List<Shortcut>) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateShortcutPositions(shortcuts)
    }
}