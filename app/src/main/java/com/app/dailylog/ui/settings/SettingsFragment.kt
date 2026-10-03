package com.app.dailylog.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.TypedValue
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.widget.doAfterTextChanged
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.dailylog.R
import com.app.dailylog.repository.Shortcut
import com.app.dailylog.utils.DetermineBuild
import com.app.dailylog.utils.FileNameTemplate
import com.app.dailylog.databinding.SettingsViewBinding
import com.google.android.material.chip.Chip

class SettingsFragment(
    private val viewModel: SettingsViewModel
) : Fragment(),
    AddShortcutDialogFragment.AddShortcutDialogListener,
    BulkAddShortcutsDialogFragment.BulkAddListener,
    EditShortcutDialogFragment.EditShortcutDialogListener,
    ShortcutDialogListener {

    private var shortcutsLiveData: LiveData<List<Shortcut>> = viewModel.getAllShortcuts()
    private lateinit var adapter: ShortcutListAdapter
    private lateinit var binding: SettingsViewBinding

    companion object {
        fun newInstance(viewModel: SettingsViewModel) = SettingsFragment(viewModel)
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
        
        // Apply window insets to handle notch and navigation areas
        ViewCompat.setOnApplyWindowInsetsListener(view) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                systemBars.left,
                systemBars.top,    // avoids notch/status bar
                systemBars.right,
                systemBars.bottom  // avoids nav buttons
            )
            insets
        }
        
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
        renderFileNameRow()
        renderFileModeSection()
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
        binding.shortcutMenuButton.setOnClickListener { v: View ->
            showMenu(v, R.menu.shortcut_options_menu)
        }
    }

    private val selectImportFileLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK && result.data != null) {
                val selectedFileUri = result.data?.data
                if (selectedFileUri != null) {
                    viewModel.saveFilename(selectedFileUri.toString())
                    val contentResolver = requireContext().contentResolver
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    contentResolver.takePersistableUriPermission(selectedFileUri, takeFlags)
                    //                TODO("if we didn't get the permissions we needed, ask for permission or have the user select a different file")
                    binding.fileName.text = viewModel.getFilename()
                }
            }
        }

    private val selectLogFolderLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val folder = result.data?.data
            if (result.resultCode == AppCompatActivity.RESULT_OK && folder != null) {
                val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                val contentResolver = requireContext().contentResolver
                try {
                    contentResolver.takePersistableUriPermission(folder, flags)
                    // The system caps how many grants an app keeps, so let go of the folder this replaces.
                    viewModel.getLogFolder()?.takeIf { it != folder }?.let {
                        try {
                            contentResolver.releasePersistableUriPermission(it, flags)
                        } catch (e: SecurityException) {
                            // Already gone.
                        }
                    }
                    viewModel.chooseLogFolder(folder)
                } catch (e: SecurityException) {
                    Toast.makeText(requireContext(), R.string.log_folder_missing, Toast.LENGTH_LONG).show()
                }
            }
            renderFileMode()
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

    private fun showMenu(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(requireContext(), v)
        popup.menuInflater.inflate(menuRes, popup.menu)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.bulkAdd -> bulkAddShortcuts()
                R.id.exportShortcuts -> selectExportFile()
                R.id.importShortcuts -> selectImportFile()
                R.id.importShortcutsLegacy -> selectImportFileLegacyCSV()
            }
            return@setOnMenuItemClickListener true
        }
        popup.setOnDismissListener {
            // Respond to popup being dismissed.
        }
        // Show the popup menu.
        popup.show()
    }

    private fun onEdit(shortcut: Shortcut) {
        val editDialog: EditShortcutDialogFragment =
            EditShortcutDialogFragment.newInstance(
                shortcut,
                viewModel.createShortcutDialogViewModel()
            )
        editDialog.show(childFragmentManager, "fragment_edit")
    }

    private fun renderFileNameRow() {
        binding.fileName.text = viewModel.getFilename()
        binding.selectFileButton.setOnClickListener {
            val intent =
                Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/*"
                }
            selectImportFileLauncher.launch(Intent.createChooser(intent, "Select a file"))
        }
    }

    private fun renderFileModeSection() {
        // Date templates need java.time.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            binding.fileModeTitle.visibility = View.GONE
            binding.fileModeToggle.visibility = View.GONE
            return
        }
        binding.fileNameTemplate.setText(viewModel.getFileNameTemplate())
        renderFileMode()
        binding.fileModeToggle.addOnButtonCheckedListener { _, buttonId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            if (buttonId == R.id.oneFileButton) {
                viewModel.useOneFile()
            } else if (!viewModel.useDatedFiles()) {
                // Rendered again when the picker returns; a cancelled pick switches back to one file.
                selectLogFolder()
                return@addOnButtonCheckedListener
            }
            renderFileMode()
        }
        binding.chooseFolderButton.setOnClickListener { selectLogFolder() }
        binding.fileNameTemplate.doAfterTextChanged { renderTemplate() }
        val orderNames = listOf(
            R.string.date_order_ymd, R.string.date_order_dmy, R.string.date_order_mdy, R.string.date_order_year_folder
        )
        for ((datePart, orderName) in FileNameTemplate.PRESET_DATE_PARTS.zip(orderNames)) {
            val chip = Chip(requireContext())
            chip.text = FileNameTemplate.resolve(datePart)
            chip.contentDescription = getString(R.string.date_order_preset, getString(orderName), chip.text)
            chip.setOnClickListener {
                val template = binding.fileNameTemplate.text.toString()
                binding.fileNameTemplate.setText(FileNameTemplate.withDatePart(template, datePart))
            }
            binding.templatePresets.addView(chip)
        }
    }

    private fun selectLogFolder() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).addFlags(
            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or
                    Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        )
        selectLogFolderLauncher.launch(intent)
    }

    private fun renderFileMode() {
        val dated = viewModel.isDatedMode()
        binding.fileModeToggle.check(if (dated) R.id.datedFileButton else R.id.oneFileButton)
        binding.fileRow.visibility = if (dated) View.GONE else View.VISIBLE
        binding.datedFileGroup.visibility = if (dated) View.VISIBLE else View.GONE
        binding.folderName.text = viewModel.getLogFolderName() ?: getString(R.string.no_folder_chosen)
        renderTemplate()
    }

    private fun renderTemplate() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val template = binding.fileNameTemplate.text.toString()
        val error = viewModel.setFileNameTemplate(template)
        binding.fileNameTemplateLayout.error = error
        binding.todaysFilePreview.text = if (error == null) {
            val path = FileNameTemplate.resolve(template)
            getString(R.string.todays_file, listOfNotNull(viewModel.getLogFolderName(), path).joinToString("/"))
        } else {
            ""
        }
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