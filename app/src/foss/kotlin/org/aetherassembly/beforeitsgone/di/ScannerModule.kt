package org.aetherassembly.beforeitsgone.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.aetherassembly.beforeitsgone.ui.screen.scanner.BarcodeAnalyzerFactory
import org.aetherassembly.beforeitsgone.ui.screen.scanner.FossBarcodeAnalyzerFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ScannerModule {
    @Binds
    @Singleton
    abstract fun bindBarcodeAnalyzerFactory(impl: FossBarcodeAnalyzerFactory): BarcodeAnalyzerFactory
}
