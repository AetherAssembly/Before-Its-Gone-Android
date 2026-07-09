package org.aetherassembly.beforeitsgone.ui.screen.scanner

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScannerScreen(
    onBarcodeDetected: (String) -> Unit,
    onClose: () -> Unit,
    viewModel: BarcodeScannerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var torchEnabled by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<androidx.camera.core.Camera?>(null) }
    var manualBarcode by remember { mutableStateOf("") }
    var detected by remember { mutableStateOf(false) }
    val previewView = remember { PreviewView(context) }

    if (hasCameraPermission) {
        LaunchedEffect(Unit) {
            val provider = suspendCoroutine<ProcessCameraProvider> { cont ->
                ProcessCameraProvider.getInstance(context).also { future ->
                    future.addListener(
                        { cont.resume(future.get()) },
                        ContextCompat.getMainExecutor(context)
                    )
                }
            }

            val preview = Preview.Builder().build()
                .also { it.surfaceProvider = previewView.surfaceProvider }

            val analyzer = viewModel.createAnalyzer { barcode ->
                if (!detected) {
                    detected = true
                    onBarcodeDetected(barcode)
                }
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { it.setAnalyzer(ContextCompat.getMainExecutor(context), analyzer) }

            provider.unbindAll()
            camera = provider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalysis
            )
        }

        LaunchedEffect(torchEnabled) {
            camera?.cameraControl?.enableTorch(torchEnabled)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        if (!hasCameraPermission) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Camera permission is required for scanning.",
                    color = Color.White,
                    modifier = Modifier.padding(32.dp)
                )
            }
        }

        // Close
        IconButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close scanner", tint = Color.White)
        }

        // Torch
        if (hasCameraPermission) {
            IconButton(
                onClick = { torchEnabled = !torchEnabled },
                modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
            ) {
                Icon(
                    if (torchEnabled) Icons.Default.FlashOff else Icons.Default.FlashOn,
                    contentDescription = "Toggle flashlight",
                    tint = Color.White
                )
            }
        }

        // Manual entry
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = manualBarcode,
                onValueChange = { manualBarcode = it },
                label = { Text("Enter barcode manually", color = Color.White.copy(alpha = 0.7f)) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
                )
            )
            Button(
                onClick = {
                    val code = manualBarcode.trim()
                    if (code.isNotEmpty() && !detected) {
                        detected = true
                        onBarcodeDetected(code)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = manualBarcode.isNotBlank()
            ) {
                Text("Use this barcode")
            }
        }
    }
}
