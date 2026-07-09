package org.aetherassembly.beforeitsgone.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.aetherassembly.beforeitsgone.data.local.db.entity.WasteLogEntity

@Dao
interface WasteLogDao {
    @Query("SELECT * FROM waste_log ORDER BY wastedAt DESC")
    fun observeAll(): Flow<List<WasteLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WasteLogEntity)

    @Query("DELETE FROM waste_log WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM waste_log")
    suspend fun deleteAll()
}
