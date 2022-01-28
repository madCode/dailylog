package com.app.dailylog.ui.settings

import android.app.Application
import android.net.Uri
import com.app.dailylog.repository.Repository
import com.app.dailylog.repository.Shortcut
import com.app.dailylog.utils.DetermineBuild
import junit.framework.TestCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.Mockito.mock

@ExperimentalCoroutinesApi
class SettingsViewModelTest : TestCase() {

    private val testDispatcher = TestCoroutineDispatcher()
    private var settingsViewModel: SettingsViewModel? = null
    private var applicationMock: Application = mock(Application::class.java)
    private var buildMock: DetermineBuild = mock(DetermineBuild::class.java)
    private var repository: Repository = mock(Repository::class.java)

    private fun finalize() {
        settingsViewModel = null
    }

    @Before
    fun setup() {
        // Sets the given [dispatcher] as an underlying dispatcher of [Dispatchers.Main].
        // All consecutive usages of [Dispatchers.Main] will use given [dispatcher] under the hood.
        Dispatchers.setMain(testDispatcher)
        applicationMock = mock(Application::class.java)
        buildMock = mock(DetermineBuild::class.java)
        repository = mock(Repository::class.java)
    }

    @After
    fun takeDown() {
        // Resets state of the [Dispatchers.Main] to the original main dispatcher.
        // For example, in Android Main thread dispatcher will be set as [Dispatchers.Main].
        Dispatchers.resetMain()

        // Clean up the TestCoroutineDispatcher to make sure no other work is running.
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `test repository called when saveFilename called`() {
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        settingsViewModel = SettingsViewModel(applicationMock, repository, buildMock, { _: String -> }, testDispatcher)
        settingsViewModel!!.saveFilename("hello")
        verify(repository).storeFilename("hello")
    }

    @Test
    fun `test repository called when updateShortcutPositions called`() = runBlocking {
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        settingsViewModel = SettingsViewModel(applicationMock, repository, buildMock, { _: String -> }, testDispatcher)
        val shortcutList = arrayListOf<Shortcut>(
            Shortcut(label = "test", value = "test", cursorIndex = 3, type = "TEXT", position = 1),
            Shortcut(label = "test2", value = "test", cursorIndex = 3, type = "TEXT", position = 2),
            Shortcut(label = "test3", value = "test", cursorIndex = 3, type = "TEXT", position = 3),
            Shortcut(label = "test4", value = "test", cursorIndex = 3, type = "TEXT", position = 4))
        settingsViewModel!!.updateShortcutPositions(shortcutList)
        verify(repository).updateShortcutPositions(shortcutList)
    }

    @Test
    fun `test repository called when removeCallback called`(): Unit = runBlocking {
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        settingsViewModel = SettingsViewModel(applicationMock, repository, buildMock, { _: String -> }, testDispatcher)
        settingsViewModel!!.removeCallback("test")
        verify(repository).removeShortcut("test")
    }

    @Test
    fun `test repository called when updateShortcut called`(): Unit = runBlocking {
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        settingsViewModel = SettingsViewModel(applicationMock, repository, buildMock, { _: String -> }, testDispatcher)
        settingsViewModel!!.updateShortcut("test","testTExt",0,1,"TEXT")
        verify(repository).updateShortcut("test","testTExt",0,1,"TEXT")
    }

    @Test
    fun `test repository called when bulkAddShortcuts called`(): Unit = runBlocking {
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        settingsViewModel = SettingsViewModel(applicationMock, repository, buildMock, { _: String -> }, testDispatcher)
        val shortcut = listOf(arrayOf("hello,hello,1,TEXT"))
        settingsViewModel!!.bulkAddShortcuts(shortcut)
        verify(repository).bulkAddShortcuts(shortcut)
    }

    @Test
    fun `test repository called when addShortcut called`(): Unit = runBlocking  {
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        settingsViewModel = SettingsViewModel(applicationMock, repository, buildMock, { _: String -> }, testDispatcher)
        settingsViewModel!!.addShortcut("hello", "hello", 1,"TEXT")
        verify(repository).addShortcut("hello", "hello", 1,"TEXT")
    }

    @Test
    fun `test repository called when getFilename called`() {
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        settingsViewModel = SettingsViewModel(applicationMock, repository, buildMock, { _: String -> }, testDispatcher)
        settingsViewModel!!.getFilename()
        verify(repository).retrieveFilename()
    }

    @Test
    fun `test repository called when getAllShortcuts called`() {
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        settingsViewModel = SettingsViewModel(applicationMock, repository, buildMock, { _: String -> }, testDispatcher)
        settingsViewModel!!.getAllShortcuts()
        verify(repository).getAllShortcuts()
    }

    @Test
    fun `when export called and not right OS version, error`() {
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        val settingsViewModel = SettingsViewModel(applicationMock, repository, buildMock, { _: String -> }, testDispatcher)
        settingsViewModel.exportFileUri = Uri.EMPTY
        val error = settingsViewModel.exportShortcuts()
        assertEquals("Need OS of Oreo or greater to export to CSV", error?.message)
    }

    @Test
    fun `when export called and no export file selected, error`() {
        `when`(buildMock.isOreoOrGreater()).thenReturn(true)
        val settingsViewModel = SettingsViewModel(applicationMock, repository, buildMock, { _: String -> }, testDispatcher)
        val error = settingsViewModel.exportShortcuts()
        assertEquals("No export file selected", error?.message)

    }

    @Test
    fun `when export called correctly, call repository`() {
        `when`(buildMock.isOreoOrGreater()).thenReturn(true)
        val settingsViewModel = SettingsViewModel(applicationMock, repository, buildMock, { _: String -> }, testDispatcher)
        settingsViewModel.exportFileUri = Uri.EMPTY
        val error = settingsViewModel.exportShortcuts()
        assertNull(error);
        verify(repository).exportShortcuts(Uri.EMPTY)

    }
}