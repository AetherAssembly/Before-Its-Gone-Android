package org.aetherassembly.beforeitsgone.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.aetherassembly.beforeitsgone.data.local.db.entity.ItemHistoryEntity

@Dao
interface ItemHistoryDao {
    @Query("SELECT * FROM item_history WHERE id = :id")
    suspend fun getById(id: String): ItemHistoryEntity?

    @Query("SELECT * FROM item_history ORDER BY useCount DESC, lastUsedAt DESC LIMIT :limit")
    suspend fun getFrequent(limit: Int): List<ItemHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: ItemHistoryEntity)
}
