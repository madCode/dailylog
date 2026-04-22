package com.app.dailylog.utils

import com.app.dailylog.repository.Shortcut
import org.junit.Assert
import org.junit.Test

class JsonShortcutUtilsTest {

    @Test
    fun testExportShortcutsAsJson() {
        // Create test shortcuts
        val shortcuts = listOf(
            Shortcut("test1", "Test value 1", 5, "TEXT", 0),
            Shortcut("test2", "Test value 2", 10, "TEXT", 1)
        )

        // Export to JSON
        val json = JsonShortcutUtils.exportShortcutsAsJson(shortcuts, 4)
        
        // Verify JSON contains expected structure
        Assert.assertTrue("JSON should contain schemaVersion", json.contains("\"schemaVersion\":4"))
        Assert.assertTrue("JSON should contain shortcuts array", json.contains("\"shortcuts\""))
        Assert.assertTrue("JSON should contain first shortcut label", json.contains("\"label\":\"test1\""))
        Assert.assertTrue("JSON should contain second shortcut label", json.contains("\"label\":\"test2\""))
    }

    @Test
    fun testValidateJsonStructure() {
        // Test valid JSON with schema version
        val validJson = """
            {
                "schemaVersion": 5,
                "shortcuts": []
            }
        """.trimIndent()
        
        val schemaVersion = JsonShortcutUtils.validateJsonStructure(validJson)
        Assert.assertEquals("Schema version should be 5", 5, schemaVersion)
        
        // Test invalid JSON
        val invalidJson = """
            {
                "invalid": "json"
            }
        """.trimIndent()
        
        val invalidSchemaVersion = JsonShortcutUtils.validateJsonStructure(invalidJson)
        Assert.assertNull("Should return null for invalid JSON", invalidSchemaVersion)
    }
}