package com.app.dailylog.ui.log

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.app.dailylog.repository.Repository
import com.app.dailylog.repository.Shortcut

class LogViewModel(var repository: Repository) : ViewModel() {
    var cursorIndex = repository.getCursorIndex()
    var loadedFileForFirstTime = false

    fun getLog(): String {
        val fileContents = repository.readFile(!loadedFileForFirstTime)
        loadedFileForFirstTime = true
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