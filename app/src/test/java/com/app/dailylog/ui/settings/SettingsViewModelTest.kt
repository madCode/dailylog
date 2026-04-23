package com.app.dailylog.ui.settings

import android.net.Uri
import com.app.dailylog.repository.RepositoryInterface
import com.app.dailylog.repository.Shortcut
import com.app.dailylog.utils.DetermineBuildInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@ExperimentalCoroutinesApi
class SettingsViewModelTest {

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
    fun testRepositoryCalledWhenSaveFilenameCalled() {
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        settingsViewModel = SettingsViewModel(repository, buildMock, { _: String -> }, testDispatcher)
        settingsViewModel!!.saveFilename("hello")
        verify(repository).storeFilename("hello")
    }

    @Test
    fun testRepositoryCalledWhenUpdateShortcutPositionsCalled(): Unit = runBlocking {
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        settingsViewModel = SettingsViewModel(repository, buildMock, { _: String -> }, testDispatcher)
        val shortcutList = arrayListOf(
            Shortcut(id = "id-1", label = "test", value = "test", cursorIndex = 3, type = "TEXT", position = 1),
            Shortcut(id = "id-2", label = "test2", value = "test", cursorIndex = 3, type = "TEXT", position = 2),
            Shortcut(id = "id-3", label = "test3", value = "test", cursorIndex = 3, type = "TEXT", position = 3),
            Shortcut(id = "id-4", label = "test4", value = "test", cursorIndex = 3, type = "TEXT", position = 4))
        settingsViewModel!!.updateShortcutPositions(shortcutList)
        verify(repository).updateShortcutPositions(shortcutList)
    }

    @Test
    fun testRepositoryCalledWhenRemoveCallbackCalled(): Unit = runBlocking {
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        settingsViewModel = SettingsViewModel(repository, buildMock, { _: String -> }, testDispatcher)
        settingsViewModel!!.removeCallback("uuid-test")
        verify(repository).removeShortcut("uuid-test")
    }

    @Test
    fun testRepositoryCalledWhenUpdateShortcutCalled(): Unit = runBlocking {
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        settingsViewModel = SettingsViewModel(repository, buildMock, { _: String -> }, testDispatcher)
        settingsViewModel!!.updateShortcut("uuid-test", "test", "testTExt", 0, 1, "TEXT")
        verify(repository).updateShortcut("uuid-test", "test", "testTExt", 0, 1, "TEXT")
    }

    @Test
    fun testRepositoryCalledWhenBulkAddShortcutsCalled(): Unit = runBlocking {
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        settingsViewModel = SettingsViewModel(repository, buildMock, { _: String -> }, testDispatcher)
        val shortcut = listOf(arrayOf("hello,hello,1,TEXT"))
        settingsViewModel!!.bulkAddShortcuts(shortcut)
        verify(repository).bulkAddShortcuts(shortcut)
    }

    @Test
    fun testRepositoryCalledWhenAddShortcutCalled(): Unit = runBlocking {
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        settingsViewModel = SettingsViewModel(repository, buildMock, { _: String -> }, testDispatcher)
        settingsViewModel!!.addShortcut("hello", "hello", 1, "TEXT")
        verify(repository).addShortcut("hello", "hello", 1, "TEXT")
    }

    @Test
    fun testRepositoryCalledWhenGetFilenameCalled() {
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        settingsViewModel = SettingsViewModel(repository, buildMock, { _: String -> }, testDispatcher)
        settingsViewModel!!.getFilename()
        verify(repository).retrieveFilename()
    }

    @Test
    fun testRepositoryCalledWhenGetAllShortcutsCalled() {
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        settingsViewModel = SettingsViewModel(repository, buildMock, { _: String -> }, testDispatcher)
        settingsViewModel!!.getAllShortcuts()
        verify(repository).getAllShortcuts()
    }

    @Test
    fun testRepositoryCalledWhenLabelExistsCalled() {
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        settingsViewModel = SettingsViewModel(repository, buildMock, { _: String -> }, testDispatcher)
        settingsViewModel!!.labelExists("test")
        verify(repository).labelExists("test")
    }

    @Test
    fun testExportReturnsErrorWhenNoFileSelected() {
        `when`(buildMock.isOreoOrGreater()).thenReturn(true)
        val settingsViewModel = SettingsViewModel(repository, buildMock, { _: String -> }, testDispatcher)
        val error = settingsViewModel.exportShortcuts()
        assertEquals("No export file selected", error?.message)
    }

    @Test
    fun testExportCallsRepository() {
        `when`(buildMock.isOreoOrGreater()).thenReturn(true)
        val settingsViewModel = SettingsViewModel(repository, buildMock, { _: String -> }, testDispatcher)
        val mockUri = mock(Uri::class.java)
        settingsViewModel.exportFileUri = mockUri
        val error = settingsViewModel.exportShortcuts()
        assertNull(error)
        verify(repository).exportShortcutsAsJson(mockUri)
    }

