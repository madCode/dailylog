package com.example.dailylog.ui.log

import com.example.dailylog.repository.Repository
import junit.framework.TestCase
import org.junit.Test
import org.mockito.Mockito.*


class LogViewModelTest : TestCase() {

    @Test
    fun `test cursor index gets set`() {
        val repository: Repository = mock(Repository::class.java)
        val index = 3
        doNothing().`when`(repository).setCursorIndex(index)
        val viewModel = LogViewModel(repository)
        viewModel.saveCursorIndex(index)
        assertEquals(index, viewModel.cursorIndex)
    }

    @Test
    fun `test repository called when getLog called`() {
        val repository: Repository = mock(Repository::class.java)
        val viewModel = LogViewModel(repository)
        viewModel.getLog()
        verify(repository).readFile()
    }

    @Test
    fun `test repository called when getAllShortcuts called`() {
        val repository: Repository = mock(Repository::class.java)
        val viewModel = LogViewModel(repository)
        viewModel.getAllShortcuts()
        verify(repository).getAllShortcuts()
    }

    @Test
    fun `test repository called when save called`() {
        val repository: Repository = mock(Repository::class.java)
        val viewModel = LogViewModel(repository)
        viewModel.save("hello")
        verify(repository).saveToFile("hello")
    }
}