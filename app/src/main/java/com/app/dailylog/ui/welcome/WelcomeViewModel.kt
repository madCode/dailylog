package com.app.dailylog.ui.welcome

import androidx.lifecycle.ViewModel
import com.app.dailylog.repository.RepositoryInterface

class WelcomeViewModel(val repository: RepositoryInterface, val openLog: () -> Unit) : ViewModel() {
    fun saveFilename(filename: String) {
        repository.storeFilename(filename)
    }

    fun openLogView() {
        openLog()
    }
}