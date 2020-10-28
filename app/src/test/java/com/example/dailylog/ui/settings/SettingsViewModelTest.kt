package com.example.dailylog.ui.settings

import android.app.Application
import android.content.Context
import com.example.dailylog.repository.Constants
import com.example.dailylog.repository.Repository
import com.example.dailylog.repository.Shortcut
import junit.framework.TestCase
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.Mockito.mock


class SettingsViewModelTest : TestCase() {

    var settingsViewModel: SettingsViewModel? = null

    private fun init() {
        val applicationMock = mock(Application::class.java)
        val repository: Repository = mock(Repository::class.java)
        `when`(repository.getDateTimeFormat()).thenReturn(Constants.DATE_TIME_DEFAULT_FORMAT)
        doNothing().`when`(repository).setDateTimeFormat(Constants.DATE_TIME_DEFAULT_FORMAT)
        val context = mock(Context::class.java)
        val editCallback: (Shortcut) -> Unit = { _ -> }
        settingsViewModel = SettingsViewModel(applicationMock, repository, context, editCallback)
    }

    private fun finalize() {
        settingsViewModel = null
    }

    @Test
    fun `test DateTimeFormat is retrieved`() {
        init()
        assertEquals(Constants.DATE_TIME_DEFAULT_FORMAT, settingsViewModel?.dateTimeFormat)
        finalize()
    }

    @Test
    fun `test (Oreo) saves dateTimeFormat when it's valid`() {
        init()
        `when`(settingsViewModel!!.isOreoOrGreater()).thenReturn(true)
        assertNotNull(settingsViewModel)
        assertTrue(settingsViewModel!!.saveDateTimeFormat(Constants.DATE_TIME_DEFAULT_FORMAT))
        finalize()
    }

    @Test
    fun `test (Nougat) saves dateTimeFormat when it's valid`() {
        init()
        assertNotNull(settingsViewModel)
        `when`(settingsViewModel!!.isOreoOrGreater()).thenReturn(false)
        assertTrue(settingsViewModel!!.saveDateTimeFormat(Constants.DATE_TIME_DEFAULT_FORMAT))
        finalize()
    }

    @Test
    fun `test (Oreo) does not dateTimeFormat when it's invalid`() {
        init()
        assertNotNull(settingsViewModel)
        `when`(settingsViewModel!!.isOreoOrGreater()).thenReturn(true)
        assertFalse(settingsViewModel!!.saveDateTimeFormat("EEEE"))
        finalize()
    }

    @Test
    fun `test (Nougat) does not dateTimeFormat when it's invalid`() {
        init()
        `when`(settingsViewModel!!.isOreoOrGreater()).thenReturn(false)
        assertNotNull(settingsViewModel)
        assertFalse(settingsViewModel!!.saveDateTimeFormat("EEEE"))
        finalize()
    }

}