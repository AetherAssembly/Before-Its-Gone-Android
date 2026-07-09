package org.aetherassembly.beforeitsgone.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.aetherassembly.beforeitsgone.data.local.db.dao.InventoryItemDao
import org.aetherassembly.beforeitsgone.data.local.db.entity.InventoryItemEntity
import org.aetherassembly.beforeitsgone.domain.model.InventoryItem
import org.aetherassembly.beforeitsgone.domain.repository.InventoryRepository
import javax.inject.Inject

class InventoryRepositoryImpl @Inject constructor(
    private val dao: InventoryItemDao
) : InventoryRepository {

    override fun getAll(): Flow<List<InventoryItem>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: String): InventoryItem? =
        dao.getById(id)?.toDomain()

    override suspend fun upsert(item: InventoryItem) =
        dao.upsert(item.toEntity())

    override suspend fun delete(id: String) = dao.deleteById(id)

    override suspend fun deleteAll() = dao.deleteAll()
}

private fun InventoryItemEntity.toDomain() = InventoryItem(
    id = id, name = name, quantity = quantity, location = location,
    barcode = barcode, expiresAt = expiresAt, shelfLifeDays = shelfLifeDays,
    createdAt = createdAt, updatedAt = updatedAt, category = category,
    depletionThreshold = depletionThreshold, tags = tags, recurring = recurring,
    restockQuantity = restockQuantity, photoPath = photoPath
)

private fun InventoryItem.toEntity() = InventoryItemEntity(
    id = id, name = name, quantity = quantity, location = location,
    barcode = barcode, expiresAt = expiresAt, shelfLifeDays = shelfLifeDays,
    createdAt = createdAt, updatedAt = updatedAt, category = category,
    depletionThreshold = depletionThreshold, tags = tags, recurring = recurring,
    restockQuantity = restockQuantity, photoPath = photoPath
)
