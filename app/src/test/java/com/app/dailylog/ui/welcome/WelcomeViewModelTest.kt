package com.app.dailylog.ui.welcome

import com.app.dailylog.repository.RepositoryInterface
import junit.framework.TestCase
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class WelcomeViewModelTest : TestCase() {

    fun testSaveFilenameDelegates() {
        val repository = mock(RepositoryInterface::class.java)
        val viewModel = WelcomeViewModel(repository) {}
        viewModel.saveFilename("content://test/file.md")
        verify(repository).storeFilename("content://test/file.md")
    }

    fun testOpenLogViewInvokesCallback() {
        var called = false
        val repository = mock(RepositoryInterface::class.java)
        val viewModel = WelcomeViewModel(repository) { called = true }
        viewModel.openLogView()
        assertTrue(called)
    }
}