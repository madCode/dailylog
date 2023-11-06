package com.app.dailylog.repository

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import com.app.dailylog.R
import com.app.dailylog.ui.permissions.PermissionChecker

interface RepositoryInterface: FileRepositoryInterface, ShortcutRepositoryInterface {
    fun getCursorIndex(): Int
    fun setCursorIndex(index: Int)
    fun getAllShortcuts(): LiveData<List<Shortcut>>
    fun labelExists(label: String): LiveData<Boolean>

    @RequiresApi(Build.VERSION_CODES.O)
    fun exportShortcuts(uri: Uri)

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun importShortcuts(uri: Uri)
}

class Repository(override val context: Context,
                 override val permissionChecker: PermissionChecker
): RepositoryInterface {
    override lateinit var filename : String
    override var lastSavedContentsHash: String = ""
    override var shortcutDao = ShortcutDatabase.getDatabase(context).shortcutDao()
    override var shortcutLiveData: LiveData<List<Shortcut>> = shortcutDao.getAll()

    init {
        initializeFilename()
    }

    override fun getCursorIndex(): Int {
        val preferences =
            context.getSharedPreferences(
                context.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            )
        return preferences.getInt(
            Constants.CURSOR_KEY,
            Constants.DEFAULT_CURSOR_INDEX
        )
    }

    override fun setCursorIndex(index: Int) {
        val preferences =
            context.getSharedPreferences(
                context.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            )
        val editor = preferences.edit()
        editor.putInt(Constants.CURSOR_KEY, index)
        editor.apply()
    }

    override fun getAllShortcuts(): LiveData<List<Shortcut>> {
        return shortcutLiveData
    }

    override fun labelExists(label: String): LiveData<Boolean> {
        return shortcutDao.labelExists(label)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun exportShortcuts(uri: Uri) {
        shortcutLiveData.value?.let { exportShortcuts(uri, getExportRows()) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun importShortcuts(uri: Uri) {
        val results = importShortcutValuesFromCSV(uri)
        if (results != null) {
            bulkAddShortcuts(shortcutInfoList = results)
        }
    }
}