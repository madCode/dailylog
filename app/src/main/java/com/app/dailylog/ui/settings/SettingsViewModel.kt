package com.app.dailylog.ui.settings

import android.app.Application
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.*
import com.app.dailylog.repository.Repository
import com.app.dailylog.repository.Shortcut
import com.app.dailylog.utils.DetermineBuild
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsViewModelFactory(private val application: Application,
                               private var repository: Repository,
                               private var build: DetermineBuild,
                               private var showToastOnActivity: (String) -> Unit,
                               private val dispatcher: CoroutineDispatcher = Dispatchers.IO
): ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(application, repository, build, showToastOnActivity, dispatcher) as T
    }

}

class SettingsViewModel(
    application: Application,
    private var repository: Repository,
    private var build: DetermineBuild,
    private var showToastOnActivity: (String) -> Unit,
    private var dispatcher: CoroutineDispatcher
) : AndroidViewModel(application) {
    var exportFileUri: Uri? = null

    fun removeCallback(label: String) = viewModelScope.launch(dispatcher) {
        repository.removeShortcut(label)
    }

    fun saveFilename(filename: String) {
        repository.storeFilename(filename)
    }

    fun updateShortcut(label: String, text: String, cursor: Int, position: Int, type:String) = viewModelScope.launch(dispatcher) {
        repository.updateShortcut(label,text,cursor, position, type)
    }

    fun bulkAddShortcuts(shortcutsData: List<Array<String>>) = viewModelScope.launch(dispatcher) {
        repository.bulkAddShortcuts(shortcutsData)
    }

    fun addShortcut(label: String, text: String, cursor: Int, type: String) = viewModelScope.launch(dispatcher) {
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

    fun updateShortcutPositions(shortcuts: List<Shortcut>) = viewModelScope.launch(dispatcher) {
        repository.updateShortcutPositions(shortcuts)
    }

    fun exportShortcuts(): Error? {
        if (this.exportFileUri == null) {
            return Error("No export file selected")
        }
        if (build.isOreoOrGreater()) {
            try {
                this.exportFileUri?.let { repository.exportShortcuts(it) }
            } catch(ex: Exception) {
                return Error("Error: ${ex.printStackTrace()}")
            }
        } else {
            return Error("Need OS of Oreo or greater to export to CSV")
        }
        return null
    }

    fun importShortcuts(uri: Uri) = viewModelScope.launch(dispatcher) {
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