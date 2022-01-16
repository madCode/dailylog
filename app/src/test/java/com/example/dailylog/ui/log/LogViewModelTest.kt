package com.example.dailylog.ui.log

import com.example.dailylog.repository.Constants
import com.example.dailylog.repository.Repository
import junit.framework.TestCase
import org.junit.Test
import org.mockito.Mockito.*
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset


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
    fun `test date properly formatted using default date string`() {
        val clock = Clock.fixed(
            Instant.parse("2018-08-22T10:00:00Z"),
            ZoneOffset.UTC)
        val repository: Repository = mock(Repository::class.java)
        `when`(repository.getDateTimeFormat()).thenReturn(Constants.DATE_TIME_DEFAULT_FORMAT)
        val viewModel = LogViewModel(repository)
        viewModel.clock = clock
        val dateString = viewModel.getDateString()
        assertEquals("Wed 8-22-2018 10:00 AM PT", dateString)
    }

    @Test
    fun `test date properly formatted with timezone`() {
        val clock = Clock.fixed(
                Instant.parse("2018-08-22T10:00:00Z"),
                ZoneOffset.UTC)
        val repository: Repository = mock(Repository::class.java)
        `when`(repository.getDateTimeFormat()).thenReturn("E LLL-dd-yyyy h:mm a z")
        val viewModel = LogViewModel(repository)
        viewModel.clock = clock
        val dateString = viewModel.getDateString()
        assertEquals("Wed 8-22-2018 10:00 AM PT", dateString)
    }

    @Test
    fun `test print error message when date can't be formatted`() {
        val clock = Clock.fixed(
            Instant.parse("2018-08-22T10:00:00Z"),
            ZoneOffset.UTC)
        val repository: Repository = mock(Repository::class.java)
        `when`(repository.getDateTimeFormat()).thenReturn("EEEEEE")
        val viewModel = LogViewModel(repository)
        viewModel.clock = clock
        val dateString = viewModel.getDateString()
        assertEquals(
            "Issue with date time string. Please change Date Format in settings screen. Error message: Too many pattern letters: E",
            dateString)
    }

    @Test
    fun `test clock goes to system default when not passed in`() {
        val repository: Repository = mock(Repository::class.java)
        `when`(repository.getDateTimeFormat()).thenReturn("yyyy")
        val viewModel = LogViewModel(repository)
        assertEquals("2022", viewModel.getDateString())
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