package org.aetherassembly.beforeitsgone.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.aetherassembly.beforeitsgone.data.local.db.entity.BarcodeProfileEntity

@Dao
interface BarcodeProfileDao {
    @Query("SELECT * FROM barcode_profiles WHERE barcode = :barcode")
    suspend fun getByBarcode(barcode: String): BarcodeProfileEntity?

    @Query("SELECT * FROM barcode_profiles")
    suspend fun getAll(): List<BarcodeProfileEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(profile: BarcodeProfileEntity)
}
