package com.example.dailylog.ui.welcome

import androidx.lifecycle.ViewModel
import com.example.dailylog.repository.Repository

class WelcomeViewModel(val repository: Repository, val openLog: () -> Unit) : ViewModel() {
    fun saveFilename(filename: String) {
        repository.storeFilename(filename)
    }

    fun openLogView() {
        openLog()
    }
}