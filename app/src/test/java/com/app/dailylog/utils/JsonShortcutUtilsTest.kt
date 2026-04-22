package com.app.dailylog.utils

import com.app.dailylog.repository.Shortcut
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

class JsonShortcutUtilsTest {

    @Test
    fun testExportShortcutsAsJson() {
        val shortcuts = listOf(
            Shortcut("test1", "Test value 1", 5, "TEXT", 0),
            Shortcut("test2", "Test value 2", 10, "TEXT", 1)
        )
        val json = JsonShortcutUtils.exportShortcutsAsJson(shortcuts, 4)
        Assert.assertTrue(json.contains("\"schemaVersion\":4"))
        Assert.assertTrue(json.contains("\"shortcuts\""))
        Assert.assertTrue(json.contains("\"label\":\"test1\""))
        Assert.assertTrue(json.contains("\"label\":\"test2\""))
    }

    @Test
    fun testValidateJsonStructureValid() {
        val json = """{"schemaVersion": 5, "shortcuts": []}"""
        Assert.assertEquals(5, JsonShortcutUtils.validateJsonStructure(json))
    }

    @Test
    fun testValidateJsonStructureMissingVersion() {
        val json = """{"invalid": "json"}"""
        Assert.assertNull(JsonShortcutUtils.validateJsonStructure(json))
    }

    @Test
    fun testImportRoundtrip() {
        val shortcuts = listOf(
            Shortcut("test1", "Test value 1", 5, "TEXT", 0),
            Shortcut("test2", "Test value 2", 10, "TEXT", 1)
        )
        val json = JsonShortcutUtils.exportShortcutsAsJson(shortcuts, 4)
        val imported = runBlocking { JsonShortcutUtils.importShortcutsFromJson(json) }
        Assert.assertNotNull(imported)
        Assert.assertEquals(2, imported!!.size)
        Assert.assertEquals("test1", imported[0].label)
        Assert.assertEquals("Test value 1", imported[0].value)
        Assert.assertEquals(5, imported[0].cursorIndex)
        Assert.assertEquals("TEXT", imported[0].type)
    }

    @Test
    fun testImportInvalidJsonReturnsNull() {
        val result = runBlocking { JsonShortcutUtils.importShortcutsFromJson("not valid json") }
        Assert.assertNull(result)
    }

    @Test
    fun testImportMissingShortcutsKeyReturnsNull() {
        val result = runBlocking { JsonShortcutUtils.importShortcutsFromJson("""{"schemaVersion": 4}""") }
        Assert.assertNull(result)
    }
}