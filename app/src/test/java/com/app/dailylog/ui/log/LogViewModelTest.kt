package com.app.dailylog.ui.log

import com.app.dailylog.repository.RepositoryInterface
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.*


class LogViewModelTest {

    @Test
    fun testCursorIndexGetsSet() {
        val repository: RepositoryInterface = mock(RepositoryInterface::class.java)
        val index = 3
        doNothing().`when`(repository).setCursorIndex(index)
        val viewModel = LogViewModel(repository)
        viewModel.saveCursorIndex(index)
        assertEquals(index, viewModel.cursorIndex)
    }

    @Test
    fun testRepositoryCalledWhenGetLogCalled() {
        val repository: RepositoryInterface = mock(RepositoryInterface::class.java)
        val viewModel = LogViewModel(repository)
        viewModel.getLog()
        verify(repository).readFile(true)
    }

    @Test
    fun testRepositoryCalledWhenGetAllShortcutsCalled() {
        val repository: RepositoryInterface = mock(RepositoryInterface::class.java)
        val viewModel = LogViewModel(repository)
        viewModel.getAllShortcuts()
        verify(repository).getAllShortcuts()
    }

    @Test
    fun testRepositoryCalledWhenForceSaveCalled() {
        val repository: RepositoryInterface = mock(RepositoryInterface::class.java)
        val viewModel = LogViewModel(repository)
        viewModel.forceSave("hello")
        verify(repository).saveToFile("hello", true)
    }

    @Test
    fun testRepositoryCalledWhenSmartSaveCalled() {
        val repository: RepositoryInterface = mock(RepositoryInterface::class.java)
        val viewModel = LogViewModel(repository)
        viewModel.smartSave("hello")
        verify(repository).saveToFile("hello", false)
    }

    @Test
    fun testLoadedFileForFirstTimeGoesTrueToFalseAfterFirstLogLoad() {
        val repository: RepositoryInterface = mock(RepositoryInterface::class.java)
        val viewModel = LogViewModel(repository)
        viewModel.getLog()
        verify(repository).readFile(true)
        viewModel.getLog()
        verify(repository).readFile(false)
    }
}
