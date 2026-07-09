package org.aetherassembly.beforeitsgone.ui.screen.scanner

import androidx.camera.core.ImageAnalysis

interface BarcodeAnalyzerFactory {
    fun create(onBarcodeDetected: (String) -> Unit): ImageAnalysis.Analyzer
}
