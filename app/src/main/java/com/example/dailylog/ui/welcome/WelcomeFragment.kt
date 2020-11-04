package com.example.dailylog.ui.welcome

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.dailylog.R
import com.example.dailylog.repository.Constants
import kotlinx.android.synthetic.main.welcome_fragment.view.*

class WelcomeFragment(private val viewModel: WelcomeViewModel) : Fragment() {

    companion object {
        fun newInstance(viewModel: WelcomeViewModel) = WelcomeFragment(viewModel)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.welcome_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        view?.createFileButton?.setOnClickListener {
            val intent =
            Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/markdown"
                putExtra("Intent.EXTRA_TITLE", "journal.md")
            }
            startActivityForResult(Intent.createChooser(intent, "Create file"), Constants.CREATE_FILE_CODE)
        }
        view?.selectFileButton?.setOnClickListener {
            val intent =
                Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                }
            startActivityForResult(Intent.createChooser(intent, "Select a file"), 111)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == Constants.CREATE_FILE_CODE || requestCode == Constants.SELECT_FILE_CODE)&& resultCode == AppCompatActivity.RESULT_OK && data != null) {
            val selectedFileUri = data.data;
            if (selectedFileUri != null) {
                viewModel.saveFilename(selectedFileUri.toString())
                val contentResolver = context!!.contentResolver
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                contentResolver.takePersistableUriPermission(selectedFileUri, takeFlags)
//                TODO("if we didn't get the permissions we needed, ask for permission or have the user select a different file")
                viewModel.openLogView()
            }
        }
    }

}