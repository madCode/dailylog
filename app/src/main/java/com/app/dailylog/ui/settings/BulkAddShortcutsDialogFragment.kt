package com.app.dailylog.ui.settings

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.core.widget.doAfterTextChanged
import com.app.dailylog.R
import com.app.dailylog.databinding.BulkAddRowBinding
import com.app.dailylog.databinding.BulkAddShortcutsBinding
import com.google.android.material.textfield.TextInputLayout

class BulkAddShortcutsDialogFragment(viewModel: ShortcutDialogViewModel) : ShortcutDialogFragment(viewModel)  {
    private val listener get() = parentFragment as BulkAddListener

    lateinit var binding: BulkAddShortcutsBinding
    interface BulkAddListener {
        fun onBulkAddShortcuts(rows: List<Pair<String, String>>)
    }

    companion object {
        fun newInstance(viewModel: ShortcutDialogViewModel) = BulkAddShortcutsDialogFragment(viewModel)
        private const val NAMES = "names"
        private const val TEXTS = "texts"
        private const val SHOW_MISSING = "showMissing"
    }

    private val rows = mutableListOf<BulkAddRowBinding>()

    // Rows that were on screen when Add was tapped. Only their empty fields count as mistakes,
    // so a row still being typed into isn't flagged before it's finished.
    private val flagged = mutableSetOf<BulkAddRowBinding>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bulk_add_shortcuts, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // The dialog grows with each row, so it has to resize for the keyboard to keep the
        // focused row in view.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            dialog?.window?.setDecorFitsSystemWindows(true)
        } else {
            // You'll get a build warning that this is deprecated. Ignore it, we're only calling this
            // if it's not deprecated (Version < 30)
            dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        super.onViewCreated(view, savedInstanceState)
        binding = BulkAddShortcutsBinding.bind(view)
        saveButton = binding.btnSaveBulkShortcut

        val names = savedInstanceState?.getStringArrayList(NAMES)
        val texts = savedInstanceState?.getStringArrayList(TEXTS)
        if (names != null && texts != null && names.size == texts.size && names.isNotEmpty()) {
            names.indices.forEach { addRow(names[it], texts[it]) }
            if (savedInstanceState?.getBoolean(SHOW_MISSING) == true) flagged += rows
        } else {
            addRow()
        }

        binding.btnAddRow.setOnClickListener { addRow(focus = true) }
        binding.btnCancelBulkShortcut.setOnClickListener { dismiss() }
        binding.btnSaveBulkShortcut.setOnClickListener {
            flagged += rows
            val filled = entries().filter { (name, text) -> name.isNotEmpty() && text.isNotEmpty() }
            if (refresh() && filled.isNotEmpty()) {
                listener.onBulkAddShortcuts(filled)
                dismiss()
            }
        }
        refresh()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val entries = entries()
        outState.putStringArrayList(NAMES, ArrayList(entries.map { it.first }))
        outState.putStringArrayList(TEXTS, ArrayList(entries.map { it.second }))
        outState.putBoolean(SHOW_MISSING, flagged.isNotEmpty())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rows.clear()
        flagged.clear()
    }

    // A keyboard's suggestion often leaves a space after a word, which would let "mood " pass
    // as a new name beside "mood".
    private fun BulkAddRowBinding.name() = rowLabel.text.toString().trim()

    private fun entries() = rows.map { it.name() to it.rowText.text.toString() }

    private fun addRow(name: String = "", text: String = "", focus: Boolean = false) {
        val row = BulkAddRowBinding.inflate(layoutInflater, binding.bulkRows, true)
        // Every row's fields share IDs, so Android can't restore them; onSaveInstanceState does.
        row.rowLabel.isSaveEnabled = false
        row.rowText.isSaveEnabled = false
        row.rowLabel.setText(name)
        row.rowText.setText(text)
        row.rowLabel.doAfterTextChanged { refresh() }
        row.rowText.doAfterTextChanged { refresh() }
        row.removeRowButton.setOnClickListener { removeRow(row) }
        rows += row
        if (focus) row.rowLabel.requestFocus()
        refresh()
    }

    private fun removeRow(row: BulkAddRowBinding) {
        binding.bulkRows.removeView(row.root)
        rows -= row
        flagged -= row
        if (rows.isEmpty()) addRow(focus = true)
        refresh()
    }

    /**
     * Shows each row's mistakes and sets up the Add button. Add stays enabled for a half-filled
     * row so that tapping it can say what's missing; returns false if anything is wrong.
     */
    private fun refresh(): Boolean {
        val seen = mutableSetOf<String>()
        var complete = 0
        var started = false
        var mistakes = false
        rows.forEachIndexed { index, row ->
            val name = row.name()
            val text = row.rowText.text.toString()
            val blank = name.isEmpty() && text.isEmpty()
            val showMissing = row in flagged && !blank
            val nameError = when {
                name.isEmpty() -> if (showMissing) R.string.emptyNameError else null
                !viewModel.isLabelValid(name) -> R.string.duplicateNameError
                !seen.add(name) -> R.string.duplicateRowError
                else -> null
            }
            val textError = if (showMissing && !viewModel.isTextValid(text)) R.string.emptyTextError else null
            row.rowLabelLayout.setErrorIfChanged(nameError)
            row.rowTextLayout.setErrorIfChanged(textError)
            row.removeRowButton.contentDescription = getString(R.string.bulk_add_remove_row, index + 1)
            if (nameError != null || textError != null) mistakes = true
            if (name.isNotEmpty() && text.isNotEmpty()) complete++
            if (!blank) started = true
        }
        binding.btnSaveBulkShortcut.text =
            if (complete > 0) getString(R.string.bulk_add_count, complete) else getString(R.string.bulk_add_none)
        binding.btnSaveBulkShortcut.isEnabled = started && !mistakes
        return !mistakes
    }

    // Setting the same error again would make TalkBack announce it on every keystroke.
    private fun TextInputLayout.setErrorIfChanged(id: Int?) {
        val message = id?.let { getString(it) }
        if (error?.toString() != message) error = message
    }
}
