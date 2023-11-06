package com.app.dailylog.ui.settings

import android.annotation.SuppressLint
import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import com.app.dailylog.repository.RepositoryInterface
import com.app.dailylog.repository.Shortcut
import com.app.dailylog.utils.DetermineBuildInterface
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsViewModelFactory(private var repository: RepositoryInterface,
                               private var build: DetermineBuildInterface,
                               private var showToastOnActivity: (String) -> Unit,
                               private val dispatcher: CoroutineDispatcher = Dispatchers.IO
): ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(repository, build, showToastOnActivity, dispatcher) as T
    }

}

class SettingsViewModel(
    private var repository: RepositoryInterface,
    private var build: DetermineBuildInterface,
    private var showToastOnActivity: (String) -> Unit,
    private var dispatcher: CoroutineDispatcher
) : ViewModel() {
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

    // Suppress NewApi lint here because we know we're checking for the right build
    @SuppressLint("NewApi")
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

    // Suppress NewApi lint here because we know we're checking for the right build
    @SuppressLint("NewApi")fun importShortcuts(uri: Uri) = viewModelScope.launch(dispatcher) {
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