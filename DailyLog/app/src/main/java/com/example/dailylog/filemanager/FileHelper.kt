package com.example.dailylog.filemanager

import android.app.Application
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import com.example.dailylog.Constants
import com.example.dailylog.R
import java.io.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Adapted from: http://instinctcoder.com/read-and-write-text-file-in-android-studio/
 */

object FileHelper {
    val TAG = "FileHelper"
    private var fileName: String = "data.md"

    fun setUpHelper(application: Application) {
        fileName = getFilename(application)
    }

    fun getFilename (application: Application): String {
        val preferences =
            application.getSharedPreferences(
                application.applicationContext.getString(R.string.preference_file_key),
                MODE_PRIVATE
            )
        return preferences.getString(Constants.FILENAME_PREF_KEY, Constants.FILENAME_DEFAULT_FORMAT) ?: Constants.FILENAME_DEFAULT_FORMAT
    }

    fun setFilename (filename: String, application: Application) {
        val preferences =
            application.getSharedPreferences(
                application.applicationContext.getString(
                    R.string.preference_file_key
                ), Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString(Constants.FILENAME_PREF_KEY, filename)
        editor.apply()
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
            val fileStream = context.openFileOutput(fileName, MODE_PRIVATE)
            fileStream.write((data).toByteArray())
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

    fun getDateTimeFormat(application: Application): String? {
        val preferences =
            application.getSharedPreferences(
                application.applicationContext.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            )
        return preferences.getString(
            Constants.DATE_TIME_PREF_KEY,
            Constants.DATE_TIME_DEFAULT_FORMAT
        )
    }

    fun setDateTimeFormat(format: String, application: Application) {
        val preferences =
            application.getSharedPreferences(
                application.applicationContext.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            )
        val editor = preferences.edit()
        editor.putString("dateFormat", format)
        editor.apply()
    }
}