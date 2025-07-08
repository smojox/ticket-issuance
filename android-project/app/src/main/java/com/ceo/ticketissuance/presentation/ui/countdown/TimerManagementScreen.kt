package com.ceo.ticketissuance.presentation.ui.countdown

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ceo.ticketissuance.domain.model.ObservationTimer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerManagementScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTimer: (Long) -> Unit,
    onNavigateToNewObservation: () -> Unit,
    viewModel: TimerManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Active Timers") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToNewObservation
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Observation")
            }
        }
    ) { paddingValues ->
        if (uiState.activeTimers.isEmpty()) {
            EmptyTimersContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    TimerSummaryCard(
                        totalActive = uiState.activeTimers.size,
                        completedCount = uiState.activeTimers.count { it.isCompleted },
                        nearingCompletion = uiState.activeTimers.count { it.remainingSeconds <= 300 }
                    )
                }
                
                items(uiState.activeTimers) { timer ->
                    TimerCard(
                        timer = timer,
                        onTimerClick = { onNavigateToTimer(timer.observationId) },
                        onPauseResume = { 
                            if (timer.isPaused) {
                                viewModel.resumeTimer(timer.observationId)
                            } else {
                                viewModel.pauseTimer(timer.observationId)
                            }
                        },
                        onStop = { viewModel.stopTimer(timer.observationId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyTimersContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Timer,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No Active Timers",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Start a new observation to begin timing",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun TimerSummaryCard(
    totalActive: Int,
    completedCount: Int,
    nearingCompletion: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Timer Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    label = "Active",
                    value = totalActive.toString(),
                    color = MaterialTheme.colorScheme.primary
                )
                
                SummaryItem(
                    label = "Completed",
                    value = completedCount.toString(),
                    color = MaterialTheme.colorScheme.error
                )
                
                SummaryItem(
                    label = "Urgent",
                    value = nearingCompletion.toString(),
                    color = Color(0xFFFF9800) // Orange
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TimerCard(
    timer: ObservationTimer,
    onTimerClick: () -> Unit,
    onPauseResume: () -> Unit,
    onStop: () -> Unit
) {
    Card(
        onClick = onTimerClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                timer.isCompleted -> MaterialTheme.colorScheme.errorContainer
                timer.remainingSeconds <= 60 -> MaterialTheme.colorScheme.errorContainer
                timer.remainingSeconds <= 300 -> MaterialTheme.colorScheme.tertiaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with plate number and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = timer.plateNumber,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = when {
                            timer.isCompleted -> "COMPLETED"
                            timer.isPaused -> "PAUSED"
                            else -> "ACTIVE"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            timer.isCompleted -> MaterialTheme.colorScheme.error
                            timer.isPaused -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.primary
                        }
                    )
                }
                
                Text(
                    text = timer.formatRemainingTime(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        timer.isCompleted -> MaterialTheme.colorScheme.error
                        timer.remainingSeconds <= 60 -> MaterialTheme.colorScheme.error
                        timer.remainingSeconds <= 300 -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Progress bar
            LinearProgressIndicator(
                progress = { timer.progressPercentage },
                modifier = Modifier.fillMaxWidth(),
                color = when {
                    timer.isCompleted -> MaterialTheme.colorScheme.error
                    timer.remainingSeconds <= 60 -> MaterialTheme.colorScheme.error
                    timer.remainingSeconds <= 300 -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Timer info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Elapsed: ${timer.formatElapsedTime()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = "Total: ${timer.durationMinutes}m",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Control buttons
            if (!timer.isCompleted) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onPauseResume,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (timer.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (timer.isPaused) "Resume" else "Pause"
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    IconButton(
                        onClick = onStop,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Stop,
                            contentDescription = "Stop"
                        )
                    }
                }
            }
        }
    }
}