package org.aetherassembly.beforeitsgone.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.aetherassembly.beforeitsgone.data.local.db.entity.ShoppingListItemEntity

@Dao
interface ShoppingListDao {
    @Query("SELECT * FROM shopping_list ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<ShoppingListItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ShoppingListItemEntity)

    @Query("UPDATE shopping_list SET checked = :checked WHERE id = :id")
    suspend fun setChecked(id: String, checked: Boolean)

    @Query("DELETE FROM shopping_list WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM shopping_list WHERE checked = 1")
    suspend fun deleteChecked()
}
