package org.aetherassembly.beforeitsgone.data.repository

import org.aetherassembly.beforeitsgone.data.local.db.dao.ItemHistoryDao
import org.aetherassembly.beforeitsgone.data.local.db.entity.ItemHistoryEntity
import org.aetherassembly.beforeitsgone.domain.model.ItemHistory
import org.aetherassembly.beforeitsgone.domain.repository.ItemHistoryRepository
import javax.inject.Inject

class ItemHistoryRepositoryImpl @Inject constructor(
    private val dao: ItemHistoryDao
) : ItemHistoryRepository {

    override suspend fun getByName(name: String): ItemHistory? =
        dao.getById(name.trim().lowercase())?.toDomain()

    override suspend fun upsert(entry: ItemHistory) =
        dao.upsert(entry.toEntity())

    override suspend fun getFrequent(limit: Int): List<ItemHistory> =
        dao.getFrequent(limit).map { it.toDomain() }
}

private fun ItemHistoryEntity.toDomain() = ItemHistory(
    id = id, name = name, barcode = barcode, location = location,
    shelfLifeDays = shelfLifeDays, category = category,
    lastUsedAt = lastUsedAt, useCount = useCount
)

private fun ItemHistory.toEntity() = ItemHistoryEntity(
    id = id, name = name, barcode = barcode, location = location,
    shelfLifeDays = shelfLifeDays, category = category,
    lastUsedAt = lastUsedAt, useCount = useCount
)
