package com.app.dailylog.ui.log

import com.app.dailylog.repository.RepositoryInterface
import com.app.dailylog.repository.Repository
import junit.framework.TestCase
import org.junit.Test
import org.mockito.Mockito.*


class LogViewModelTest : TestCase() {

    @Test
    fun `test cursor index gets set`() {
        val repository: RepositoryInterface = mock(RepositoryInterface::class.java)
        val index = 3
        doNothing().`when`(repository).setCursorIndex(index)
        val viewModel = LogViewModel(repository)
        viewModel.saveCursorIndex(index)
        assertEquals(index, viewModel.cursorIndex)
    }

    @Test
    fun `test repository called when getLog called`() {
        val repository: RepositoryInterface = mock(RepositoryInterface::class.java)
        val viewModel = LogViewModel(repository)
        viewModel.getLog()
        verify(repository).readFile(true)
    }

    @Test
    fun `test repository called when getAllShortcuts called`() {
        val repository: RepositoryInterface = mock(RepositoryInterface::class.java)
        val viewModel = LogViewModel(repository)
        viewModel.getAllShortcuts()
        verify(repository).getAllShortcuts()
    }

    @Test
    fun `test repository called when force save called`() {
        val repository: RepositoryInterface = mock(RepositoryInterface::class.java)
        val viewModel = LogViewModel(repository)
        viewModel.forceSave("hello")
        verify(repository).saveToFile("hello", true)
    }

    @Test
    fun `test repository called when smart save called`() {
        val repository: RepositoryInterface = mock(RepositoryInterface::class.java)
        val viewModel = LogViewModel(repository)
        viewModel.smartSave("hello")
        verify(repository).saveToFile("hello", false)
    }

    @Test
    fun `loadedFileForFirstTime goes from true to false after first log load`() {
        val repository: RepositoryInterface = mock(RepositoryInterface::class.java)
        val viewModel = LogViewModel(repository)
        viewModel.getLog()
        verify(repository).readFile(true)
        viewModel.getLog()
        verify(repository).readFile(false)
    }

}