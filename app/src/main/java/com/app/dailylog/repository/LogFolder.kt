package com.app.dailylog.repository

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.DocumentsContract.Document
import android.webkit.MimeTypeMap

/** A folder picked with the system folder picker, holding one log file per day. */
class LogFolder(private val context: Context, private val treeUri: Uri) {

    private class Entry(val uri: Uri, val isDirectory: Boolean)

    private val root: Uri? = try {
        DocumentsContract.buildDocumentUriUsingTree(treeUri, DocumentsContract.getTreeDocumentId(treeUri))
    } catch (e: IllegalArgumentException) {
        null
    }

    /** Null when the folder is gone or the app lost its permission to it. */
    val name: String? get() = root?.let { query(it, arrayOf(Document.COLUMN_DISPLAY_NAME)) { c -> c.getString(0) } }

    /** The file at [path] (`/`-separated, relative to the folder), or null if it doesn't exist yet. */
    fun find(path: String): Uri? {
        var current = root ?: return null
        val segments = path.split("/")
        for (folder in segments.dropLast(1)) {
            current = child(current, folder)?.takeIf { it.isDirectory }?.uri ?: return null
        }
        return child(current, segments.last())?.takeIf { !it.isDirectory }?.uri
    }

    /** Creates the file at [path] and any missing folders on the way. */
    fun create(path: String): Uri? {
        val segments = path.split("/")
        var current = root ?: return null
        for (folder in segments.dropLast(1)) {
            current = child(current, folder)?.takeIf { it.isDirectory }?.uri
                ?: createDocument(current, Document.MIME_TYPE_DIR, folder)
                ?: return null
        }
        val fileName = segments.last()
        // A provider may append an extension that matches the MIME type, so the type must match the name's.
        val extension = fileName.substringAfterLast('.', "").lowercase()
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            ?: "application/octet-stream"
        return createDocument(current, mimeType, fileName)
    }

    // One query for all children: looking each one up by itself costs a query per file, and a
    // flat daily folder holds hundreds.
    private fun child(parent: Uri, name: String): Entry? {
        val children = DocumentsContract.buildChildDocumentsUriUsingTree(parent, DocumentsContract.getDocumentId(parent))
        val projection = arrayOf(Document.COLUMN_DOCUMENT_ID, Document.COLUMN_DISPLAY_NAME, Document.COLUMN_MIME_TYPE)
        return try {
            context.contentResolver.query(children, projection, null, null, null)?.use { cursor ->
                while (cursor.moveToNext()) {
                    if (cursor.getString(1) == name) {
                        return Entry(
                            DocumentsContract.buildDocumentUriUsingTree(parent, cursor.getString(0)),
                            cursor.getString(2) == Document.MIME_TYPE_DIR
                        )
                    }
                }
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun createDocument(parent: Uri, mimeType: String, name: String): Uri? = try {
        DocumentsContract.createDocument(context.contentResolver, parent, mimeType, name)
    } catch (e: Exception) {
        null
    }

    private fun <T> query(uri: Uri, projection: Array<String>, read: (android.database.Cursor) -> T): T? = try {
        context.contentResolver.query(uri, projection, null, null, null)?.use { if (it.moveToFirst()) read(it) else null }
    } catch (e: Exception) {
        null
    }
}
