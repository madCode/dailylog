package com.app.dailylog.ui.log

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.app.dailylog.repository.Constants
import com.app.dailylog.repository.RepositoryInterface
import com.app.dailylog.repository.Shortcut

class LogViewModel(var repository: RepositoryInterface) : ViewModel() {
    var cursorIndex = repository.getCursorIndex()
    private var loadedFileForFirstTime = false
    private var loadedFilename: String? = null

    /** Switches to the file the settings call for now, e.g. a new day's file. */
    fun openCurrentFile() {
        if (repository.openCurrentFile()) {
            cursorIndex = repository.getCursorIndex()
        }
        // The file may also have been created, changed or deleted elsewhere since it was last read.
        loadedFileForFirstTime = false
    }

    /** True when writing to a dated folder the app can no longer reach. */
    fun logFolderMissing(): Boolean = repository.datedFilePath != null && repository.retrieveLogFolderName() == null

    /** The dated file being edited, marked "(new)" until its first save creates it. */
    fun datedFileName(): String? = repository.datedFilePath?.let {
        if (repository.filename == Constants.FILE_NOT_CREATED) "$it (new)" else it
    }

    fun getLog(): String {
        // Picking another file in Settings keeps this ViewModel, so treat the new file as a first load:
        // otherwise the old file's saved hash makes the next smart save rewrite the new file.
        val firstLoadOfThisFile = !loadedFileForFirstTime || loadedFilename != repository.filename
        val fileContents = repository.readFile(firstLoadOfThisFile)
        loadedFileForFirstTime = true
        loadedFilename = repository.filename
        return fileContents;
    }

    fun getAllShortcuts(): LiveData<List<Shortcut>> {
        return repository.getAllShortcuts()
    }

    fun saveCursorIndex(index: Int) {
        repository.setCursorIndex(index)
        cursorIndex = index
    }

    fun smartSave(text: String): Boolean {
        return repository.saveToFile(text, false)
    }

    fun forceSave(text:String): Boolean {
        return repository.saveToFile(text, true)
    }
}