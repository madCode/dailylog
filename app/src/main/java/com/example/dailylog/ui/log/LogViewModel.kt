package com.example.dailylog.ui.log

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.dailylog.repository.Repository
import com.example.dailylog.repository.Shortcut

class LogViewModel(var repository: Repository) : ViewModel() {
    var cursorIndex = repository.getCursorIndex()
    var loadedFileForFirstTime = false

    fun getLog(): String {
        return repository.readFile(!loadedFileForFirstTime)
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