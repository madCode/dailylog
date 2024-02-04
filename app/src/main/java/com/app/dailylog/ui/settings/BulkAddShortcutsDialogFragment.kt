package com.app.dailylog.ui.settings

import android.os.Bundle
import android.view.*
import com.app.dailylog.R
import com.app.dailylog.utils.DetermineBuild
import com.app.dailylog.databinding.BulkAddShortcutsBinding

class BulkAddShortcutsDialogFragment(viewModel: ShortcutDialogViewModel, private val listener: BulkAddListener) : ShortcutDialogFragment(viewModel)  {
    lateinit var binding: BulkAddShortcutsBinding
    interface BulkAddListener {
        fun onBulkAddShortcuts(info: List<Array<String>>)
    }

    companion object {
        fun newInstance(viewModel: ShortcutDialogViewModel, listener: BulkAddListener) = BulkAddShortcutsDialogFragment(viewModel, listener)
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
            // You'll get a build warning that this is deprecated. Ignore it, we're only calling this
            // if it's not deprecated (Version < 30)
            dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        super.onViewCreated(view, savedInstanceState)
        binding = BulkAddShortcutsBinding.bind(view)
        saveButton = binding.btnSaveBulkShortcut

        binding.btnCancelBulkShortcut.setOnClickListener {
            dismiss()
        }

        binding.btnSaveBulkShortcut.setOnClickListener {
            numLabelsBeingValidated = 0
            valid = true
            clearInvalidLabelMessage()
            resultLines = ArrayList()
            val lines = binding.bulkInput.text?.lines()
            lines?.forEachIndexed { index, s ->
                val regex = Regex(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)")
                val splitResults = s.split(regex = regex)
                val displayIndex = index + 1
                try {
                    viewModel.validateShortcutRow(splitResults.toTypedArray(), displayIndex)
                } catch(ex: Exception) {
                    binding.bulkInputLayout.error = ex.message
                }
                resultLines.add(splitResults.toTypedArray())
            }
            submit()
        }
    }

    override fun submit() {
        if (canSubmit() && resultLines.size > 0) {
            listener.onBulkAddShortcuts(resultLines)
            dismiss()
        }
    }

    override fun alertOnInvalidLabel(label: String) {
        binding.bulkInputLayout.error = "Label '$label' already exists."
    }

    private fun clearInvalidLabelMessage() {
        binding.bulkInputLayout.error = null
    }
}