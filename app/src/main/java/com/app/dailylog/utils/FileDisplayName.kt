package com.app.dailylog.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.app.dailylog.repository.Constants

/**
 * The readable name of the chosen log file, e.g. "journal.md".
 *
 * [Constants.FILENAME_PREF_KEY] holds a SAF URI, which renders as
 * `content://com.android.providers.media.documents/document/document%3A18`. Showing that to
 * someone who picked `journal.md` out of their vault tells them nothing.
 */
object FileDisplayName {

    fun of(context: Context, filename: String?): String {
        if (filename.isNullOrEmpty() || filename == Constants.NO_FILE_SELECTED) return ""
        val uri = try {
            Uri.parse(filename)
        } catch (e: Exception) {
            return filename
        }
        return queryDisplayName(context, uri) ?: fromPath(uri) ?: filename
    }

    private fun queryDisplayName(context: Context, uri: Uri): String? = try {
        context.contentResolver
            .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                val column = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (column >= 0 && cursor.moveToFirst()) cursor.getString(column) else null
            }
            ?.takeIf { it.isNotBlank() }
    } catch (e: Exception) {
        // A provider that has gone away, or a URI we no longer hold permission for.
        null
    }

    /**
     * file:// URIs, and tree URIs like `primary:Documents/journal.md`, carry the name in the path.
     *
     * lastPathSegment is already decoded, so a document id arrives as `document:18` rather than
     * `document%3A18`. A remaining colon means an id, not a name, and is not worth showing.
     */
    private fun fromPath(uri: Uri): String? =
        uri.lastPathSegment
            ?.substringAfterLast('/')
            ?.takeIf { it.isNotBlank() && !it.contains(':') }
}
