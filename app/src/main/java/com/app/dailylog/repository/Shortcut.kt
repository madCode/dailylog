package com.app.dailylog.repository

import androidx.lifecycle.LiveData
import androidx.room.*

object ShortcutType {
    const val TEXT = "TEXT"
    const val DATETIME = "DATETIME"
    fun validTypes(): Array<String> {
        return arrayOf(TEXT, DATETIME)
    }
    fun isTypeValid(type: String): Boolean? {
        return validTypes().contains(type)
    }
}

@Entity
data class Shortcut(
    @PrimaryKey val label: String,
    @ColumnInfo(name = "value") val value: String,
    @ColumnInfo(name = "cursorIndex") val cursorIndex: Int,
    @ColumnInfo(name = "type", defaultValue = ShortcutType.TEXT) var type: String,
    @ColumnInfo(name = "position") var position: Int,
)

@Dao
interface ShortcutDao {
    @Query("SELECT * FROM shortcut ORDER BY position ASC")
    fun getAll(): LiveData<List<Shortcut>>

    @Update
    suspend fun updateAll(vararg shortcuts: Shortcut)

    @Insert
    suspend fun add(shortcut: Shortcut)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addAll(vararg shortcuts: Shortcut)

    @Query("DELETE FROM shortcut WHERE label = :label")
    suspend fun deleteByLabel(label: String)

    @Query("SELECT EXISTS(SELECT * FROM shortcut WHERE label = :label)")
    fun labelExists(label: String): LiveData<Boolean>

    @Query("SELECT EXISTS(SELECT * FROM shortcut WHERE label = :label)")
    suspend fun labelExistsSuspend(label: String): Boolean
}