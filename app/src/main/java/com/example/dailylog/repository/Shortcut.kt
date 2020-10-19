package com.example.dailylog.repository

import androidx.room.*

@Entity
data class Shortcut(
    @PrimaryKey val label: String,
    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "cursorIndex") val cursorIndex: Int,
    @ColumnInfo(name = "position") var position: Int
)

@Dao
interface ShortcutDao {
    @Query("SELECT * FROM shortcut ORDER BY position ASC")
    fun getAll(): List<Shortcut>

    @Update
    fun updateAll(vararg shortcuts: Shortcut)

    @Insert
    fun add(shortcut: Shortcut)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun addAll(vararg shortcuts: Shortcut)

    @Delete
    fun delete(shortcut: Shortcut)

    @Query("DELETE FROM shortcut WHERE label = :label")
    fun deleteByLabel(label: String)
}