package com.app.dailylog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class ErrorDialogFragment : DialogFragment() {
    companion object {
        private const val ARG_MESSAGE = "message"

        fun newInstance(message: String) = ErrorDialogFragment().apply {
            arguments = Bundle().apply { putString(ARG_MESSAGE, message) }
        }
    }

    private val message get() = requireArguments().getString(ARG_MESSAGE)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton(R.string.ok,
                    DialogInterface.OnClickListener { _, _ ->
                        // User clicked OK button
                    })
            }
            // Set other dialog properties
            builder.setMessage(message)
                .setTitle(R.string.errorPerformingActivity)


            // Create the AlertDialog
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

}