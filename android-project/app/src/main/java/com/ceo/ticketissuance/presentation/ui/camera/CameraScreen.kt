package com.ceo.ticketissuance.presentation.ui.camera

import android.Manifest
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    onNavigateBack: () -> Unit,
    onNavigateToObservationForm: (String) -> Unit,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        if (cameraPermissionState.status != PermissionStatus.Granted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Number Plate Recognition") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (cameraPermissionState.status) {
            PermissionStatus.Granted -> {
                CameraContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    uiState = uiState,
                    onCapturePhoto = { imageCapture ->
                        viewModel.capturePhoto(imageCapture)
                    },
                    onToggleFlash = { viewModel.toggleFlash() },
                    onPlateDetected = { plate ->
                        onNavigateToObservationForm(plate)
                    },
                    onTextRecognized = { text ->
                        viewModel.processRecognizedText(text)
                    }
                )
            }
            is PermissionStatus.Denied -> {
                PermissionDeniedContent(
                    onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
                )
            }
        }
    }
}

@Composable
private fun CameraContent(
    modifier: Modifier = Modifier,
    uiState: CameraUiState,
    onCapturePhoto: (ImageCapture) -> Unit,
    onToggleFlash: () -> Unit,
    onPlateDetected: (String) -> Unit,
    onTextRecognized: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var previewView: PreviewView? by remember { mutableStateOf(null) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var cameraProvider: ProcessCameraProvider? by remember { mutableStateOf(null) }
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    
    LaunchedEffect(uiState.detectedPlate) {
        uiState.detectedPlate?.let { plate ->
            onPlateDetected(plate)
        }
    }
    
    Box(modifier = modifier) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    previewView = this
                    setupCamera(
                        context = ctx,
                        previewView = this,
                        lifecycleOwner = lifecycleOwner,
                        onImageCaptureReady = { capture -> imageCapture = capture },
                        onCameraProviderReady = { provider -> cameraProvider = provider },
                        onTextRecognized = onTextRecognized
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Overlay for number plate detection area
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawPlateDetectionOverlay(uiState.detectedPlate != null)
        }
        
        // Detection status card
        if (uiState.detectedPlate != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Number Plate Detected",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = uiState.detectedPlate!!,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // Camera controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flash toggle
            IconButton(
                onClick = onToggleFlash,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            ) {
                Icon(
                    imageVector = if (uiState.isFlashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = if (uiState.isFlashEnabled) "Flash On" else "Flash Off"
                )
            }
            
            // Capture button
            FloatingActionButton(
                onClick = { 
                    imageCapture?.let { onCapturePhoto(it) }
                },
                modifier = Modifier.size(72.dp),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    Icons.Default.Camera,
                    contentDescription = "Capture Photo",
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // Placeholder for symmetry
            Box(modifier = Modifier.size(56.dp))
        }
    }
}

@Composable
private fun PermissionDeniedContent(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Camera Permission Required",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Text(
            text = "This app needs camera access to detect number plates for parking enforcement.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        FloatingActionButton(
            onClick = onRequestPermission
        ) {
            Text("Grant Permission")
        }
    }
}

private fun DrawScope.drawPlateDetectionOverlay(isPlateDetected: Boolean) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    val overlayWidth = size.width * 0.8f
    val overlayHeight = overlayWidth * 0.3f
    
    val rect = androidx.compose.ui.geometry.Rect(
        offset = Offset(
            x = centerX - overlayWidth / 2,
            y = centerY - overlayHeight / 2
        ),
        size = Size(overlayWidth, overlayHeight)
    )
    
    // Draw overlay background
    drawRect(
        color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f),
        topLeft = Offset.Zero,
        size = size
    )
    
    // Clear the detection area
    drawRect(
        color = androidx.compose.ui.graphics.Color.Transparent,
        topLeft = rect.topLeft,
        size = rect.size,
        blendMode = androidx.compose.ui.graphics.BlendMode.Clear
    )
    
    // Draw detection frame
    val strokeColor = if (isPlateDetected) {
        androidx.compose.ui.graphics.Color.Green
    } else {
        androidx.compose.ui.graphics.Color.White
    }
    
    drawRect(
        color = strokeColor,
        topLeft = rect.topLeft,
        size = rect.size,
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
    )
    
    // Draw corner indicators
    val cornerLength = 30.dp.toPx()
    val corners = listOf(
        rect.topLeft,
        Offset(rect.topRight.x, rect.topLeft.y),
        Offset(rect.bottomLeft.x, rect.bottomRight.y),
        rect.bottomRight
    )
    
    corners.forEach { corner ->
        when (corner) {
            rect.topLeft -> {
                drawLine(strokeColor, corner, Offset(corner.x + cornerLength, corner.y), 6.dp.toPx())
                drawLine(strokeColor, corner, Offset(corner.x, corner.y + cornerLength), 6.dp.toPx())
            }
            Offset(rect.topRight.x, rect.topLeft.y) -> {
                drawLine(strokeColor, corner, Offset(corner.x - cornerLength, corner.y), 6.dp.toPx())
                drawLine(strokeColor, corner, Offset(corner.x, corner.y + cornerLength), 6.dp.toPx())
            }
            Offset(rect.bottomLeft.x, rect.bottomRight.y) -> {
                drawLine(strokeColor, corner, Offset(corner.x + cornerLength, corner.y), 6.dp.toPx())
                drawLine(strokeColor, corner, Offset(corner.x, corner.y - cornerLength), 6.dp.toPx())
            }
            rect.bottomRight -> {
                drawLine(strokeColor, corner, Offset(corner.x - cornerLength, corner.y), 6.dp.toPx())
                drawLine(strokeColor, corner, Offset(corner.x, corner.y - cornerLength), 6.dp.toPx())
            }
        }
    }
}

private fun setupCamera(
    context: Context,
    previewView: PreviewView,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    onImageCaptureReady: (ImageCapture) -> Unit,
    onCameraProviderReady: (ProcessCameraProvider) -> Unit,
    onTextRecognized: (String) -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()
        onCameraProviderReady(cameraProvider)
        
        val preview = Preview.Builder().build()
        preview.setSurfaceProvider(previewView.surfaceProvider)
        
        val imageCapture = ImageCapture.Builder().build()
        onImageCaptureReady(imageCapture)
        
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(
                    ContextCompat.getMainExecutor(context),
                    TextRecognitionAnalyzer { text ->
                        onTextRecognized(text)
                    }
                )
            }
        
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture,
                imageAnalysis
            )
        } catch (exc: Exception) {
            Toast.makeText(context, "Camera initialization failed: ${exc.message}", Toast.LENGTH_SHORT).show()
        }
    }, ContextCompat.getMainExecutor(context))
}