package com.example.dailylog.log

import android.content.Context
import com.example.dailylog.filemanager.FileHelper

class AddToLogPresenter(private var context: Context, private var fileHelper: FileHelper) {
    fun readFile(): CharSequence? {
        return FileHelper.readFile(context)
    }

    fun clearFile() {
        FileHelper.clearFile(context)
    }

    fun saveToFile(log: String): Boolean {
        return FileHelper.saveToFile(context, log)
    }
}