package com.app.dailylog.ui.settings

import android.view.View
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.dailylog.R
import com.app.dailylog.testutil.AppRobolectricTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BulkAddQuotedValueTest : AppRobolectricTest() {

    @Test
    fun quotedValueWithComma_isSavedWithoutQuotes() {
        launchApp().use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.btnSettings).performClick()
                idleUntil(500) { false }
                activity.findViewById<View>(R.id.addShortcutButton).performLongClick()
                idleUntil(500) { false }
                val settings = activity.supportFragmentManager.findFragmentById(R.id.container) as SettingsFragment
                val dialog = settings.childFragmentManager.findFragmentByTag("fragment_bulk_add") as DialogFragment
                dialog.requireView().findViewById<EditText>(R.id.bulkInput)
                    .setText("Greeting,\"hello, world\",5,TEXT")
                dialog.requireView().findViewById<View>(R.id.btnSaveBulkShortcut).performClick()
                idleUntil { activity.repository.getAllShortcuts().value.orEmpty().isNotEmpty() }
                assertEquals("hello, world", activity.repository.getAllShortcuts().value!!.single().value)
            }
        }
    }
}
