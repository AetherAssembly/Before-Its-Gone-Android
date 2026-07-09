package org.aetherassembly.beforeitsgone.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.aetherassembly.beforeitsgone.data.local.preferences.SettingsDataStore
import org.aetherassembly.beforeitsgone.data.repository.BarcodeProfileRepositoryImpl
import org.aetherassembly.beforeitsgone.data.repository.InventoryRepositoryImpl
import org.aetherassembly.beforeitsgone.data.repository.ItemHistoryRepositoryImpl
import org.aetherassembly.beforeitsgone.data.repository.SettingsRepositoryImpl
import org.aetherassembly.beforeitsgone.data.repository.ShoppingListRepositoryImpl
import org.aetherassembly.beforeitsgone.data.repository.WasteLogRepositoryImpl
import org.aetherassembly.beforeitsgone.domain.repository.BarcodeProfileRepository
import org.aetherassembly.beforeitsgone.domain.repository.InventoryRepository
import org.aetherassembly.beforeitsgone.domain.repository.ItemHistoryRepository
import org.aetherassembly.beforeitsgone.domain.repository.SettingsRepository
import org.aetherassembly.beforeitsgone.domain.repository.ShoppingListRepository
import org.aetherassembly.beforeitsgone.domain.repository.WasteLogRepository
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindInventoryRepository(impl: InventoryRepositoryImpl): InventoryRepository

    @Binds @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds @Singleton
    abstract fun bindItemHistoryRepository(impl: ItemHistoryRepositoryImpl): ItemHistoryRepository

    @Binds @Singleton
    abstract fun bindBarcodeProfileRepository(impl: BarcodeProfileRepositoryImpl): BarcodeProfileRepository

    @Binds @Singleton
    abstract fun bindWasteLogRepository(impl: WasteLogRepositoryImpl): WasteLogRepository

    @Binds @Singleton
    abstract fun bindShoppingListRepository(impl: ShoppingListRepositoryImpl): ShoppingListRepository

    companion object {
        @Provides @Singleton
        fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
            context.dataStore
    }
}
