package org.aetherassembly.beforeitsgone.domain.repository

import kotlinx.coroutines.flow.Flow
import org.aetherassembly.beforeitsgone.domain.model.WasteLogEntry

interface WasteLogRepository {
    fun observeAll(): Flow<List<WasteLogEntry>>
    suspend fun insert(entry: WasteLogEntry)
    suspend fun deleteById(id: String)
    suspend fun deleteAll()
}
