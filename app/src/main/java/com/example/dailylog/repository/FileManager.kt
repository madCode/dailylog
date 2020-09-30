package com.example.dailylog.repository

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.example.dailylog.R
import com.example.dailylog.ui.permissions.PermissionChecker
import java.io.*
import java.io.File

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
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                                stringBuilder.append(System.lineSeparator())
                            } else {
                                TODO("VERSION GREATER THAN KITKAT")
                            }
                            line = reader.readLine()
                        }
                    }
                    inputStream.close()
                }
                return stringBuilder.toString()
            } catch (ex: FileNotFoundException) {
                Toast.makeText(context, ex.toString(), Toast.LENGTH_LONG)
                    .show()
            } catch (ex: IOException) {
                Toast.makeText(context, ex.toString(), Toast.LENGTH_LONG)
                    .show()
            }
        }
        Toast.makeText(context, "No permissions", Toast.LENGTH_LONG).show()
        return ""
    }

    fun saveToFile(data: String): Boolean {
        try {
            val uri = Uri.parse(filename)
            val fileDescriptor = context.contentResolver.openFileDescriptor(uri, "rwt")?.fileDescriptor
            val fileStream = FileOutputStream(fileDescriptor)
            fileStream.write((data).toByteArray())
            fileStream.close()
            return true
        } catch (ex: FileNotFoundException) {
            Toast.makeText(context, ex.toString(), Toast.LENGTH_LONG).show()
        } catch (ex: IOException) {
            Toast.makeText(context, ex.toString(), Toast.LENGTH_LONG).show()
        }
        return false
    }
}