    @Test
    fun testExportReturnsErrorWhenRepositoryThrows() {
        `when`(buildMock.isOreoOrGreater()).thenReturn(true)
        val settingsViewModel = SettingsViewModel(repository, buildMock, { _: String -> }, testDispatcher)
        val mockUri = mock(Uri::class.java)
        settingsViewModel.exportFileUri = mockUri
        doThrow(RuntimeException("Export failed")).`when`(repository).exportShortcutsAsJson(mockUri)
        val error = settingsViewModel.exportShortcuts()
        assertNotNull(error)
        assertTrue(error?.message?.contains("Error:") == true)
    }

    @Test
    fun testExportBelowOreoReturnsError() {
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        val settingsViewModel = SettingsViewModel(repository, buildMock, { _: String -> }, testDispatcher)
        settingsViewModel.exportFileUri = mock(Uri::class.java)
        val error = settingsViewModel.exportShortcuts()
        assertEquals("Need OS of Oreo or greater to export to JSON", error?.message)
    }

    @Test
    fun testGetFilenameReturnsValueFromRepository() {
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        `when`(repository.retrieveFilename()).thenReturn("test_filename.md")
        settingsViewModel = SettingsViewModel(repository, buildMock, { _: String -> }, testDispatcher)
        val result = settingsViewModel!!.getFilename()
        assertEquals("test_filename.md", result)
        verify(repository).retrieveFilename()
    }

    @Test
    fun testCreateShortcutDialogViewModelReturnsCorrectInstance() {
        settingsViewModel = SettingsViewModel(repository, buildMock, { _: String -> }, testDispatcher)
        val dialogViewModel = settingsViewModel!!.createShortcutDialogViewModel()
        assertNotNull(dialogViewModel)
        assertTrue(dialogViewModel is ShortcutDialogViewModel)
    }

    @Test
    fun testImportShortcutsLegacyWithOreoCallsRepository(): Unit = runBlocking {
        `when`(buildMock.isOreoOrGreater()).thenReturn(true)
        val toastMessages = mutableListOf<String>()
        val mockUri = mock(Uri::class.java)
        val vm = SettingsViewModel(repository, buildMock, { msg -> toastMessages.add(msg) }, testDispatcher)
        vm.importShortcutsLegacy(mockUri)
        testDispatcher.scheduler.advanceUntilIdle()
        verify(repository).importShortcuts(mockUri)
    }

    @Test
    fun testImportShortcutsLegacyBelowOreoShowsToast(): Unit = runBlocking {
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        val toastMessages = mutableListOf<String>()
        val vm = SettingsViewModel(repository, buildMock, { msg -> toastMessages.add(msg) }, testDispatcher)
        vm.importShortcutsLegacy(mock(Uri::class.java))
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(toastMessages.contains("Need OS of Oreo or greater to import from CSV"))
    }

    @Test
    fun testImportShortcutsWithOreoCallsRepository(): Unit = runBlocking {
        `when`(buildMock.isOreoOrGreater()).thenReturn(true)
        val toastMessages = mutableListOf<String>()
        val mockUri = mock(Uri::class.java)
        val vm = SettingsViewModel(repository, buildMock, { msg -> toastMessages.add(msg) }, testDispatcher)
        vm.importShortcuts(mockUri)
        testDispatcher.scheduler.advanceUntilIdle()
        verify(repository).importShortcutsFromJson(mockUri)
    }

    @Test
    fun testImportShortcutsBelowOreoShowsToast(): Unit = runBlocking {
        `when`(buildMock.isOreoOrGreater()).thenReturn(false)
        val toastMessages = mutableListOf<String>()
        val vm = SettingsViewModel(repository, buildMock, { msg -> toastMessages.add(msg) }, testDispatcher)
        vm.importShortcuts(mock(Uri::class.java))
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(toastMessages.contains("Need OS of Oreo or greater to import from CSV"))
    }

    @Test
    fun testSettingsViewModelFactoryCreatesSettingsViewModel() {
        val factory = SettingsViewModelFactory(repository, buildMock, {}, testDispatcher)
        val vm = factory.create(SettingsViewModel::class.java)
        assertNotNull(vm)
        assertTrue(vm is SettingsViewModel)
    }

    @Test
    fun `bulkAddShortcuts shows toast when repository throws exception`(): Unit = runBlocking {
        val toastMessages = mutableListOf<String>()
        settingsViewModel = SettingsViewModel(repository, buildMock, { msg -> toastMessages.add(msg) }, testDispatcher)
        val shortcut = listOf(arrayOf("bad"))
        doThrow(IllegalArgumentException("Line 0: need exactly four values"))
            .`when`(repository).bulkAddShortcuts(shortcut)
        settingsViewModel!!.bulkAddShortcuts(shortcut)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(toastMessages.contains("Line 0: need exactly four values"))
    }

    @Test
    fun `bulkAddShortcuts does not show toast when repository succeeds`(): Unit = runBlocking {
        val toastMessages = mutableListOf<String>()
        settingsViewModel = SettingsViewModel(repository, buildMock, { msg -> toastMessages.add(msg) }, testDispatcher)
        val shortcut = listOf(arrayOf("hello", "hello", "1", "TEXT"))
        settingsViewModel!!.bulkAddShortcuts(shortcut)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(toastMessages.isEmpty())
    }
}
