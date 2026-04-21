package com.app.dailylog.utils

import android.util.Log
import com.app.dailylog.repository.Shortcut
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Utility class for JSON shortcut import/export operations
 */
class JsonShortcutUtils {
    companion object {
        private const val TAG = "JsonShortcutUtils"
        private val gson = Gson()

        /**
         * Export shortcuts as JSON with schema version
         */
        fun exportShortcutsAsJson(shortcuts: List<Shortcut>, schemaVersion: Int): String {
            val jsonObject = JsonObject()
            
            // Add schema version
            jsonObject.addProperty("schemaVersion", schemaVersion)
            
            // Add shortcuts array
            val shortcutsArray = JsonArray()
            for (shortcut in shortcuts) {
                val shortcutJson = JsonObject()
                shortcutJson.addProperty("label", shortcut.label)
                shortcutJson.addProperty("value", shortcut.value)
                shortcutJson.addProperty("cursorIndex", shortcut.cursorIndex)
                shortcutJson.addProperty("type", shortcut.type)
                shortcutJson.addProperty("position", shortcut.position)
                shortcutsArray.add(shortcutJson)
            }
            
            jsonObject.add("shortcuts", shortcutsArray)
            
            return gson.toJson(jsonObject)
        }

        /**
         * Import shortcuts from JSON string
         */
        suspend fun importShortcutsFromJson(jsonString: String): List<Shortcut>? {
            return withContext(Dispatchers.IO) {
                try {
                    val jsonObject = JsonParser.parseString(jsonString).asJsonObject
                    val shortcutsArray = jsonObject.getAsJsonArray("shortcuts")
                    
                    val shortcuts = mutableListOf<Shortcut>()
                    for (element in shortcutsArray) {
                        val shortcutJson = element.asJsonObject
                        val shortcut = Shortcut(
                            label = shortcutJson.get("label").asString,
                            value = shortcutJson.get("value").asString,
                            cursorIndex = shortcutJson.get("cursorIndex").asInt,
                            type = shortcutJson.get("type").asString,
                            position = shortcutJson.get("position").asInt
                        )
                        shortcuts.add(shortcut)
                    }
                    
                    shortcuts
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing JSON: ${e.message}")
                    null
                }
            }
        }

        /**
         * Validate JSON structure and extract schema version
         */
        fun validateJsonStructure(jsonString: String): Int? {
            return try {
                val jsonObject = JsonParser.parseString(jsonString).asJsonObject
                if (jsonObject.has("schemaVersion")) {
                    jsonObject.get("schemaVersion").asInt
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error validating JSON structure: ${e.message}")
                null
            }
        }
    }
}