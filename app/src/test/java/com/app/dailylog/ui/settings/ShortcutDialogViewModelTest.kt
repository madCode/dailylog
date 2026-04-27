package com.app.dailylog.ui.settings

import com.app.dailylog.repository.RepositoryInterface
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class ShortcutDialogViewModelTest {

    @Test
    fun testIsLabelValidDelegates() {
        val repository = mock(RepositoryInterface::class.java)
        val viewModel = ShortcutDialogViewModel(repository)
        viewModel.isLabelValid("myLabel")
        verify(repository).isLabelValid("myLabel", null)
    }

    @Test
    fun testIsLabelValidWithExcludeIdDelegates() {
        val repository = mock(RepositoryInterface::class.java)
        val viewModel = ShortcutDialogViewModel(repository)
        viewModel.isLabelValid("myLabel", "some-uuid")
        verify(repository).isLabelValid("myLabel", "some-uuid")
    }

    @Test
    fun testIsTextValidDelegates() {
        val repository = mock(RepositoryInterface::class.java)
        val viewModel = ShortcutDialogViewModel(repository)
        viewModel.isTextValid("someText")
        verify(repository).isTextValid("someText")
    }

    @Test
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