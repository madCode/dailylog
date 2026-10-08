package com.app.dailylog.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.app.dailylog.R
import com.app.dailylog.databinding.LogFileSettingsViewBinding
import com.app.dailylog.utils.FileDisplayName

/**
 * Which file the log is written to. Split out of Settings so that its controls don't compete
 * for height with the shortcut list.
 */
class LogFileSettingsFragment(
    private val viewModel: SettingsViewModel
) : Fragment() {

    private lateinit var binding: LogFileSettingsViewBinding

    companion object {
        fun newInstance(viewModel: SettingsViewModel) = LogFileSettingsFragment(viewModel)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.log_file_settings_view, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = LogFileSettingsViewBinding.bind(view)
        applySettingsInsets(view, binding.logFileToolbar)
        useDarkStatusBarIcons(requireActivity().window)

        binding.logFileToolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.fileName.text = FileDisplayName.of(requireContext(), viewModel.getFilename())
        binding.selectFileButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                // Not text/*: many providers report .md as application/octet-stream, greying it out.
                type = "*/*"
            }
            selectFileLauncher.launch(Intent.createChooser(intent, "Select a file"))
        }
    }

    override fun onResume() {
        super.onResume()
        useDarkStatusBarIcons(requireActivity().window)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        restoreThemeStatusBarIcons(requireActivity().window)
    }

    private val selectFileLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != AppCompatActivity.RESULT_OK) return@registerForActivityResult
            val selectedFileUri = result.data?.data ?: return@registerForActivityResult
            viewModel.saveFilename(selectedFileUri.toString())
            val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            requireContext().contentResolver.takePersistableUriPermission(selectedFileUri, takeFlags)
            binding.fileName.text = FileDisplayName.of(requireContext(), viewModel.getFilename())
        }
}
