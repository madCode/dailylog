package com.app.dailylog.ui.settings

import android.view.View
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.app.dailylog.R
import com.app.dailylog.testutil.AppRobolectricTest
import com.app.dailylog.utils.DetermineBuild
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class BulkAddInvalidLineTest : AppRobolectricTest() {

    // An exception escaping a background coroutine would crash the app on a device.
    private val uncaught = AtomicReference<Throwable?>()
    private var previousHandler: Thread.UncaughtExceptionHandler? = null

    @Before
    fun catchUncaught() {
        previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { _, e -> uncaught.set(e) }
    }

    @After
    fun restoreHandler() {
        Thread.setDefaultUncaughtExceptionHandler(previousHandler)
    }

    @Test
    fun invalidLine_keepsDialogOpenWithError() {
        launchApp().use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.btnSettings).performClick()
                idleUntil(500) { false }
                activity.findViewById<View>(R.id.addShortcutButton).performLongClick()
                idleUntil(500) { false }
                val settings = activity.supportFragmentManager.findFragmentById(R.id.container) as SettingsFragment
                val dialog = settings.childFragmentManager.findFragmentByTag("fragment_bulk_add") as DialogFragment
                dialog.requireView().findViewById<EditText>(R.id.bulkInput).setText("Label,text,99,TEXT")
                dialog.requireView().findViewById<View>(R.id.btnSaveBulkShortcut).performClick()
                idleUntil(1000) { uncaught.get() != null }

                assertNull(uncaught.get())
                assertTrue(dialog.isAdded)
                assertNotNull(dialog.requireView().findViewById<TextInputLayout>(R.id.bulkInputLayout).error)
                assertTrue(activity.repository.getAllShortcuts().value.orEmpty().isEmpty())
            }
        }
    }

    @Test
    fun viewModelBulkAdd_withInvalidRow_reportsErrorInsteadOfCrashing() {
        launchApp().use { scenario ->
            scenario.onActivity { activity ->
                val reported = AtomicReference<String?>()
                val viewModel = SettingsViewModel(activity.repository, DetermineBuild, { reported.set(it) }, Dispatchers.IO)
                viewModel.bulkAddShortcuts(listOf(arrayOf("Label", "text", "99", "TEXT")))
                idleUntil { reported.get() != null || uncaught.get() != null }

                assertNull(uncaught.get())
                assertTrue(reported.get()!!.contains("cursor"))
                assertEquals(0, activity.repository.getAllShortcuts().value.orEmpty().size)
            }
        }
    }
}
