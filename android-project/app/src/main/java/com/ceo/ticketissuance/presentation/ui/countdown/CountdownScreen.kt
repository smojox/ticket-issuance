package com.ceo.ticketissuance.presentation.ui.countdown

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ceo.ticketissuance.domain.model.ObservationTimer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountdownScreen(
    observationId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToIssuance: (Long) -> Unit,
    viewModel: CountdownViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCompleteDialog by remember { mutableStateOf(false) }
    
    // Initialize with observation ID
    remember(observationId) {
        viewModel.loadObservation(observationId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = uiState.observation?.vrm ?: "Observation",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.timer?.isCompleted == true) {
                ExtendedFloatingActionButton(
                    onClick = { onNavigateToIssuance(observationId) },
                    icon = { Icon(Icons.Default.Receipt, contentDescription = null) },
                    text = { Text("Issue Ticket") }
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                TimerDisplay(
                    timer = uiState.timer,
                    onPause = { viewModel.pauseTimer() },
                    onResume = { viewModel.resumeTimer() },
                    onStop = { showCompleteDialog = true }
                )
            }
            
            item {
                ObservationDetails(
                    observation = uiState.observation,
                    photoFilename = uiState.observation?.photoFilename
                )
            }
            
            if (uiState.activeTimers.isNotEmpty()) {
                item {
                    Text(
                        text = "Other Active Timers",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                items(uiState.activeTimers) { timer ->
                    if (timer.observationId != observationId) {
                        CompactTimerCard(
                            timer = timer,
                            onClick = { /* Navigate to other timer */ }
                        )
                    }
                }
            }
            
            if (uiState.errorMessage != null) {
                item {
                    ErrorCard(
                        message = uiState.errorMessage,
                        onDismiss = { viewModel.clearError() }
                    )
                }
            }
        }
    }
    
    if (showCompleteDialog) {
        CompleteObservationDialog(
            plateNumber = uiState.observation?.vrm ?: "",
            onComplete = { outcome ->
                viewModel.completeObservation(outcome)
                showCompleteDialog = false
                if (outcome == ObservationOutcome.PROCEED_TO_TICKET) {
                    onNavigateToIssuance(observationId)
                } else {
                    onNavigateBack()
                }
            },
            onDismiss = { showCompleteDialog = false }
        )
    }
}

@Composable
private fun TimerDisplay(
    timer: ObservationTimer?,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit
) {
    if (timer == null) {
        LoadingTimerCard()
        return
    }
    
    val progress by animateFloatAsState(
        targetValue = timer.progressPercentage,
        animationSpec = tween(1000),
        label = "timer_progress"
    )
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                timer.isCompleted -> MaterialTheme.colorScheme.errorContainer
                timer.remainingSeconds <= 60 -> MaterialTheme.colorScheme.errorContainer
                timer.remainingSeconds <= 300 -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.primaryContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circular timer display
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircularTimer(
                        progress = progress,
                        isCompleted = timer.isCompleted,
                        isWarning = timer.remainingSeconds <= 300
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = timer.formatRemainingTime(),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            timer.isCompleted -> MaterialTheme.colorScheme.error
                            timer.remainingSeconds <= 60 -> MaterialTheme.colorScheme.error
                            timer.remainingSeconds <= 300 -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                    
                    Text(
                        text = if (timer.isCompleted) "EXPIRED" 
                               else if (timer.isPaused) "PAUSED"
                               else "REMAINING",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Timer info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TimerInfo(
                    label = "Elapsed",
                    value = timer.formatElapsedTime(),
                    icon = Icons.Default.Timer
                )
                
                TimerInfo(
                    label = "Total",
                    value = "${timer.durationMinutes}m",
                    icon = Icons.Default.CheckCircle
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Timer controls
            if (!timer.isCompleted) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FloatingActionButton(
                        onClick = if (timer.isPaused) onResume else onPause,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = if (timer.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (timer.isPaused) "Resume" else "Pause"
                        )
                    }
                    
                    FloatingActionButton(
                        onClick = onStop,
                        modifier = Modifier.size(56.dp),
                        containerColor = MaterialTheme.colorScheme.error
                    ) {
                        Icon(
                            Icons.Default.Stop,
                            contentDescription = "Stop",
                            tint = MaterialTheme.colorScheme.onError
                        )
                    }
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onError
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Observation period complete - Vehicle may now be ticketed",
                            color = MaterialTheme.colorScheme.onError,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingTimerCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Loading timer...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ObservationDetails(
    observation: com.ceo.ticketissuance.domain.model.Observation?,
    photoFilename: String?
) {
    if (observation == null) return
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Observation Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    DetailItem(
                        icon = Icons.Default.DirectionsCar,
                        label = "Vehicle",
                        value = buildString {
                            append(observation.vrm)
                            if (observation.vehicleMake != null) {
                                append("\n${observation.vehicleMake}")
                                if (observation.vehicleModel != null) {
                                    append(" ${observation.vehicleModel}")
                                }
                            }
                            if (observation.vehicleColour != null) {
                                append("\n${observation.vehicleColour}")
                            }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    DetailItem(
                        icon = Icons.Default.LocationOn,
                        label = "Location",
                        value = "${observation.streetName}\n${observation.location}"
                    )
                }
                
                if (photoFilename != null) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data("file:///android_asset/photos/$photoFilename")
                                .build(),
                            contentDescription = "Evidence photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
            
            if (observation.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Notes: ${observation.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CompactTimerCard(
    timer: ObservationTimer,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Timer,
                contentDescription = null,
                tint = if (timer.isCompleted) MaterialTheme.colorScheme.error 
                       else MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = timer.plateNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (timer.isCompleted) "Completed" 
                           else timer.formatRemainingTime(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TimerInfo(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun DetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ErrorCard(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Error",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Dismiss")
            }
        }
    }
}

@Composable
private fun CompleteObservationDialog(
    plateNumber: String,
    onComplete: (ObservationOutcome) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Complete Observation",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text("How would you like to complete the observation for $plateNumber?")
        },
        confirmButton = {
            Column {
                Button(
                    onClick = { onComplete(ObservationOutcome.PROCEED_TO_TICKET) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Issue Ticket")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedButton(
                    onClick = { onComplete(ObservationOutcome.VEHICLE_MOVED) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Vehicle Moved")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedButton(
                    onClick = { onComplete(ObservationOutcome.CANCELLED_BY_OFFICER) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Cancel Observation")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Back")
            }
        }
    )
}

private fun DrawScope.drawCircularTimer(
    progress: Float,
    isCompleted: Boolean,
    isWarning: Boolean
) {
    val strokeWidth = 12.dp.toPx()
    val radius = (size.minDimension - strokeWidth) / 2
    val center = Offset(size.width / 2, size.height / 2)
    
    // Background circle
    drawCircle(
        color = Color.Gray.copy(alpha = 0.2f),
        radius = radius,
        center = center,
        style = Stroke(width = strokeWidth)
    )
    
    // Progress arc
    if (progress > 0f) {
        val sweepAngle = 360f * progress
        val startAngle = -90f // Start from top
        
        val progressColor = when {
            isCompleted -> Color.Red
            isWarning -> Color(0xFFFF9800) // Orange
            else -> Color(0xFF4CAF50) // Green
        }
        
        drawArc(
            color = progressColor,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round
            ),
            topLeft = Offset(
                center.x - radius,
                center.y - radius
            ),
            size = Size(radius * 2, radius * 2)
        )
    }
}

enum class ObservationOutcome {
    VEHICLE_MOVED,
    PROCEED_TO_TICKET,
    CANCELLED_BY_OFFICER,
    TIMER_EXPIRED
}