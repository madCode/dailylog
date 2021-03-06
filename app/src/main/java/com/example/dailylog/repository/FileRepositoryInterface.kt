package com.example.dailylog.repository

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.example.dailylog.R
import com.example.dailylog.ui.permissions.PermissionChecker
import java.io.*
import java.io.File
import java.lang.Exception

interface FileRepositoryInterface {
    var filename: String
    val context: Context
    val permissionChecker: PermissionChecker

    fun initializeFilename() {
        filename = retrieveFilename()
    }

    fun userHasSelectedFile(): Boolean {
        return filename == Constants.NO_FILE_SELECTED
    }

    fun retrieveFilename(): String {
        val preferences =
            context.getSharedPreferences(
                context.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            )
        return preferences.getString(Constants.FILENAME_PREF_KEY, Constants.NO_FILE_SELECTED)
            ?: Constants.NO_FILE_SELECTED
    }

    fun storeFilename(filename: String) {
        val preferences =
            context.getSharedPreferences(
                context.getString(
                    R.string.preference_file_key
                ), Context.MODE_PRIVATE
            )
        val editor = preferences.edit()
        editor.putString(Constants.FILENAME_PREF_KEY, filename)
        editor.apply()
        this.filename = filename
    }

    fun readFile(): String {
        if (permissionChecker.doIfExtStoragePermissionGranted()) {
            try {
                val stringBuilder = StringBuilder()
                val uri = Uri.parse(filename)
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
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
            } catch (ex: Exception) {
                Toast.makeText(context, ex.toString(), Toast.LENGTH_LONG)
                    .show()
            }
        }
        Toast.makeText(context, "File read permissions not granted", Toast.LENGTH_LONG).show()
        return ""
    }

    fun saveToFile(data: String): Boolean {
        if (permissionChecker.doIfExtStoragePermissionGranted()) {
            return try {
                val uri = Uri.parse(filename)
                val fileDescriptor =
                    context.contentResolver.openFileDescriptor(uri, "rwt")?.fileDescriptor
                val fileStream = FileOutputStream(fileDescriptor)
                fileStream.write((data).toByteArray())
                fileStream.close()
                true
            } catch (ex: IllegalArgumentException) {
                Toast.makeText(context, ex.toString(), Toast.LENGTH_LONG).show()
                false
            } catch (ex: Exception) {
                Toast.makeText(context, ex.toString(), Toast.LENGTH_LONG).show()
                false
            }
        }
        Toast.makeText(context, "File write permissions not granted.", Toast.LENGTH_LONG).show()
        return false
    }
}