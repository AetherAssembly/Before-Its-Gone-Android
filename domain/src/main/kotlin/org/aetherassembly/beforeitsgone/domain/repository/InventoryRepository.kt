package org.aetherassembly.beforeitsgone.domain.repository

import kotlinx.coroutines.flow.Flow
import org.aetherassembly.beforeitsgone.domain.model.InventoryItem

interface InventoryRepository {
    fun getAll(): Flow<List<InventoryItem>>
    suspend fun getById(id: String): InventoryItem?
    suspend fun upsert(item: InventoryItem)
    suspend fun delete(id: String)
    suspend fun deleteAll()
}
