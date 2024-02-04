package com.app.dailylog.ui.settings

import android.content.Intent
import android.net.Uri
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.dailylog.R
import com.app.dailylog.repository.Shortcut
import com.app.dailylog.ui.permissions.PermissionChecker
import com.app.dailylog.utils.DetermineBuild
import com.app.dailylog.databinding.SettingsViewBinding

class SettingsFragment(
    private val viewModel: SettingsViewModel,
    private val permissionChecker: PermissionChecker
) : Fragment(),
    AddShortcutDialogFragment.AddShortcutDialogListener,
    BulkAddShortcutsDialogFragment.BulkAddListener,
    EditShortcutDialogFragment.EditShortcutDialogListener,
    ShortcutDialogListener {

    private var shortcutsLiveData: LiveData<List<Shortcut>> = viewModel.getAllShortcuts()
    private lateinit var adapter: ShortcutListAdapter
    private lateinit var binding: SettingsViewBinding

    companion object {
        fun newInstance(viewModel: SettingsViewModel, permissionChecker: PermissionChecker) =
            SettingsFragment(viewModel, permissionChecker)
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
        renderShortcutList()
        binding.addShortcutButton.setOnClickListener {
            val addDialog: AddShortcutDialogFragment =
                AddShortcutDialogFragment.newInstance(
                    viewModel.createShortcutDialogViewModel(),
                    this
                )
            addDialog.show(parentFragmentManager, "fragment_add_shortcut")
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
                    //                TODO("if we didn't get the permissions we needed, ask for permission or have the user select a different file")
                    val error = viewModel.exportShortcuts()
                    if (error != null) Toast.makeText(
                        requireContext(),
                        error.message,
                        Toast.LENGTH_LONG
                    ).show()
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
                viewModel.createShortcutDialogViewModel(),
                this
            )
        addBulkDialog.show(parentFragmentManager, "fragment_bulk_add")
    }

    private fun selectImportFile() {
        if (!permissionChecker.requestPermissionsBasedOnAppVersion()) {
            return
        }
        val intent =
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/*"
            }
        selectShortcutFileLauncher.launch(
            Intent.createChooser(intent, "Select file")
        )
    }

    private fun selectExportFile() {
        if (!permissionChecker.requestPermissionsBasedOnAppVersion()) {
            return
        }
        val intent =
            Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/csv"
                putExtra(Intent.EXTRA_TITLE, "shortcuts.csv")
            }
        if (DetermineBuild.isOreoOrGreater()) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse("/Documents"));
        }
        selectExportFileLauncher.launch(
            Intent.createChooser(intent, "Create file"),
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
                viewModel.createShortcutDialogViewModel(),
                this
            )
        editDialog.show(parentFragmentManager, "fragment_edit")
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