package com.app.dailylog.utils

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.dailylog.repository.Constants
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class FileDisplayNameTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun fileUri_showsTheFileName() {
        val file = File(context.filesDir, "journal.md")
        assertEquals("journal.md", FileDisplayName.of(context, Uri.fromFile(file).toString()))
    }

    @Test
    fun noFileSelected_showsNothing() {
        assertEquals("", FileDisplayName.of(context, Constants.NO_FILE_SELECTED))
        assertEquals("", FileDisplayName.of(context, ""))
        assertEquals("", FileDisplayName.of(context, null))
    }

    /** An encoded document id is not a name; better to show the URI than "document%3A18". */
    @Test
    fun opaqueProviderUri_fallsBackToTheUri() {
        val uri = "content://com.android.providers.media.documents/document/document%3A18"
        assertEquals(uri, FileDisplayName.of(context, uri))
    }

    @Test
    fun unparseableValue_isReturnedUnchanged() {
        assertEquals("not a uri", FileDisplayName.of(context, "not a uri"))
    }
}
