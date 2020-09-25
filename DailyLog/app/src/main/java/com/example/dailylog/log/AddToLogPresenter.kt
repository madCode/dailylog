package com.example.dailylog.log

import android.app.Application
import com.example.dailylog.filemanager.FileHelper
import com.example.dailylog.settings.PermissionChecker

class AddToLogPresenter(private var application: Application, private var fileHelper: FileHelper, private var permissionChecker: PermissionChecker) {
    fun readFile(): CharSequence? {
        return fileHelper.readFile(permissionChecker, application)
    }

    fun clearFile() {
        fileHelper.clearFile(application.applicationContext)
    }

    fun saveToFile(log: String): Boolean {
        return fileHelper.saveToFile(log, application)
    }
}