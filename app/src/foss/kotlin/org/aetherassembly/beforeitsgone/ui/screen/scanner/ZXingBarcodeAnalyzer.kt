package org.aetherassembly.beforeitsgone.ui.screen.scanner

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class ZXingBarcodeAnalyzer(
    private val onBarcodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {
    private val reader = MultiFormatReader()
    private val consumed = AtomicBoolean(false)

    override fun analyze(imageProxy: ImageProxy) {
        try {
            if (consumed.get()) return
            val plane = imageProxy.planes[0]
            val data = ByteArray(plane.buffer.remaining()).also { plane.buffer.get(it) }
            val source = PlanarYUVLuminanceSource(
                data,
                imageProxy.width, imageProxy.height,
                0, 0,
                imageProxy.width, imageProxy.height,
                false
            )
            val bitmap = BinaryBitmap(HybridBinarizer(source))
            try {
                val result = reader.decode(bitmap)
                if (!consumed.getAndSet(true)) {
                    onBarcodeDetected(result.text)
                }
            } catch (_: NotFoundException) {
                // no barcode in this frame, try next
            }
        } finally {
            imageProxy.close()
        }
    }
}

class FossBarcodeAnalyzerFactory @Inject constructor() : BarcodeAnalyzerFactory {
    override fun create(onBarcodeDetected: (String) -> Unit): ImageAnalysis.Analyzer =
        ZXingBarcodeAnalyzer(onBarcodeDetected)
}
