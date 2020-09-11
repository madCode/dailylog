package com.example.dailylog

import android.content.Context

class AddToLogPresenter(private var context: Context, private var fileHelper: FileHelper) {
    fun readFile(): CharSequence? {
        return fileHelper.readFile(context)
    }

    fun clearFile() {
        fileHelper.clearFile(context)
    }

    fun saveToFile(log: String): Boolean {
        return fileHelper.saveToFile(context, log)
    }
}