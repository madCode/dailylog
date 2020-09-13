package com.example.dailylog.entities

import androidx.room.*

@Entity
data class Shortcut(
    @PrimaryKey val label: String,
    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "cursorIndex") val cursorIndex: Int
)

@Dao
interface ShortcutDao {
    @Query("SELECT * FROM shortcut")
    fun getAll(): List<Shortcut>

    @Query("SELECT * FROM shortcut WHERE type = :type")
    fun getAllByType(type: String): List<Shortcut>

    @Insert
    fun insertAll(vararg shortcuts: Shortcut)

    @Delete
    fun delete(shortcut: Shortcut)

    @Query("DELETE FROM shortcut WHERE label = :label")
    fun deleteByLabel(label: String)
}