package com.app.dailylog.ui.settings

import android.os.Bundle
import android.view.*
import com.app.dailylog.R
import com.app.dailylog.utils.DetermineBuild
import kotlinx.android.synthetic.main.bulk_add_shortcuts.view.*

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
        if (!DetermineBuild.isROrGreater()) {
            // We don't seem to need to worry about keeping above
            // the keyboard in the dialog anyway if we're at API 30.
            dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
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