package org.aetherassembly.beforeitsgone.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.aetherassembly.beforeitsgone.data.local.db.converter.StringListConverter
import org.aetherassembly.beforeitsgone.data.local.db.dao.BarcodeProfileDao
import org.aetherassembly.beforeitsgone.data.local.db.dao.InventoryItemDao
import org.aetherassembly.beforeitsgone.data.local.db.dao.ItemHistoryDao
import org.aetherassembly.beforeitsgone.data.local.db.dao.ShoppingListDao
import org.aetherassembly.beforeitsgone.data.local.db.dao.WasteLogDao
import org.aetherassembly.beforeitsgone.data.local.db.entity.BarcodeProfileEntity
import org.aetherassembly.beforeitsgone.data.local.db.entity.InventoryItemEntity
import org.aetherassembly.beforeitsgone.data.local.db.entity.ItemHistoryEntity
import org.aetherassembly.beforeitsgone.data.local.db.entity.ShoppingListItemEntity
import org.aetherassembly.beforeitsgone.data.local.db.entity.WasteLogEntity

@Database(
    entities = [
        InventoryItemEntity::class,
        BarcodeProfileEntity::class,
        WasteLogEntity::class,
        ItemHistoryEntity::class,
        ShoppingListItemEntity::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun inventoryItemDao(): InventoryItemDao
    abstract fun barcodeProfileDao(): BarcodeProfileDao
    abstract fun wasteLogDao(): WasteLogDao
    abstract fun itemHistoryDao(): ItemHistoryDao
    abstract fun shoppingListDao(): ShoppingListDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `item_history` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `barcode` TEXT,
                        `location` TEXT NOT NULL,
                        `shelfLifeDays` INTEGER NOT NULL,
                        `category` TEXT,
                        `lastUsedAt` TEXT NOT NULL,
                        `useCount` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `shopping_list` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `quantity` REAL NOT NULL,
                        `checked` INTEGER NOT NULL,
                        `addedAt` TEXT NOT NULL,
                        `sourceItemId` TEXT,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }
    }
}
