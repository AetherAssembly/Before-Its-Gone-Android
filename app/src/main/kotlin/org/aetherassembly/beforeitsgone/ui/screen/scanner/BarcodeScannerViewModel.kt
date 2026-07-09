package org.aetherassembly.beforeitsgone.ui.screen.scanner

import androidx.camera.core.ImageAnalysis
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BarcodeScannerViewModel @Inject constructor(
    private val analyzerFactory: BarcodeAnalyzerFactory
) : ViewModel() {
    fun createAnalyzer(onBarcodeDetected: (String) -> Unit): ImageAnalysis.Analyzer =
        analyzerFactory.create(onBarcodeDetected)
}
