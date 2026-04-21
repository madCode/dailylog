package com.app.dailylog.utils

import com.app.dailylog.repository.Shortcut
import org.junit.Assert
import org.junit.Test

class SimpleJsonTest {

    @Test
    fun testJsonExportExists() {
        // Create test shortcuts
        val shortcuts = listOf(
            Shortcut("test1", "Test value 1", 5, "TEXT", 0)
        )

        // This should compile and run without errors
        val json = JsonShortcutUtils.exportShortcutsAsJson(shortcuts, 4)
        
        // Basic validation that it's valid JSON structure
        Assert.assertTrue("Should contain schemaVersion", json.contains("\"schemaVersion\":4"))
        Assert.assertTrue("Should contain shortcuts array", json.contains("\"shortcuts\""))
    }
}