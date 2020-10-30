package com.example.dailylog.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.example.dailylog.R
import kotlinx.android.synthetic.main.bulk_add_shortcuts.view.*

class BulkAddShortcutsDialogFragment : ShortcutDialogFragment()  {
    interface BulkAddListener {
        fun onBulkAddShortcuts(info: List<List<String>>)
    }

    companion object {
        fun newInstance() = BulkAddShortcutsDialogFragment()
    }

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
            val resultLines = ArrayList<List<String>>()
            val lines = view.bulkInput.text?.lines()
            var valid = true
            lines?.forEachIndexed { index, s ->
                val regex = Regex(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)")
                val splitResults = s.split(regex = regex)
                val displayIndex = index + 1
                if (splitResults.size != 3) {
                    view.bulkInputLayout.error = "Line $displayIndex: need exactly three values"
                    valid = false
                    return@forEachIndexed
                }
                var (label, text, cursor) = splitResults
                cursor = cursor.trim()
                text = cleanUpText(text)
                if (!isLabelValid(label)) {
                    view.bulkInputLayout.error = "Line $displayIndex: label must be unique and cannot be empty"
                    valid = false
                    return@forEachIndexed
                }
                if (!isTextValid(text)) {
                    view.bulkInputLayout.error = "Line $displayIndex: text cannot be empty"
                    valid = false
                    return@forEachIndexed
                }
                if (!isCursorValid(cursor, text)) {
                    view.bulkInputLayout.error = "Line $displayIndex: cursor must be an int. Cursor cannot be less than 0 or greater than the length of text."
                    valid = false
                    return@forEachIndexed
                }
                resultLines.add(listOf(label, text, cursor))
            }
            if (valid) {
                val listener: BulkAddListener = targetFragment as BulkAddListener
                listener.onBulkAddShortcuts(resultLines)
                dismiss()
            }
        }
    }

    private fun cleanUpText(text: String): String {
        // If it contains a comma and is surrounded by quotes, remove the quotes
        val containsComma = text.contains(',')
        val startsAndEndsWithQuotes = text.startsWith('"') && text.endsWith('"')
        return if (!containsComma || !startsAndEndsWithQuotes) {
            text
        } else {
            text.substring(1, text.length-2)
        }
    }

    private fun isCursorValid(cursor: String, text: String): Boolean {
        return try {
            val value = cursor.toInt()
            value >= 0 && value <= text.length
        } catch (e: NumberFormatException) {
            false
        }
    }
}