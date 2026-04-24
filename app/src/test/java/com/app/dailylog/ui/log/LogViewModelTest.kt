package com.app.dailylog.ui.log

import com.app.dailylog.repository.RepositoryInterface
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
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.doNothing

@ExperimentalCoroutinesApi
class LogViewModelTest : TestCase() {

    private val testDispatcher = TestCoroutineDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun takeDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun `test cursor index gets set`() {
        val repository: RepositoryInterface = mock(RepositoryInterface::class.java)
        val index = 3
        doNothing().`when`(repository).setCursorIndex(index)
        val viewModel = LogViewModel(repository, testDispatcher)
        viewModel.saveCursorIndex(index)
        assertEquals(index, viewModel.cursorIndex)
    }

    @Test
    fun `test repository called when getLog called`() {
        val repository: RepositoryInterface = mock(RepositoryInterface::class.java)
        val viewModel = LogViewModel(repository, testDispatcher)
        viewModel.getLog()
        verify(repository).readFile(true)
    }

    @Test
    fun `test repository called when getAllShortcuts called`() {
        val repository: RepositoryInterface = mock(RepositoryInterface::class.java)
        val viewModel = LogViewModel(repository, testDispatcher)
        viewModel.getAllShortcuts()
        verify(repository).getAllShortcuts()
    }

    @Test
    fun `test repository called when force save called`(): Unit = runBlocking {
        val repository: RepositoryInterface = mock(RepositoryInterface::class.java)
        val viewModel = LogViewModel(repository, testDispatcher)
        viewModel.forceSave("hello")
        testDispatcher.scheduler.advanceUntilIdle()
        verify(repository).saveToFile("hello", true)
    }

    @Test
    fun `test repository called when smart save called`(): Unit = runBlocking {
        val repository: RepositoryInterface = mock(RepositoryInterface::class.java)
        val viewModel = LogViewModel(repository, testDispatcher)
        viewModel.smartSave("hello")
        testDispatcher.scheduler.advanceUntilIdle()
        verify(repository).saveToFile("hello", false)
    }

    @Test
    fun `loadedFileForFirstTime goes from false to true after first log load`() {
        val repository: RepositoryInterface = mock(RepositoryInterface::class.java)
        val viewModel = LogViewModel(repository, testDispatcher)
        viewModel.getLog()
        verify(repository).readFile(true)
        viewModel.getLog()
        verify(repository).readFile(false)
    }
}
