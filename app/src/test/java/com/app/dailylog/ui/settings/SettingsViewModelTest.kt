package com.app.dailylog.ui.settings

import android.app.Application
import com.app.dailylog.repository.Repository
import com.app.dailylog.repository.Shortcut
import com.app.dailylog.utils.DetermineBuild
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
    }

    private fun finalize() {
        settingsViewModel = null
    }

    @Test
    fun `test repository called when saveFilename called`() {
        init()
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        settingsViewModel = SettingsViewModel(applicationMock, repository, buildMock) { _: String -> }
        settingsViewModel!!.saveFilename("hello")
        verify(repository).storeFilename("hello")
    }

    @Test
    suspend fun `test repository called when updateShortcutPositions called`() {
        init()
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        settingsViewModel = SettingsViewModel(applicationMock, repository, buildMock) { _: String -> }
        var shortcutList = arrayListOf<Shortcut>(
            Shortcut(label = "test", value = "test", cursorIndex = 3, type = "TEXT", position = 1),
            Shortcut(label = "test2", value = "test", cursorIndex = 3, type = "TEXT", position = 2),
            Shortcut(label = "test3", value = "test", cursorIndex = 3, type = "TEXT", position = 3),
            Shortcut(label = "test4", value = "test", cursorIndex = 3, type = "TEXT", position = 4))
        settingsViewModel!!.updateShortcutPositions(shortcutList)
        verify(repository).updateShortcutPositions(shortcutList)
    }

    @Test
    suspend fun `test repository called when removeCallback called`() {
        init()
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        settingsViewModel = SettingsViewModel(applicationMock, repository, buildMock) { _: String -> }
        settingsViewModel!!.removeCallback("test")
        verify(repository).removeShortcut("test")
    }

    @Test
    suspend fun `test repository called when updateShortcut called`() {
        init()
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        settingsViewModel = SettingsViewModel(applicationMock, repository, buildMock) { _: String -> }
        settingsViewModel!!.updateShortcut("test","testTExt",0,1,"TEXT")
        verify(repository).updateShortcut("test","testTExt",0,1,"TEXT")
    }

    @Test
    suspend fun `test repository called when bulkAddShortcuts called`() {
        init()
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        settingsViewModel = SettingsViewModel(applicationMock, repository, buildMock) { _: String -> }
        settingsViewModel!!.bulkAddShortcuts(listOf(arrayOf("hello,hello,1,TEXT")))
        verify(repository).bulkAddShortcuts(listOf(arrayOf("hello,hello,1,TEXT")))
    }

    @Test
    suspend fun `test repository called when addShortcut called`() {
        init()
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        settingsViewModel = SettingsViewModel(applicationMock, repository, buildMock) { _: String -> }
        settingsViewModel!!.addShortcut("hello", "hello", 1,"TEXT")
        verify(repository).addShortcut("hello", "hello", 1,"TEXT")
    }

    @Test
    fun `test repository called when getFilename called`() {
        init()
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        settingsViewModel = SettingsViewModel(applicationMock, repository, buildMock) { _: String -> }
        settingsViewModel!!.getFilename()
        verify(repository).retrieveFilename()
    }

    @Test
    fun `test repository called when getAllShortcuts called`() {
        init()
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        settingsViewModel = SettingsViewModel(applicationMock, repository, buildMock) { _: String -> }
        settingsViewModel!!.getAllShortcuts()
        verify(repository).getAllShortcuts()
    }
}