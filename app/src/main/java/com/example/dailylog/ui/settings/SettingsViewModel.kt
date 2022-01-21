package com.example.dailylog.ui.settings

import android.app.Application
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.*
import com.example.dailylog.repository.Repository
import com.example.dailylog.repository.Shortcut
import com.example.dailylog.utils.DetermineBuild
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsViewModelFactory(private val application: Application, private var repository: Repository, private var build: DetermineBuild, private var showToastOnActivity: (String) -> Unit): ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(application, repository, build, showToastOnActivity) as T
    }

}

class SettingsViewModel(application: Application, private var repository: Repository, private var build: DetermineBuild, private var showToastOnActivity: (String) -> Unit) : AndroidViewModel(application) {
    var exportFileUri: Uri? = null

    fun removeCallback(label: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.removeShortcut(label)
    }

    fun saveFilename(filename: String) {
        repository.storeFilename(filename)
    }

    fun updateShortcut(label: String, text: String, cursor: Int, position: Int, type:String) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateShortcut(label,text,cursor, position, type)
    }

    fun bulkAddShortcuts(shortcutsData: List<Array<String>>) = viewModelScope.launch(Dispatchers.IO) {
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

    fun exportShortcuts(context: Context) {
        if (this.exportFileUri == null) {
            Toast.makeText(context, "No export file selected", Toast.LENGTH_LONG).show()
            return
        }
        if (build.isOreoOrGreater()) {
            try {
                this.exportFileUri?.let { repository.exportShortcuts(it) }
            } catch(ex: Exception) {
                Toast.makeText(context, "Error: ${ex.printStackTrace()}", Toast.LENGTH_LONG).show()
                return
            }
        } else {
            Toast.makeText(context, "Need OS of Oreo or greater to export to CSV", Toast.LENGTH_LONG).show()
            return
        }
    }

    fun importShortcuts(uri: Uri) = viewModelScope.launch(Dispatchers.IO) {
        if (build.isOreoOrGreater()) {
            try {
                repository.importShortcuts(uri)
            } catch(ex: java.lang.Exception) {
                ex.message?.let { showToastOnActivity(it) }
            }
        } else {
            showToastOnActivity("Need OS of Oreo or greater to import from CSV")
        }
    }

    fun createShortcutDialogViewModel(): ShortcutDialogViewModel {
        return ShortcutDialogViewModel(repository)
    }
}