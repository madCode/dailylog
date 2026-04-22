package com.app.dailylog.ui.settings

import android.net.Uri
import com.app.dailylog.repository.RepositoryInterface
import com.app.dailylog.repository.Shortcut
import com.app.dailylog.utils.DetermineBuildInterface
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
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.Mockito.any

@ExperimentalCoroutinesApi
class SettingsViewModelTest : TestCase() {

    private val testDispatcher = TestCoroutineDispatcher()
    private var settingsViewModel: SettingsViewModel? = null
    private var buildMock: DetermineBuildInterface = mock(DetermineBuildInterface::class.java)
    private var repository: RepositoryInterface = mock(RepositoryInterface::class.java)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        buildMock = mock(DetermineBuildInterface::class.java)
        repository = mock(RepositoryInterface::class.java)
    }

    @After
    fun takeDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `test repository called when saveFilename called`() {
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        settingsViewModel = SettingsViewModel(repository, buildMock, { _: String -> }, testDispatcher)
        settingsViewModel!!.saveFilename("hello")
        verify(repository).storeFilename("hello")
    }

    @Test
    fun `test repository called when updateShortcutPositions called`(): Unit = runBlocking {
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        settingsViewModel = SettingsViewModel(repository, buildMock, { _: String -> }, testDispatcher)
        val shortcutList = arrayListOf(
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
        settingsViewModel = SettingsViewModel(repository, buildMock, { _: String -> }, testDispatcher)
        settingsViewModel!!.removeCallback("test")
        verify(repository).removeShortcut("test")
    }

    @Test
    fun `test repository called when updateShortcut called`(): Unit = runBlocking {
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        settingsViewModel = SettingsViewModel(repository, buildMock, { _: String -> }, testDispatcher)
        settingsViewModel!!.updateShortcut("test", "testTExt", 0, 1, "TEXT")
        verify(repository).updateShortcut("test", "testTExt", 0, 1, "TEXT")
    }

    @Test
    fun `test repository called when bulkAddShortcuts called`(): Unit = runBlocking {
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        settingsViewModel = SettingsViewModel(repository, buildMock, { _: String -> }, testDispatcher)
        val shortcut = listOf(arrayOf("hello,hello,1,TEXT"))
        settingsViewModel!!.bulkAddShortcuts(shortcut)
        verify(repository).bulkAddShortcuts(shortcut)
    }

    @Test
    fun `test repository called when addShortcut called`(): Unit = runBlocking {
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        settingsViewModel = SettingsViewModel(repository, buildMock, { _: String -> }, testDispatcher)
        settingsViewModel!!.addShortcut("hello", "hello", 1, "TEXT")
        verify(repository).addShortcut("hello", "hello", 1, "TEXT")
    }

    @Test
    fun `test repository called when getFilename called`() {
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        settingsViewModel = SettingsViewModel(repository, buildMock, { _: String -> }, testDispatcher)
        settingsViewModel!!.getFilename()
        verify(repository).retrieveFilename()
    }

    @Test
    fun `test repository called when getAllShortcuts called`() {
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        settingsViewModel = SettingsViewModel(repository, buildMock, { _: String -> }, testDispatcher)
        settingsViewModel!!.getAllShortcuts()
        verify(repository).getAllShortcuts()
    }

    @Test
    fun `test repository called when labelExists called`() {
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        settingsViewModel = SettingsViewModel(repository, buildMock, { _: String -> }, testDispatcher)
        settingsViewModel!!.labelExists("test")
        verify(repository).labelExists("test")
    }

    @Test
    fun `when export called and no export file selected error`() {
        `when`(buildMock.isOreoOrGreater()).thenReturn(true)
        val settingsViewModel = SettingsViewModel(repository, buildMock, { _: String -> }, testDispatcher)
        val error = settingsViewModel.exportShortcuts()
        assertEquals("No export file selected", error?.message)
    }

    @Test
    fun `when export called correctly call repository`() {
        `when`(buildMock.isOreoOrGreater()).thenReturn(true)
        val settingsViewModel = SettingsViewModel(repository, buildMock, { _: String -> }, testDispatcher)
        settingsViewModel.exportFileUri = Uri.EMPTY
        val error = settingsViewModel.exportShortcuts()
        assertNull(error)
        verify(repository).exportShortcutsAsJson(Uri.EMPTY)
    }

    @Test
    fun `when exportShortcuts throws exception returns error`() {
        `when`(buildMock.isOreoOrGreater()).thenReturn(true)
        val settingsViewModel = SettingsViewModel(repository, buildMock, { _: String -> }, testDispatcher)
        settingsViewModel.exportFileUri = Uri.EMPTY
        doThrow(Exception("Export failed")).`when`(repository).exportShortcutsAsJson(any())
        val error = settingsViewModel.exportShortcuts()
        assertNotNull(error)
        assertTrue(error?.message?.contains("Error:") == true)
    }

    @Test
    fun `when getFilename called then returns value from repository`() {
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        `when`(repository.retrieveFilename()).thenReturn("test_filename.md")
        settingsViewModel = SettingsViewModel(repository, buildMock, { _: String -> }, testDispatcher)
        val result = settingsViewModel!!.getFilename()
        assertEquals("test_filename.md", result)
        verify(repository).retrieveFilename()
    }

    @Test
    fun `when createShortcutDialogViewModel called returns correct instance`() {
        settingsViewModel = SettingsViewModel(repository, buildMock, { _: String -> }, testDispatcher)
        val dialogViewModel = settingsViewModel!!.createShortcutDialogViewModel()
        assertNotNull(dialogViewModel)
        assertTrue(dialogViewModel is ShortcutDialogViewModel)
    }

    // Task 7: import path tests

    @Test
    fun `importShortcutsLegacy with Oreo calls repository`(): Unit = runBlocking {
        `when`(buildMock.isOreoOrGreater()).thenReturn(true)
        val toastMessages = mutableListOf<String>()
        val vm = SettingsViewModel(repository, buildMock, { msg -> toastMessages.add(msg) }, testDispatcher)
        vm.importShortcutsLegacy(Uri.EMPTY)
        // Use the proper way to advance coroutines instead of deprecated advanceUntilIdle()
        testDispatcher.scheduler.advanceUntilIdle()
        verify(repository).importShortcuts(Uri.EMPTY)
    }

    @Test
    fun `importShortcutsLegacy below Oreo shows toast`(): Unit = runBlocking {
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        val toastMessages = mutableListOf<String>()
        val vm = SettingsViewModel(repository, buildMock, { msg -> toastMessages.add(msg) }, testDispatcher)
        vm.importShortcutsLegacy(Uri.EMPTY)
        // Use the proper way to advance coroutines instead of deprecated advanceUntilIdle()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(toastMessages.contains("Need OS of Oreo or greater to import from CSV"))
    }

    @Test
    fun `importShortcuts with Oreo calls repository`(): Unit = runBlocking {
        `when`(buildMock.isOreoOrGreater()).thenReturn(true)
        val toastMessages = mutableListOf<String>()
        val vm = SettingsViewModel(repository, buildMock, { msg -> toastMessages.add(msg) }, testDispatcher)
        vm.importShortcuts(Uri.EMPTY)
        // Use the proper way to advance coroutines instead of deprecated advanceUntilIdle()
        testDispatcher.scheduler.advanceUntilIdle()
        verify(repository).importShortcutsFromJson(Uri.EMPTY)
    }

    @Test
    fun `importShortcuts below Oreo shows toast`(): Unit = runBlocking {
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        val toastMessages = mutableListOf<String>()
        val vm = SettingsViewModel(repository, buildMock, { msg -> toastMessages.add(msg) }, testDispatcher)
        vm.importShortcuts(Uri.EMPTY)
        // Use the proper way to advance coroutines instead of deprecated advanceUntilIdle()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(toastMessages.contains("Need OS of Oreo or greater to import from CSV"))
    }

    // Task 8A: factory test

    @Test
    fun `SettingsViewModelFactory creates SettingsViewModel`() {
        val factory = SettingsViewModelFactory(repository, buildMock, {}, testDispatcher)
        val vm = factory.create(SettingsViewModel::class.java)
        assertNotNull(vm)
        assertTrue(vm is SettingsViewModel)
    }
}