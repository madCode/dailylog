package com.app.dailylog.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class FileNameTemplateTest {

    private val clock = Clock.fixed(Instant.parse("2026-10-03T09:00:00Z"), ZoneOffset.UTC)

    @Test
    fun resolvesEachTokenSeparately() {
        assertEquals(
            "2026/10-03-journal.md",
            FileNameTemplate.resolve("{DATETIME: yyyy}/{DATETIME: MM-dd}-journal.md", clock)
        )
    }

    @Test
    fun theDefaultNamesTodaysFile() {
        assertEquals("2026-10-03-journal.md", FileNameTemplate.resolve(FileNameTemplate.DEFAULT, clock))
    }

    @Test
    fun acceptsANameWithoutADate() {
        assertNull(FileNameTemplate.error("journal.md", clock))
    }

    @Test
    fun rejectsNamesThatCantBeFiles() {
        assertNotNull(FileNameTemplate.error("", clock))
        assertNotNull(FileNameTemplate.error("{DATETIME: yyyy}/", clock))
        assertNotNull(FileNameTemplate.error("../{DATETIME: yyyy}.md", clock))
        assertNotNull(FileNameTemplate.error("{DATETIME: yyyy}//log.md", clock))
    }

    @Test
    fun rejectsBadDatePatterns() {
        assertNotNull(FileNameTemplate.error("{DATETIME: bbbb}.md", clock))
        // A time zone can't be read from the local date and time the name is made from.
        assertNotNull(FileNameTemplate.error("{DATETIME: z}.md", clock))
    }

    @Test
    fun aPresetKeepsTheRestOfTheName() {
        assertEquals(
            "{DATETIME: dd-MM-yyyy}-fitness.txt",
            FileNameTemplate.withDatePart("{DATETIME: yyyy}/{DATETIME: MM-dd}-fitness.txt", "{DATETIME: dd-MM-yyyy}")
        )
        assertEquals(
            "{DATETIME: yyyy-MM-dd}-notes.md",
            FileNameTemplate.withDatePart("notes.md", "{DATETIME: yyyy-MM-dd}")
        )
        assertEquals(
            "{DATETIME: yyyy-MM-dd}-journal.md",
            FileNameTemplate.withDatePart("{DATETIME: yyyy}", "{DATETIME: yyyy-MM-dd}")
        )
    }
}
