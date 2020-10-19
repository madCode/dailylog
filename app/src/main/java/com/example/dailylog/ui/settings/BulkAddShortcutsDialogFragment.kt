package com.example.dailylog.ui.settings

import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.example.dailylog.R
import com.google.android.material.slider.Slider
import kotlinx.android.synthetic.main.bulk_add_shortcuts.view.*

class BulkAddShortcutsDialogFragment : DialogFragment()  {
    interface BulkAddListener {
        fun onBulkAddShortcuts(info: List<List<String>>)
        fun labelIsUnique(label: String): Boolean
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
                val splitResults = s.split(",")
                if (splitResults.size != 3) {
                    view.bulkInputLayout.error = "Line $index: need exactly three values"
                    valid = false
                    return@forEachIndexed
                }
                val (label, text, cursor) = splitResults
                if (!isLabelValid(label)) {
                    view.bulkInputLayout.error = "Line $index: label must be unique and cannot be empty"
                    valid = false
                    return@forEachIndexed
                }
                if (!isTextValid(text)) {
                    view.bulkInputLayout.error = "Line $index: text cannot be empty"
                    valid = false
                    return@forEachIndexed
                }
                if (!isCursorValid(cursor, text)) {
                    view.bulkInputLayout.error = "Line $index: cursor must be an int. Cursor cannot be less than 0 or greater than the length of text."
                    valid = false
                    return@forEachIndexed
                }

                resultLines.add(splitResults)
            }
            if (valid) {
                val listener: BulkAddListener = targetFragment as BulkAddListener
                listener.onBulkAddShortcuts(resultLines)
                dismiss()
            }
        }
    }

    private fun isLabelValid(label: String): Boolean {
        val listener: BulkAddListener = targetFragment as BulkAddListener
        return label.isNotEmpty() && listener.labelIsUnique(label)
    }

    private fun isCursorValid(cursor: String, text: String): Boolean {
        return try {
            val value = cursor.toInt()
            value >= 0 && value <= text.length
        } catch (e: NumberFormatException) {
            false
        }
    }

    private fun isTextValid(text: String): Boolean {
        return text.isNotEmpty()
    }
}