package com.app.dailylog.ui.log

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.dailylog.repository.RepositoryInterface
import com.app.dailylog.repository.Shortcut
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LogViewModel(
    var repository: RepositoryInterface,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    var cursorIndex = repository.getCursorIndex()
    private var loadedFileForFirstTime = false

    private val _saveComplete = MutableLiveData<Boolean>()
    val saveComplete: LiveData<Boolean> get() = _saveComplete

    private val _saveError = MutableLiveData<String>()
    val saveError: LiveData<String> get() = _saveError

    fun getLog(): String {
        val fileContents = repository.readFile(!loadedFileForFirstTime)
        loadedFileForFirstTime = true
        return fileContents
    }

    fun getAllShortcuts(): LiveData<List<Shortcut>> = repository.getAllShortcuts()

    fun saveCursorIndex(index: Int) {
        repository.setCursorIndex(index)
        cursorIndex = index
    }

    fun smartSave(text: String) {
        viewModelScope.launch(dispatcher) {
            try {
                val saved = repository.saveToFile(text, false)
                if (saved) _saveComplete.postValue(true)
            } catch (e: Exception) {
                _saveError.postValue("Could not save: ${e.message}")
            }
        }
    }

    fun forceSave(text: String) {
        viewModelScope.launch(dispatcher) {
            try {
                repository.saveToFile(text, true)
                _saveComplete.postValue(true)
            } catch (e: Exception) {
                _saveError.postValue("Could not save: ${e.message}")
            }
        }
    }
}
