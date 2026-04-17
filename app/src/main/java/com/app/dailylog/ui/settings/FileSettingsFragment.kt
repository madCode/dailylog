package com.app.dailylog.ui.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.app.dailylog.R
import com.app.dailylog.databinding.FileSettingsViewBinding

class FileSettingsFragment(
    private val viewModel: FileSettingsViewModel,
) : Fragment() {

    private lateinit var binding: FileSettingsViewBinding

    companion object {
        fun newInstance(viewModel: FileSettingsViewModel) =
            FileSettingsFragment(viewModel)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.settings_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FileSettingsViewBinding.bind(view)
        val value = TypedValue()
        context?.theme?.resolveAttribute(R.attr.colorAccent, value, true)
        renderFileNameRow()
        binding.createFileButton.setOnClickListener {
            val intent =
                Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/markdown"
                    putExtra(Intent.EXTRA_TITLE, "journal.md")
                }
            val filePickerIntent = Intent.createChooser(intent, "Create file")
            filePickerActivityResult.launch(filePickerIntent)
        }
        binding.selectFileButton.setOnClickListener {
            val intent =
                Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                }
            val filePickerIntent = Intent.createChooser(intent, "Select a file")
            filePickerActivityResult.launch(filePickerIntent)
        }
    }

    private val filePickerActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data != null) {
                val selectedFileUri = data.data
                if (selectedFileUri != null) {
                    viewModel.saveFilename(selectedFileUri.toString())
                    val contentResolver = requireContext().contentResolver
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    contentResolver.takePersistableUriPermission(selectedFileUri, takeFlags)
                    // TODO("if we didn't get the permissions we needed, ask for permission or have the user select a different file")
                }
            }
        }
    }

    private fun renderFileNameRow() {
        binding.filename.text = viewModel.getFilename()
    }
}