package com.ceo.ticketissuance.presentation.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ceo.ticketissuance.R
import com.ceo.ticketissuance.domain.model.SyncStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToObservation: () -> Unit,
    onNavigateToIssuance: () -> Unit,
    onNavigateToCountdown: () -> Unit,
    onNavigateToQueue: () -> Unit,
    onNavigateToSync: () -> Unit,
    onNavigateToTicketHistory: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                DashboardEvent.NavigateToObservation -> onNavigateToObservation()
                DashboardEvent.NavigateToIssuance -> onNavigateToIssuance()
                DashboardEvent.NavigateToCountdown -> onNavigateToCountdown()
                DashboardEvent.NavigateToQueue -> onNavigateToQueue()
                DashboardEvent.NavigateToSync -> onNavigateToSync()
                DashboardEvent.NavigateToTicketHistory -> onNavigateToTicketHistory()
                DashboardEvent.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = viewModel::onSyncClick) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = stringResource(R.string.sync),
                            tint = when (uiState.syncStatus) {
                                SyncStatus.COMPLETED -> Color.Green
                                SyncStatus.FAILED -> Color.Red
                                SyncStatus.PENDING -> Color.Orange
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Info Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Welcome, ${uiState.currentUser?.fullName ?: "Officer"}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Today: ${uiState.currentTime}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Primary Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Observation Button
                PrimaryActionButton(
                    text = stringResource(R.string.observation),
                    subtitle = "Start New Watch",
                    icon = Icons.Default.CameraAlt,
                    containerColor = MaterialTheme.colorScheme.primary,
                    onClick = viewModel::onObservationClick,
                    modifier = Modifier.weight(1f)
                )

                // Issuance Button
                PrimaryActionButton(
                    text = stringResource(R.string.issuance),
                    subtitle = "Create New Ticket",
                    icon = Icons.Default.CreateNewFolder,
                    containerColor = MaterialTheme.colorScheme.secondary,
                    onClick = viewModel::onIssuanceClick,
                    modifier = Modifier.weight(1f)
                )
            }

            // Secondary Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Countdown Button
                SecondaryActionButton(
                    text = stringResource(R.string.countdown),
                    icon = Icons.Default.Schedule,
                    badgeCount = uiState.countdownCount,
                    onClick = viewModel::onCountdownClick,
                    modifier = Modifier.weight(1f)
                )

                // Ticket History Button
                SecondaryActionButton(
                    text = "History",
                    icon = Icons.Default.History,
                    badgeCount = 0,
                    onClick = viewModel::onTicketHistoryClick,
                    modifier = Modifier.weight(1f)
                )

                // Sync Button
                SecondaryActionButton(
                    text = stringResource(R.string.sync),
                    icon = Icons.Default.SyncAlt,
                    badgeCount = 0,
                    onClick = viewModel::onSyncClick,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Logout Button
            OutlinedButton(
                onClick = viewModel::onLogoutClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.logout))
            }
        }
    }
}

@Composable
private fun PrimaryActionButton(
    text: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
            containerColor = containerColor
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SecondaryActionButton(
    text: String,
    icon: ImageVector,
    badgeCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(80.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            BadgedBox(
                badge = {
                    if (badgeCount > 0) {
                        Badge {
                            Text(text = badgeCount.toString())
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}