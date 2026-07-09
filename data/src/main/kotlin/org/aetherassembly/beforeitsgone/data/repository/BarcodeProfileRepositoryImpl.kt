package org.aetherassembly.beforeitsgone.data.repository

import org.aetherassembly.beforeitsgone.data.local.db.dao.BarcodeProfileDao
import org.aetherassembly.beforeitsgone.data.local.db.entity.BarcodeProfileEntity
import org.aetherassembly.beforeitsgone.domain.model.BarcodeProfile
import org.aetherassembly.beforeitsgone.domain.repository.BarcodeProfileRepository
import javax.inject.Inject

class BarcodeProfileRepositoryImpl @Inject constructor(
    private val dao: BarcodeProfileDao
) : BarcodeProfileRepository {

    override suspend fun getByBarcode(barcode: String): BarcodeProfile? =
        dao.getByBarcode(barcode)?.toDomain()

    override suspend fun upsert(profile: BarcodeProfile) =
        dao.upsert(profile.toEntity())

    override suspend fun getAll(): List<BarcodeProfile> =
        dao.getAll().map { it.toDomain() }
}

private fun BarcodeProfileEntity.toDomain() = BarcodeProfile(
    barcode = barcode,
    productName = productName,
    defaultShelfLifeDays = defaultShelfLifeDays,
    preferredLocation = preferredLocation,
    updatedAt = updatedAt,
    caloriesPer100g = caloriesPer100g,
    allergens = allergens
)

private fun BarcodeProfile.toEntity() = BarcodeProfileEntity(
    barcode = barcode,
    productName = productName,
    defaultShelfLifeDays = defaultShelfLifeDays,
    preferredLocation = preferredLocation,
    updatedAt = updatedAt,
    caloriesPer100g = caloriesPer100g,
    allergens = allergens
)
