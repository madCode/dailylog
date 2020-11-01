package com.example.dailylog.ui.settings

import android.app.Application
import com.example.dailylog.repository.Constants
import com.example.dailylog.repository.Repository
import com.example.dailylog.utils.DetermineBuild
import junit.framework.TestCase
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.Mockito.mock


class SettingsViewModelTest : TestCase() {

    var settingsViewModel: SettingsViewModel? = null
    var applicationMock: Application = mock(Application::class.java)
    var buildMock: DetermineBuild = mock(DetermineBuild::class.java)
    var repository: Repository = mock(Repository::class.java)

    private fun init() {
        applicationMock = mock(Application::class.java)
        buildMock = mock(DetermineBuild::class.java)
        repository = mock(Repository::class.java)
        `when`(repository.getDateTimeFormat()).thenReturn(Constants.DATE_TIME_DEFAULT_FORMAT)
        doNothing().`when`(repository).setDateTimeFormat(Constants.DATE_TIME_DEFAULT_FORMAT)
    }

    private fun finalize() {
        settingsViewModel = null
    }

    @Test
    fun `test DateTimeFormat is retrieved`() {
        init()
        `when`(buildMock.isOreoOrGreater()).thenReturn(true)
        settingsViewModel = SettingsViewModel(applicationMock, repository, buildMock)
        assertEquals(Constants.DATE_TIME_DEFAULT_FORMAT, settingsViewModel?.dateTimeFormat)
        finalize()
    }

    @Test
    fun `test (Oreo) saves dateTimeFormat when it's valid`() {
        init()
        `when`(buildMock.isOreoOrGreater()).thenReturn(true)
        settingsViewModel = SettingsViewModel(applicationMock, repository, buildMock)
        assertNotNull(settingsViewModel)
        assertTrue(settingsViewModel!!.saveDateTimeFormat(Constants.DATE_TIME_DEFAULT_FORMAT))
        finalize()
    }

    @Test
    fun `test (Nougat) saves dateTimeFormat when it's valid`() {
        init()
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        settingsViewModel = SettingsViewModel(applicationMock, repository, buildMock)
        assertTrue(settingsViewModel!!.saveDateTimeFormat(Constants.DATE_TIME_DEFAULT_FORMAT))
        finalize()
    }

    @Test
    fun `test (Oreo) does not dateTimeFormat when it's invalid`() {
        init()
        `when`(buildMock.isOreoOrGreater()).thenReturn(true)
        settingsViewModel = SettingsViewModel(applicationMock, repository, buildMock)
        assertFalse(settingsViewModel!!.saveDateTimeFormat("# E"))
        finalize()
    }

    @Test
    fun `test (Nougat) does not dateTimeFormat when it's invalid`() {
        init()
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        settingsViewModel = SettingsViewModel(applicationMock, repository, buildMock)
        assertFalse(settingsViewModel!!.saveDateTimeFormat("hello"))
        finalize()
    }

    @Test
    fun `test repository called when saveFilename called`() {
        init()
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        settingsViewModel = SettingsViewModel(applicationMock, repository, buildMock)
        settingsViewModel!!.saveFilename("hello")
        verify(repository).storeFilename("hello")
    }

//    @Test
//    fun `test repository called when updateShortcutCallbackPosition called`() {
//        init()
//        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
//        settingsViewModel = SettingsViewModel(applicationMock, repository, buildMock)
//        settingsViewModel!!.getFilename()
//        verify(repository).retrieveFilename()
//    }
//
//    @Test
//    fun `test repository called when removeCallback called`() {
//        init()
//        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
//        settingsViewModel = SettingsViewModel(applicationMock, repository, buildMock)
//        settingsViewModel!!.getFilename()
//        verify(repository).retrieveFilename()
//    }
//
//    @Test
//    fun `test repository called when updateShortcut called`() {
//        init()
//        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
//        settingsViewModel = SettingsViewModel(applicationMock, repository, buildMock)
//        settingsViewModel!!.getFilename()
//        verify(repository).retrieveFilename()
//    }

//    @Test
//    fun `test repository called when bulkAddShortcuts called`() {
//        init()
//        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
//        settingsViewModel = SettingsViewModel(applicationMock, repository, buildMock)
//        settingsViewModel!!.bulkAddShortcuts(listOf(listOf("hello,hello,1")))
//        verify(repository).bulkAddShortcuts(listOf(listOf("hello,hello,1")))
//    }

//    @Test
//    fun `test repository called when addShortcut called`() {
//        init()
//        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
//        settingsViewModel = SettingsViewModel(applicationMock, repository, buildMock)
//        settingsViewModel!!.addShortcut("hello", "hello", 1)
//        verify(repository).addShortcut("hello", "hello", 1)
//    }

    @Test
    fun `test repository called when getFilename called`() {
        init()
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        settingsViewModel = SettingsViewModel(applicationMock, repository, buildMock)
        settingsViewModel!!.getFilename()
        verify(repository).retrieveFilename()
    }

    @Test
    fun `test repository called when getAllShortcuts called`() {
        init()
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        settingsViewModel = SettingsViewModel(applicationMock, repository, buildMock)
        settingsViewModel!!.getAllShortcuts()
        verify(repository).getAllShortcuts()
    }

}