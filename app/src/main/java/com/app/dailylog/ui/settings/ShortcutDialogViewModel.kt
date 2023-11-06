package com.app.dailylog.ui.settings

import androidx.lifecycle.ViewModel
import com.app.dailylog.repository.RepositoryInterface

class ShortcutDialogViewModel(private var repository: RepositoryInterface) : ViewModel() {

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
