package org.aetherassembly.beforeitsgone.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.aetherassembly.beforeitsgone.data.local.db.entity.InventoryItemEntity

@Dao
interface InventoryItemDao {
    @Query("SELECT * FROM inventory_items ORDER BY expiresAt ASC")
    fun observeAll(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items WHERE id = :id")
    suspend fun getById(id: String): InventoryItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: InventoryItemEntity)

    @Query("DELETE FROM inventory_items WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM inventory_items")
    suspend fun deleteAll()
}
