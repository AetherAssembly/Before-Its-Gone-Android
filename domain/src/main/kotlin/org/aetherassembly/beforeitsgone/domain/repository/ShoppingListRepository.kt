package org.aetherassembly.beforeitsgone.domain.repository

import kotlinx.coroutines.flow.Flow
import org.aetherassembly.beforeitsgone.domain.model.ShoppingListItem

interface ShoppingListRepository {
    fun observeAll(): Flow<List<ShoppingListItem>>
    suspend fun upsert(item: ShoppingListItem)
    suspend fun setChecked(id: String, checked: Boolean)
    suspend fun deleteById(id: String)
    suspend fun deleteChecked()
}
