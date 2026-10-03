package com.app.dailylog.repository

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.documentfile.provider.DocumentFile

/** A folder picked with the system folder picker, holding one log file per day. */
class LogFolder(private val context: Context, private val treeUri: Uri) {

    private val root: DocumentFile? get() = DocumentFile.fromTreeUri(context, treeUri)

    val name: String? get() = root?.name

    /** The file at [path] (`/`-separated, relative to the folder), or null if it doesn't exist yet. */
    fun find(path: String): Uri? {
        var current = root ?: return null
        for (segment in path.split("/")) {
            current = current.findFile(segment) ?: return null
        }
        return if (current.isFile) current.uri else null
    }

    /** Creates the file at [path] and any folders on the way, or returns it if it already exists. */
    fun create(path: String): Uri? {
        val segments = path.split("/")
        var current = root ?: return null
        for (folder in segments.dropLast(1)) {
            current = current.findFile(folder) ?: current.createDirectory(folder) ?: return null
        }
        val fileName = segments.last()
        current.findFile(fileName)?.let { return it.uri }
        // A provider may append an extension that matches the MIME type, so the type must match the name's.
        val extension = fileName.substringAfterLast('.', "").lowercase()
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            ?: "application/octet-stream"
        return current.createFile(mimeType, fileName)?.uri
    }
}
