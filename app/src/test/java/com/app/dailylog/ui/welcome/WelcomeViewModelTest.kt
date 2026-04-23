package com.app.dailylog.ui.welcome

import com.app.dailylog.repository.RepositoryInterface
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class WelcomeViewModelTest {

    @Test
    fun testSaveFilenameDelegates() {
        val repository = mock(RepositoryInterface::class.java)
        val viewModel = WelcomeViewModel(repository) {}
        viewModel.saveFilename("content://test/file.md")
        verify(repository).storeFilename("content://test/file.md")
    }

    @Test
    fun testOpenLogViewInvokesCallback() {
        var called = false
        val repository = mock(RepositoryInterface::class.java)
        val viewModel = WelcomeViewModel(repository) { called = true }
        viewModel.openLogView()
        assertTrue(called)
    }
}