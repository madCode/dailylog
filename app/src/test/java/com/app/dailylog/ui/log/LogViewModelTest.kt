package com.app.dailylog.ui.log

import com.app.dailylog.repository.RepositoryInterface
import junit.framework.TestCase
import org.mockito.Mockito.*


class LogViewModelTest : TestCase() {

    fun `test cursor index gets set`() {
        val repository: RepositoryInterface = mock(RepositoryInterface::class.java)
        val index = 3
        doNothing().`when`(repository).setCursorIndex(index)
        val viewModel = LogViewModel(repository)
        viewModel.saveCursorIndex(index)
        assertEquals(index, viewModel.cursorIndex)
    }

    fun `test repository called when getLog called`() {
        val repository: RepositoryInterface = mock(RepositoryInterface::class.java)
        val viewModel = LogViewModel(repository)
        viewModel.getLog()
        verify(repository).readFile(true)
    }

    fun `test repository called when getAllShortcuts called`() {
        val repository: RepositoryInterface = mock(RepositoryInterface::class.java)
        val viewModel = LogViewModel(repository)
        viewModel.getAllShortcuts()
        verify(repository).getAllShortcuts()
    }

    fun `test repository called when force save called`() {
        val repository: RepositoryInterface = mock(RepositoryInterface::class.java)
        val viewModel = LogViewModel(repository)
        viewModel.forceSave("hello")
        verify(repository).saveToFile("hello", true)
    }

    fun `test repository called when smart save called`() {
        val repository: RepositoryInterface = mock(RepositoryInterface::class.java)
        val viewModel = LogViewModel(repository)
        viewModel.smartSave("hello")
        verify(repository).saveToFile("hello", false)
    }

    fun `loadedFileForFirstTime goes from true to false after first log load`() {
        val repository: RepositoryInterface = mock(RepositoryInterface::class.java)
        val viewModel = LogViewModel(repository)
        viewModel.getLog()
        verify(repository).readFile(true)
        viewModel.getLog()
        verify(repository).readFile(false)
    }

    fun `switching files loads the new file as a first load`() {
        val repository: RepositoryInterface = mock(RepositoryInterface::class.java)
        `when`(repository.filename).thenReturn("content://old")
        val viewModel = LogViewModel(repository)
        viewModel.getLog()
        `when`(repository.filename).thenReturn("content://new")
        viewModel.getLog()
        verify(repository, times(2)).readFile(true)
    }

}