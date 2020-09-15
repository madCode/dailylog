package com.example.dailylog.filemanager

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.ContentUris
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.dailylog.Constants
import com.example.dailylog.R
import com.example.dailylog.settings.PermissionChecker
import java.io.*

private const val STORAGE_PERMISSIONS_REQUEST = 100

/**
 * Adapted from: http://instinctcoder.com/read-and-write-text-file-in-android-studio/
 */

object FileHelper {
    val TAG = "FileHelper"
    var fileName: String? = null

    fun setUpHelper(application: Application) {
        fileName = getFilename(application)
    }

    fun getFilename(application: Application): String {
        val preferences =
            application.getSharedPreferences(
                application.applicationContext.getString(R.string.preference_file_key),
                MODE_PRIVATE
            )
        return preferences.getString(Constants.FILENAME_PREF_KEY, Constants.FILENAME_DEFAULT) ?: Constants.FILENAME_DEFAULT
    }

    fun setFilename(filename: String?, application: Application) {
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

    fun readFile(permissionChecker: PermissionChecker): String? {
        if (permissionChecker.doIfExtStoragePermissionGranted()) {
            var line: String? = null
            try {
                val fileInputStream = FileInputStream(File(fileName))
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
                } else {
                    "File Not Found"
                }
                Log.d(TAG, message)
            } catch (ex: IOException) {
                val message = if (ex.message is String) {
                    ex.message!!
                } else {
                    "An IOException occurred."
                }
                Log.d(TAG, message)
            }
            return line
        }
        Log.d(TAG, "No permissions")
        return ""
    }

    fun saveToFile(data: String): Boolean {
        try {
            val fileStream = FileOutputStream(fileName)
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

    fun getPathFromUri(context: Context, uri: Uri): String? {
        // from: https://stackoverflow.com/questions/17546101/get-real-path-for-uri-android
        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                if ("home".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/Documents/" + split[1]
                }

                // TODO handle non-primary volumes
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri: Uri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)
                )
                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(
                    split[1]
                )
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {

            // Return the remote address
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
                context,
                uri,
                null,
                null
            )
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    private fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
            column
        )
        try {
            cursor = uri?.let {
                context.contentResolver.query(
                    it, projection, selection, selectionArgs,
                    null
                )
            }
            if (cursor?.moveToFirst()!!) {
                val index: Int = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }
}