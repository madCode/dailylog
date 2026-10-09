package com.app.dailylog.ui.log

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.app.dailylog.repository.Repository
import com.app.dailylog.repository.RepositoryInterface
import com.app.dailylog.repository.Shortcut

class LogViewModel(var repository: RepositoryInterface) : ViewModel() {
    var cursorIndex = repository.getCursorIndex()
    private var loadedFileForFirstTime = false
    private var loadedFilename: String? = null

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

    fun getEditorTextSize(): Int? = repository.getEditorTextSize()

    fun smartSave(text: String): Boolean {
        return repository.saveToFile(text, false)
    }

    fun forceSave(text:String): Boolean {
        return repository.saveToFile(text, true)
    }
}