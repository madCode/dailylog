package com.example.dailylog.log

import android.content.Context
import com.example.dailylog.filemanager.FileHelper
import com.example.dailylog.settings.PermissionChecker

class AddToLogPresenter(private var context: Context, private var fileHelper: FileHelper, private var permissionChecker: PermissionChecker) {
    fun readFile(): CharSequence? {
        return fileHelper.readFile(permissionChecker)
    }

    fun clearFile() {
        fileHelper.clearFile(context)
    }

    fun saveToFile(log: String): Boolean {
        return fileHelper.saveToFile(log)
    }
}