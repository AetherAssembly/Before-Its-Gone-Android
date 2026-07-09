package org.aetherassembly.beforeitsgone.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.aetherassembly.beforeitsgone.data.local.db.dao.ShoppingListDao
import org.aetherassembly.beforeitsgone.data.local.db.entity.ShoppingListItemEntity
import org.aetherassembly.beforeitsgone.domain.model.ShoppingListItem
import org.aetherassembly.beforeitsgone.domain.repository.ShoppingListRepository
import javax.inject.Inject

class ShoppingListRepositoryImpl @Inject constructor(
    private val dao: ShoppingListDao
) : ShoppingListRepository {

    override fun observeAll(): Flow<List<ShoppingListItem>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun upsert(item: ShoppingListItem) = dao.upsert(item.toEntity())
    override suspend fun setChecked(id: String, checked: Boolean) = dao.setChecked(id, checked)
    override suspend fun deleteById(id: String) = dao.deleteById(id)
    override suspend fun deleteChecked() = dao.deleteChecked()
}

private fun ShoppingListItemEntity.toDomain() = ShoppingListItem(
    id = id,
    name = name,
    quantity = quantity,
    checked = checked,
    addedAt = addedAt,
    sourceItemId = sourceItemId
)

private fun ShoppingListItem.toEntity() = ShoppingListItemEntity(
    id = id,
    name = name,
    quantity = quantity,
    checked = checked,
    addedAt = addedAt,
    sourceItemId = sourceItemId
)
