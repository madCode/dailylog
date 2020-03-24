package com.example.dailylog

import android.content.Context

class AddToLogPresenter(private var context: Context) {
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