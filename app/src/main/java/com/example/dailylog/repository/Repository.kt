package com.example.dailylog.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.dailylog.R
import com.example.dailylog.ui.permissions.PermissionChecker
import java.util.ArrayList


class Repository(override val context: Context,
                 override val permissionChecker: PermissionChecker
): FileRepositoryInterface, ShortcutRepositoryInterface {
    override lateinit var filename : String

    override var shortcutDao = ShortcutDatabase.getDatabase(context).shortcutDao()
    override var shortcutLiveData: LiveData<List<Shortcut>> = shortcutDao.getAll()

    init {
        initializeFilename()
    }

    fun getCursorIndex(): Int {
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

    fun setCursorIndex(index: Int) {
        val preferences =
            context.getSharedPreferences(
                context.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            )
        val editor = preferences.edit()
        editor.putInt(Constants.CURSOR_KEY, index)
        editor.apply()
    }

    fun getAllShortcuts(): LiveData<List<Shortcut>> {
        return shortcutLiveData
    }

    fun labelExists(label: String): LiveData<Boolean> {
        return shortcutDao.labelExists(label)
    }
}