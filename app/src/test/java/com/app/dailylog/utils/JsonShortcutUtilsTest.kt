package com.app.dailylog.utils

import com.app.dailylog.repository.Shortcut
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test

class JsonShortcutUtilsTest {

    @Test
    fun testExportShortcutsAsJson() {
        val shortcuts = listOf(
            Shortcut("id-1", "test1", "Test value 1", 5, "TEXT", 0),
            Shortcut("id-2", "test2", "Test value 2", 10, "TEXT", 1)
        )
        val json = JsonShortcutUtils.exportShortcutsAsJson(shortcuts, 4)
        Assert.assertTrue(json.contains("\"schemaVersion\":4"))
        Assert.assertTrue(json.contains("\"shortcuts\""))
        Assert.assertTrue(json.contains("\"label\":\"test1\""))
        Assert.assertTrue(json.contains("\"label\":\"test2\""))
        Assert.assertTrue(json.contains("\"id\":\"id-1\""))
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
    fun testValidateJsonStructureInvalidJsonReturnsNull() {
        Assert.assertNull(JsonShortcutUtils.validateJsonStructure("not valid json"))
    }

    @Test
    fun testValidateJsonStructureEmptyStringReturnsNull() {
        Assert.assertNull(JsonShortcutUtils.validateJsonStructure(""))
    }

    @Test
    fun testImportRoundtrip() {
        val shortcuts = listOf(
            Shortcut("id-1", "test1", "Test value 1", 5, "TEXT", 0),
            Shortcut("id-2", "test2", "Test value 2", 10, "TEXT", 1)
        )
        val json = JsonShortcutUtils.exportShortcutsAsJson(shortcuts, 4)
        val imported = runBlocking { JsonShortcutUtils.importShortcutsFromJson(json) }
        Assert.assertNotNull(imported)
        Assert.assertEquals(2, imported!!.size)
        Assert.assertEquals("test1", imported[0].label)
        Assert.assertEquals("id-1", imported[0].id)
        Assert.assertEquals("Test value 1", imported[0].value)
        Assert.assertEquals(5, imported[0].cursorIndex)
        Assert.assertEquals("TEXT", imported[0].type)
    }

    @Test
    fun testImportWithoutIdGeneratesUuid() {
        val json = """{"schemaVersion":4,"shortcuts":[{"label":"lbl","value":"val","cursorIndex":0,"type":"TEXT","position":0}]}"""
        val imported = runBlocking { JsonShortcutUtils.importShortcutsFromJson(json) }
        Assert.assertNotNull(imported)
        Assert.assertEquals(1, imported!!.size)
        Assert.assertNotNull(imported[0].id)
        Assert.assertTrue(imported[0].id.isNotEmpty())
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