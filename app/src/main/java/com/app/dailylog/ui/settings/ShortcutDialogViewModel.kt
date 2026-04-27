package com.app.dailylog.ui.settings

import androidx.lifecycle.ViewModel
import com.app.dailylog.repository.RepositoryInterface

class ShortcutDialogViewModel(private var repository: RepositoryInterface) : ViewModel() {

    fun isLabelValid(label: String, excludeId: String? = null): Boolean {
        return repository.isLabelValid(label, excludeId)
    }

    fun isTextValid(text: String): Boolean {
        return repository.isTextValid(text)
    }

    fun validateShortcutRow(row: Array<String>, index: Int): Boolean {
        return repository.validateShortcutRow(row, index)
    }

}
