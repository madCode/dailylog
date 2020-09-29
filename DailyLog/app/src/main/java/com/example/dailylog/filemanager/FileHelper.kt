package com.example.dailylog.filemanager

import android.app.Application
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.net.Uri
import android.widget.Toast
import com.example.dailylog.Constants
import com.example.dailylog.R
import com.example.dailylog.settings.PermissionChecker
import java.io.*

/**
 * Adapted from: http://instinctcoder.com/read-and-write-text-file-in-android-studio/
 */

object FileHelper {
    private const val TAG = "FileHelper"
    private var fileName: String = Constants.FILENAME_DEFAULT

    fun setUpHelper(application: Application) {
        fileName = getFilename(application)
        if (fileName == Constants.FILENAME_DEFAULT) {
            val dir = application.applicationContext.getDir("log_files", MODE_PRIVATE)
            val file = File(dir, fileName)
            file.createNewFile()
            val fileUri = file.toURI().toString()
            setFilename(fileUri, application)
            fileName = fileUri
            saveToFile("", application)
        }
    }

    fun getFilename(application: Application): String {
        val preferences =
            application.getSharedPreferences(
                application.applicationContext.getString(R.string.preference_file_key),
                MODE_PRIVATE
            )
        return preferences.getString(Constants.FILENAME_PREF_KEY, Constants.FILENAME_DEFAULT)
            ?: Constants.FILENAME_DEFAULT
    }

    fun setFilename(filename: String, application: Application) {
        val preferences =
            application.getSharedPreferences(
                application.applicationContext.getString(
                    R.string.preference_file_key
                ), MODE_PRIVATE
            )
        val editor = preferences.edit()
        editor.putString(Constants.FILENAME_PREF_KEY, filename)
        editor.apply()
        fileName = filename
    }

    fun readFile(permissionChecker: PermissionChecker, application: Application): String {
        if (permissionChecker.doIfExtStoragePermissionGranted()) {
            try {
                val stringBuilder = StringBuilder()
                val uri = Uri.parse(fileName)
                application.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        var line: String? = reader.readLine()
                        while (line != null) {
                            stringBuilder.append(line)
                            stringBuilder.append(System.lineSeparator())
                            line = reader.readLine()
                        }
                    }
                    inputStream.close()
                }
                return stringBuilder.toString()
            } catch (ex: FileNotFoundException) {
                Toast.makeText(application.applicationContext, ex.toString(), Toast.LENGTH_LONG)
                    .show()
            } catch (ex: IOException) {
                Toast.makeText(application.applicationContext, ex.toString(), Toast.LENGTH_LONG)
                    .show()
            }
        }
        Toast.makeText(application.applicationContext, "No permissions", Toast.LENGTH_LONG).show()
        return ""
    }

    fun saveToFile(data: String, application: Application): Boolean {
        try {
            val uri = Uri.parse(fileName)
            val fileDescriptor = application.contentResolver.openFileDescriptor(uri, "rwt")?.fileDescriptor
            val fileStream = FileOutputStream(fileDescriptor)
            fileStream.write((data).toByteArray())
            fileStream.close()
            return true
        } catch (ex: FileNotFoundException) {
            Toast.makeText(application.applicationContext, ex.toString(), Toast.LENGTH_LONG).show()
        } catch (ex: IOException) {
            Toast.makeText(application.applicationContext, ex.toString(), Toast.LENGTH_LONG).show()
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