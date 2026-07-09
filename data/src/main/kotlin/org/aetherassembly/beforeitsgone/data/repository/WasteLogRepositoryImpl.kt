package org.aetherassembly.beforeitsgone.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.aetherassembly.beforeitsgone.data.local.db.dao.WasteLogDao
import org.aetherassembly.beforeitsgone.data.local.db.entity.WasteLogEntity
import org.aetherassembly.beforeitsgone.domain.model.WasteLogEntry
import org.aetherassembly.beforeitsgone.domain.repository.WasteLogRepository
import javax.inject.Inject

class WasteLogRepositoryImpl @Inject constructor(
    private val dao: WasteLogDao
) : WasteLogRepository {

    override fun observeAll(): Flow<List<WasteLogEntry>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun insert(entry: WasteLogEntry) = dao.insert(entry.toEntity())
    override suspend fun deleteById(id: String) = dao.deleteById(id)
    override suspend fun deleteAll() = dao.deleteAll()
}

private fun WasteLogEntity.toDomain() = WasteLogEntry(
    id = id,
    itemName = itemName,
    quantity = quantity,
    location = location,
    category = category,
    expiresAt = expiresAt,
    wastedAt = wastedAt
)

private fun WasteLogEntry.toEntity() = WasteLogEntity(
    id = id,
    itemName = itemName,
    quantity = quantity,
    location = location,
    category = category,
    expiresAt = expiresAt,
    wastedAt = wastedAt
)
