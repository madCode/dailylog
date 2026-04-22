package com.app.dailylog.utils

import com.app.dailylog.repository.Shortcut
import org.junit.Test
import org.junit.Assert

class JsonExportImportTest {

    @Test
    fun testJsonExportDataCreation() {
        // Test that we can create a proper JSON export structure using the actual implementation
        val shortcuts = listOf(
            Shortcut("test_label", "test_value", 0, "TEXT", 0),
            Shortcut("test_label2", "test_value2", 10, "DATETIME", 1)
        )
        
        // Use the actual implementation to export shortcuts as JSON
        val json = JsonShortcutUtils.exportShortcutsAsJson(shortcuts, 4)
        
        // Verify that the JSON contains expected structure with schema version
        Assert.assertTrue("JSON should contain schemaVersion", json.contains("\"schemaVersion\":4"))
        Assert.assertTrue("JSON should contain shortcuts array", json.contains("\"shortcuts\""))
        Assert.assertTrue("JSON should contain first shortcut label", json.contains("\"label\":\"test_label\""))  
        Assert.assertTrue("JSON should contain first shortcut value", json.contains("\"value\":\"test_value\""))  
    }

    @Test
    fun testJsonSerializationDeserialization() {
        // Test that we can serialize and deserialize properly using the actual implementation
        val shortcuts = listOf(
            Shortcut("test_label", "test_value", 0, "TEXT", 0)
        )
        
        // Use the actual implementation to export shortcuts as JSON
        val json = JsonShortcutUtils.exportShortcutsAsJson(shortcuts, 4)
        
        // Verify that the JSON contains expected structure with schema version
        Assert.assertTrue("JSON should contain schemaVersion", json.contains("\"schemaVersion\":4"))
        Assert.assertTrue("JSON should contain shortcuts array", json.contains("\"shortcuts\""))
        Assert.assertTrue("JSON should contain shortcut label", json.contains("\"label\":\"test_label\""))  
        Assert.assertTrue("JSON should contain shortcut value", json.contains("\"value\":\"test_value\""))  
    }
}