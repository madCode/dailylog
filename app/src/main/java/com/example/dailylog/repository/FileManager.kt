package com.example.dailylog.repository

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.example.dailylog.R
import com.example.dailylog.ui.permissions.PermissionChecker
import java.io.*
import java.io.File
import java.lang.Exception

class FileManager(var context: Context, var permissionChecker: PermissionChecker) {
    private var filename: String
    init {
        filename = getFilename()
        if (filename == Constants.FILENAME_DEFAULT) {
            val dir = context.getDir("log_files", Context.MODE_PRIVATE)
            val file = File(dir, filename)
            file.createNewFile()
            val fileUri = file.toURI().toString()
            setFilename(fileUri)
            filename = fileUri
            saveToFile("")
        }
    }

    fun getFilename(): String {
        val preferences =
            context.getSharedPreferences(
                context.getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
            )
        return preferences.getString(Constants.FILENAME_PREF_KEY, Constants.FILENAME_DEFAULT)
            ?: Constants.FILENAME_DEFAULT
    }

    fun setFilename(filename: String) {
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
        if (permissionChecker.doIfAndroid10ExtStoragePermissionGranted()) {
            try {
                val uri = Uri.parse(filename)
                val fileDescriptor =
                    context.contentResolver.openFileDescriptor(uri, "rwt")?.fileDescriptor
                val fileStream = FileOutputStream(fileDescriptor)
                fileStream.write((data).toByteArray())
                fileStream.close()
                return true
            } catch (ex: Exception) {
                Toast.makeText(context, ex.toString(), Toast.LENGTH_LONG).show()
            }
        }
        Toast.makeText(context, "File write permissions not granted", Toast.LENGTH_LONG).show()
        return false
    }
}