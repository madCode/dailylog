package com.app.dailylog.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.TypedValue
import android.widget.PopupMenu
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.dailylog.R
import com.app.dailylog.repository.Shortcut
import com.app.dailylog.utils.DetermineBuild
import com.app.dailylog.utils.FileDisplayName
import com.app.dailylog.databinding.SettingsViewBinding

class SettingsFragment(
    private val viewModel: SettingsViewModel,
    private val openLogFileSettings: () -> Unit = {}
) : Fragment(),
    AddShortcutDialogFragment.AddShortcutDialogListener,
    BulkAddShortcutsDialogFragment.BulkAddListener,
    EditShortcutDialogFragment.EditShortcutDialogListener,
    ShortcutDialogListener {

    private var shortcutsLiveData: LiveData<List<Shortcut>> = viewModel.getAllShortcuts()
    private lateinit var adapter: ShortcutListAdapter
    private lateinit var binding: SettingsViewBinding

    companion object {
        fun newInstance(viewModel: SettingsViewModel, openLogFileSettings: () -> Unit = {}) =
            SettingsFragment(viewModel, openLogFileSettings)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.settings_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = SettingsViewBinding.bind(view)
        
        applySettingsInsets(view, binding.settingsToolbar)
        useDarkStatusBarIcons(requireActivity().window)
        
        val value = TypedValue()
        context?.theme?.resolveAttribute(R.attr.colorAccent, value, true)
        adapter = ShortcutListAdapter(
            removeCallback = { label -> viewModel.removeCallback(label) },
            updateShortcutPositions = { shortcuts ->
                viewModel.updateShortcutPositions(
                    shortcuts
                )
            },
            editCallback = { shortcut ->
                onEdit(shortcut)
            },
            cursorColor = if (value.type == TypedValue.TYPE_INT_COLOR_RGB8 || value.type == TypedValue.TYPE_INT_COLOR_RGB4 || value.type == TypedValue.TYPE_INT_COLOR_ARGB4 || value.type == TypedValue.TYPE_INT_COLOR_ARGB8) value.data else -0x10000
        )
        shortcutsLiveData.observe(viewLifecycleOwner, Observer { shortcuts ->
            // Update the cached copy of the words in the adapter.
            shortcuts.let {
                adapter.updateItems(it)
                renderShortcutInstructions()
            }
        })
        renderLogFileRow()
        renderShortcutList()
        binding.addShortcutButton.setOnClickListener {
            val addDialog: AddShortcutDialogFragment =
                AddShortcutDialogFragment.newInstance(
                    viewModel.createShortcutDialogViewModel()
                )
            addDialog.show(childFragmentManager, "fragment_add_shortcut")
        }
        binding.addShortcutButton.setOnLongClickListener {
            bulkAddShortcuts()
            return@setOnLongClickListener true
        }
        binding.settingsToolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.shortcutMenuButton.setOnClickListener { anchor ->
            PopupMenu(requireContext(), anchor).apply {
                menuInflater.inflate(R.menu.shortcut_options_menu, menu)
                setOnMenuItemClickListener { menuItem -> onMenuItem(menuItem) }
            }.show()
        }
    }

    override fun onResume() {
        super.onResume()
        useDarkStatusBarIcons(requireActivity().window)
        // The file can change on the sub-screen, so re-read it when we come back.
        binding.logFileValue.text = FileDisplayName.of(requireContext(), viewModel.getFilename())
    }

    private val selectLegacyShortcutFileLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK && result.data != null) {
                val selectedFileUri = result.data?.data
                if (selectedFileUri != null) {
                    viewModel.importShortcutsLegacy(selectedFileUri)
                    val contentResolver = requireContext().contentResolver
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    contentResolver.takePersistableUriPermission(selectedFileUri, takeFlags)
                    //                TODO("if we didn't get the permissions we needed, ask for permission or have the user select a different file")
                }
            }
        }

    private val selectShortcutFileLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK && result.data != null) {
                val selectedFileUri = result.data?.data
                if (selectedFileUri != null) {
                    viewModel.importShortcuts(selectedFileUri)
                    val contentResolver = requireContext().contentResolver
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    contentResolver.takePersistableUriPermission(selectedFileUri, takeFlags)
                    //                TODO("if we didn't get the permissions we needed, ask for permission or have the user select a different file")
                }
            }
        }

    private fun bulkAddShortcuts() {
        val addBulkDialog: BulkAddShortcutsDialogFragment =
            BulkAddShortcutsDialogFragment.newInstance(
                viewModel.createShortcutDialogViewModel()
            )
        addBulkDialog.show(childFragmentManager, "fragment_bulk_add")
    }

    private fun selectImportFileLegacyCSV() {
        val intent =
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/*"
            }
        selectLegacyShortcutFileLauncher.launch(
            Intent.createChooser(intent, "Select file")
        )
    }

    private val selectExportFileLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK && result.data != null) {
                val selectedFileUri = result.data?.data
                if (selectedFileUri != null) {
                    viewModel.exportFileUri = selectedFileUri
                    val contentResolver = requireContext().contentResolver
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    contentResolver.takePersistableUriPermission(selectedFileUri, takeFlags)
                    
                    // Perform the actual JSON export after file selection
                    val error = viewModel.exportShortcuts()
                    if (error != null) {
                        Toast.makeText(
                            requireContext(),
                            error.message,
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Exported shortcuts to JSON successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

    private fun selectExportFile() {
        val intent =
            Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
                putExtra(Intent.EXTRA_TITLE, "shortcuts.json")
            }
        if (DetermineBuild.isOreoOrGreater()) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse("/Documents"));
        }
        selectExportFileLauncher.launch(
            Intent.createChooser(intent, "Create JSON file"),
        )
    }

    private fun selectImportFile() {
        val intent =
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
            }
        selectShortcutFileLauncher.launch(
            Intent.createChooser(intent, "Select JSON file")
        )
    }

    private fun onMenuItem(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.bulkAdd -> bulkAddShortcuts()
            R.id.exportShortcuts -> selectExportFile()
            R.id.importShortcuts -> selectImportFile()
            R.id.importShortcutsLegacy -> selectImportFileLegacyCSV()
            else -> return false
        }
        return true
    }

    private fun onEdit(shortcut: Shortcut) {
        val editDialog: EditShortcutDialogFragment =
            EditShortcutDialogFragment.newInstance(
                shortcut,
                viewModel.createShortcutDialogViewModel()
            )
        editDialog.show(childFragmentManager, "fragment_edit")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        restoreThemeStatusBarIcons(requireActivity().window)
    }

    private fun renderLogFileRow() {
        binding.logFileValue.text = FileDisplayName.of(requireContext(), viewModel.getFilename())
        binding.logFileRow.setOnClickListener { openLogFileSettings() }
    }

    private fun renderShortcutList() {
        val recyclerView = binding.recyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        val callback: ItemTouchHelper.Callback = ShortcutTouchHelperCallback(adapter)
        val shortcutTouchHelper = ItemTouchHelper(callback)
        shortcutTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun renderShortcutInstructions() {
        if (adapter.items.isEmpty()) {
            binding.noShortcutsMessage.visibility = View.VISIBLE
        } else {
            binding.noShortcutsMessage.visibility = View.GONE
        }
    }

    override fun onFinishAddShortcutDialog(label: String, text: String, cursor: Int, type: String) {
        viewModel.addShortcut(
            label,
            text,
            cursor,
            type
        )
    }

    override fun onBulkAddShortcuts(info: List<Array<String>>) {
        viewModel.bulkAddShortcuts(info)
    }

    override fun onFinishEditShortcutDialog(
        label: String,
        text: String,
        cursor: Int,
        position: Int,
        type: String
    ) {
        viewModel.updateShortcut(
            label,
            text,
            cursor,
            position,
            type
        )
    }

    override fun labelExists(label: String): LiveData<Boolean> {
        return viewModel.labelExists(label)
    }

}