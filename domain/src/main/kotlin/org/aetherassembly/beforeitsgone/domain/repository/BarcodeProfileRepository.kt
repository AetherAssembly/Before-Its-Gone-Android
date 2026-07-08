package org.aetherassembly.beforeitsgone.domain.repository

import org.aetherassembly.beforeitsgone.domain.model.BarcodeProfile

interface BarcodeProfileRepository {
    suspend fun getByBarcode(barcode: String): BarcodeProfile?
    suspend fun upsert(profile: BarcodeProfile)
    suspend fun getAll(): List<BarcodeProfile>
}
