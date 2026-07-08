package org.aetherassembly.beforeitsgone.domain.repository

import org.aetherassembly.beforeitsgone.domain.model.ItemHistory

interface ItemHistoryRepository {
    suspend fun getByName(name: String): ItemHistory?
    suspend fun upsert(entry: ItemHistory)
    suspend fun getFrequent(limit: Int = 5): List<ItemHistory>
}
