package org.aetherassembly.beforeitsgone.ui.screen.scanner

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class MlKitBarcodeAnalyzer(
    private val onBarcodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {
    private val scanner = BarcodeScanning.getClient()
    private val consumed = AtomicBoolean(false)

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run { imageProxy.close(); return }
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (!consumed.getAndSet(true)) {
                    barcodes.firstOrNull()?.rawValue?.let(onBarcodeDetected)
                }
            }
            .addOnCompleteListener { imageProxy.close() }
    }
}

class PlayBarcodeAnalyzerFactory @Inject constructor() : BarcodeAnalyzerFactory {
    override fun create(onBarcodeDetected: (String) -> Unit): ImageAnalysis.Analyzer =
        MlKitBarcodeAnalyzer(onBarcodeDetected)
}
