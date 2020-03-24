package com.example.dailylog

import android.content.Context
import android.content.Context.MODE_APPEND
import android.content.Context.MODE_PRIVATE
import android.util.Log
import java.io.*

/**
 * Adapted from: http://instinctcoder.com/read-and-write-text-file-in-android-studio/
 */

object FileHelper {
    const val fileName = "data.txt"
    const val TAG = "FileHelper"
    fun readFile(context: Context): String? {
        var line: String? = null
        try {
            val fileInputStream = context.openFileInput(fileName)
            val inputStreamReader =
                InputStreamReader(fileInputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            val stringBuilder = StringBuilder()
            while (bufferedReader.readLine().also { line = it } != null) {
                stringBuilder.append(line + System.getProperty("line.separator"))
            }
            fileInputStream.close()
            line = stringBuilder.toString()
            bufferedReader.close()
        } catch (ex: FileNotFoundException) {
            Log.d(TAG, ex.message)
        } catch (ex: IOException) {
            Log.d(TAG, ex.message)
        }
        return line
    }

    fun saveToFile(context: Context, data: String): Boolean {
        try {
            val fileStream = context.openFileOutput(fileName, MODE_APPEND)
            fileStream.write((data + System.getProperty("line.separator")).toByteArray())
            fileStream.close()
            return true
        } catch (ex: FileNotFoundException) {
            Log.d(TAG, ex.message)
        } catch (ex: IOException) {
            Log.d(TAG, ex.message)
        }
        return false
    }

    fun clearFile(context: Context): Boolean {
        val fileStream = context.openFileOutput(fileName, MODE_PRIVATE)
        fileStream.write("".toByteArray())
        fileStream.close()
        return true
    }
}