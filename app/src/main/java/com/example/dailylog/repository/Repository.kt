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
    override var labelList: ArrayList<String> = ArrayList()
    override var shortcutLiveData: LiveData<List<Shortcut>> = shortcutDao.getAll()
    override lateinit var shortcutList: List<Shortcut>

    init {
        initializeFilename()
    }

    fun getDateTimeFormat(): String? {
        val preferences =
            context.getSharedPreferences(
                context.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            )
        return preferences.getString(
            Constants.DATE_TIME_PREF_KEY,
            Constants.DATE_TIME_DEFAULT_FORMAT
        )
    }

    fun setDateTimeFormat(format: String) {
        val preferences =
            context.getSharedPreferences(
                context.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            )
        val editor = preferences.edit()
        editor.putString(Constants.DATE_TIME_PREF_KEY, format)
        editor.apply()
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

    fun labelIsUnique(label: String): Boolean {
        return !labelList.contains(label)
    }

    suspend fun updateShortcut(label: String, text: String, cursorIndex: Int): Boolean {
        val position = labelList.indexOf(label)
        return updateShortcut(label, text, cursorIndex, position)
    }

    fun updateShortcutList(it: List<Shortcut>?) {
        if (it != null) {
            shortcutList = it
            val labelList = ArrayList<String>()
            it.forEach{
                labelList.add(it.label)
            }
            this.labelList = labelList
        }
    }
}