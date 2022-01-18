package com.example.dailylog.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.example.dailylog.R
import com.example.dailylog.repository.Repository
import com.example.dailylog.repository.ShortcutType
import kotlinx.android.synthetic.main.bulk_add_shortcuts.view.*
import kotlinx.android.synthetic.main.create_new_shortcut.view.*

class BulkAddShortcutsDialogFragment(viewModel: ShortcutDialogViewModel) : ShortcutDialogFragment(viewModel)  {
    interface BulkAddListener {
        fun onBulkAddShortcuts(info: List<Array<String>>)
    }

    companion object {
        fun newInstance(viewModel: ShortcutDialogViewModel) = BulkAddShortcutsDialogFragment(viewModel)
    }
    private var resultLines = ArrayList<Array<String>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bulk_add_shortcuts, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        super.onViewCreated(view, savedInstanceState)

        view.btnCancelBulkShortcut.setOnClickListener {
            dismiss()
        }

        view.btnSaveBulkShortcut.setOnClickListener {
            numLabelsBeingValidated = 0
            valid = true
            clearInvalidLabelMessage()
            resultLines = ArrayList()
            val lines = view.bulkInput.text?.lines()
            lines?.forEachIndexed { index, s ->
                val regex = Regex(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)")
                val splitResults = s.split(regex = regex)
                val displayIndex = index + 1
                try {
                    viewModel.validateShortcutRow(splitResults.toTypedArray(), displayIndex)
                } catch(ex: Exception) {
                    view.bulkInputLayout.error = ex.message
                }
                resultLines.add(splitResults.toTypedArray())
            }
            submit()
        }
    }

    override fun submit() {
        if (canSubmit() && resultLines.size > 0) {
            val listener: BulkAddListener = targetFragment as BulkAddListener
            listener.onBulkAddShortcuts(resultLines)
            dismiss()
        }
    }

    override fun alertOnInvalidLabel(label: String) {
        view?.bulkInputLayout?.error = "Label '$label' already exists."
    }

    private fun clearInvalidLabelMessage() {
        view?.bulkInputLayout?.error = null
    }

    override fun savingIndicator() {
        val button = view?.btnSaveBulkShortcut
        if (numLabelsBeingValidated > 0) {
            button?.isEnabled = false
            button?.text = getString(R.string.saving)
        } else {
            button?.isEnabled = true
            button?.text = getString(R.string.save)
        }
    }
}