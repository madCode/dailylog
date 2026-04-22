package com.app.dailylog.ui.settings

import com.app.dailylog.repository.RepositoryInterface
import junit.framework.TestCase
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class ShortcutDialogViewModelTest : TestCase() {

    fun testIsLabelValidDelegates() {
        val repository = mock(RepositoryInterface::class.java)
        val viewModel = ShortcutDialogViewModel(repository)
        viewModel.isLabelValid("myLabel", false)
        verify(repository).isLabelValid("myLabel", false)
    }

    fun testIsTextValidDelegates() {
        val repository = mock(RepositoryInterface::class.java)
        val viewModel = ShortcutDialogViewModel(repository)
        viewModel.isTextValid("someText")
        verify(repository).isTextValid("someText")
    }

    fun testValidateShortcutRowDelegates() {
        val repository = mock(RepositoryInterface::class.java)
        val row = arrayOf("a", "b", "1", "TEXT")
        // Use the same array reference for both stub and verify to avoid any() null issue
        org.mockito.Mockito.`when`(repository.validateShortcutRow(row, 0)).thenReturn(true)
        val viewModel = ShortcutDialogViewModel(repository)
        val result = viewModel.validateShortcutRow(row, 0)
        assertTrue(result)
        verify(repository).validateShortcutRow(row, 0)
    }
}