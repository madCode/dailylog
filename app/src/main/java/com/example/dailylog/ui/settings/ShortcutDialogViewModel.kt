package com.example.dailylog.ui.settings

import androidx.lifecycle.ViewModel
import com.example.dailylog.repository.Repository

class ShortcutDialogViewModel(private var repository: Repository) : ViewModel() {

    fun isLabelValid(label: String, skipUniqueCheck: Boolean = false): Boolean {
        return repository.isLabelValid(label, skipUniqueCheck)
    }

    fun isTextValid(text: String): Boolean {
        return repository.isTextValid(text)
    }

    fun validateShortcutRow(row: Array<String>, index: Int): Boolean {
        return repository.validateShortcutRow(row, index)
    }

}
