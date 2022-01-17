package com.example.dailylog.ui.log

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.dailylog.repository.Repository
import com.example.dailylog.repository.Shortcut

class LogViewModel(var repository: Repository) : ViewModel() {
    var cursorIndex = repository.getCursorIndex()

    fun getLog(): String {
        return repository.readFile()
    }

    fun getAllShortcuts(): LiveData<List<Shortcut>> {
        return repository.getAllShortcuts()
    }

    fun saveCursorIndex(index: Int) {
        repository.setCursorIndex(index)
        cursorIndex = index
    }

    fun save(text: String) {
        repository.saveToFile(text)
    }
}