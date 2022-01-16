package com.example.dailylog.ui.log

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.dailylog.repository.Repository
import com.example.dailylog.repository.Shortcut
import com.example.dailylog.utils.getDateString as utilsGetDateString
import java.time.Clock

class LogViewModel(var repository: Repository) : ViewModel() {
    var cursorIndex = repository.getCursorIndex()

    @VisibleForTesting
    var clock: Clock? = null // allow passing in of clock for testing purposes

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

    @RequiresApi(Build.VERSION_CODES.O)
    fun getDateString(): String {
        return if (!repository.getDateTimeFormat().isNullOrBlank()) {
            utilsGetDateString(this.clock, repository.getDateTimeFormat()!!);
        } else {
            "No date time string found. Please add one in the settings screen."
        }
    }

    fun save(text: String) {
        repository.saveToFile(text)
    }
}