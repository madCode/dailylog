package com.example.dailylog

import android.app.Application
import android.content.Context
import android.content.Context.MODE_APPEND
import android.content.Context.MODE_PRIVATE
import android.util.Log
import java.io.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Adapted from: http://instinctcoder.com/read-and-write-text-file-in-android-studio/
 */

object FileHelper {
    val TAG = "FileHelper"
    private var fileNameFormat: String = Constants.FILENAME_DEFAULT_FORMAT
    private var fileName: String? = "data.txt"

    fun setUpHelper(application: Application) {
        fileNameFormat = getFilenameFormat(application)
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern(fileNameFormat)
        fileName = current.format(formatter) + ".txt"
    }

    fun getFilenameFormat (application: Application): String {
        val preferences =
            application.getSharedPreferences(
                application.applicationContext.getString(R.string.preference_file_key),
                MODE_PRIVATE
            )
        return preferences.getString(Constants.FILENAME_PREF_KEY, Constants.FILENAME_DEFAULT_FORMAT) ?: Constants.FILENAME_DEFAULT_FORMAT
    }

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
            val message: String = if (ex.message is String) {
                ex.message!!
            } else {"File Not Found"}
            Log.d(TAG, message)
        } catch (ex: IOException) {
            val message = if (ex.message is String) { ex.message!! } else {"An IOException occurred."}
            Log.d(TAG, message)
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
            val message = if (ex.message is String) { ex.message!! } else {"File Not Found"}
            Log.d(TAG, message)
        } catch (ex: IOException) {
            val message = if (ex.message is String) { ex.message!! } else {"An IOException occurred."}
            Log.d(TAG, message)
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