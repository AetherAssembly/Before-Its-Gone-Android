package org.aetherassembly.beforeitsgone.data.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.aetherassembly.beforeitsgone.data.local.db.AppDatabase
import org.aetherassembly.beforeitsgone.data.local.db.dao.BarcodeProfileDao
import org.aetherassembly.beforeitsgone.data.local.db.dao.InventoryItemDao
import org.aetherassembly.beforeitsgone.data.local.db.dao.ItemHistoryDao
import org.aetherassembly.beforeitsgone.data.local.db.dao.ShoppingListDao
import org.aetherassembly.beforeitsgone.data.local.db.dao.WasteLogDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "before_its_gone.db")
            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
            .build()

    @Provides fun provideInventoryItemDao(db: AppDatabase): InventoryItemDao = db.inventoryItemDao()
    @Provides fun provideBarcodeProfileDao(db: AppDatabase): BarcodeProfileDao = db.barcodeProfileDao()
    @Provides fun provideWasteLogDao(db: AppDatabase): WasteLogDao = db.wasteLogDao()
    @Provides fun provideItemHistoryDao(db: AppDatabase): ItemHistoryDao = db.itemHistoryDao()
    @Provides fun provideShoppingListDao(db: AppDatabase): ShoppingListDao = db.shoppingListDao()
}
