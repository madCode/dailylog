package com.app.dailylog

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class ErrorDialogFragment(private val message: String): DialogFragment() {

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