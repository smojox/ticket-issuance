package com.ceo.ticketissuance.presentation.ui.observation

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ceo.ticketissuance.domain.model.Contravention
import com.ceo.ticketissuance.domain.model.Street
import com.ceo.ticketissuance.domain.model.VehicleMake
import com.ceo.ticketissuance.domain.model.VehicleModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ObservationFormScreen(
    plateNumber: String?,
    onNavigateBack: () -> Unit,
    onNavigateToCamera: () -> Unit,
    onNavigateToCountdown: (Long) -> Unit,
    viewModel: ObservationFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Initialize with plate number if provided
    remember(plateNumber) {
        plateNumber?.let { viewModel.setPlateNumber(it) }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Observation") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    viewModel.saveObservation { observationId ->
                        onNavigateToCountdown(observationId)
                    }
                },
                icon = { Icon(Icons.Default.Save, contentDescription = null) },
                text = { Text("Save & Start Timer") }
            )
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
                VehicleDetailsSection(
                    plateNumber = uiState.plateNumber,
                    onPlateNumberChange = viewModel::setPlateNumber,
                    selectedMake = uiState.selectedVehicleMake,
                    selectedModel = uiState.selectedVehicleModel,
                    vehicleColour = uiState.vehicleColour,
                    onVehicleColourChange = viewModel::setVehicleColour,
                    availableMakes = uiState.availableVehicleMakes,
                    availableModels = uiState.availableVehicleModels,
                    onMakeSelected = viewModel::setVehicleMake,
                    onModelSelected = viewModel::setVehicleModel,
                    isLoadingMakes = uiState.isLoadingVehicleMakes,
                    isLoadingModels = uiState.isLoadingVehicleModels
                )
            }
            
            item {
                LocationSection(
                    selectedStreet = uiState.selectedStreet,
                    location = uiState.location,
                    onLocationChange = viewModel::setLocation,
                    availableStreets = uiState.availableStreets,
                    onStreetSelected = viewModel::setStreet,
                    isLoadingStreets = uiState.isLoadingStreets
                )
            }
            
            item {
                ContraventionSection(
                    selectedContravention = uiState.selectedContravention,
                    availableContraventions = uiState.availableContraventions,
                    onContraventionSelected = viewModel::setContravention,
                    isLoadingContraventions = uiState.isLoadingContraventions
                )
            }
            
            item {
                PhotoSection(
                    photoFilename = uiState.photoFilename,
                    onNavigateToCamera = onNavigateToCamera,
                    onRemovePhoto = viewModel::removePhoto
                )
            }
            
            item {
                NotesSection(
                    notes = uiState.notes,
                    onNotesChange = viewModel::setNotes
                )
            }
            
            if (uiState.errorMessage != null) {
                item {
                    ErrorSection(
                        errorMessage = uiState.errorMessage,
                        onDismissError = viewModel::clearError
                    )
                }
            }
        }
    }
}

@Composable
private fun VehicleDetailsSection(
    plateNumber: String,
    onPlateNumberChange: (String) -> Unit,
    selectedMake: VehicleMake?,
    selectedModel: VehicleModel?,
    vehicleColour: String,
    onVehicleColourChange: (String) -> Unit,
    availableMakes: List<VehicleMake>,
    availableModels: List<VehicleModel>,
    onMakeSelected: (VehicleMake) -> Unit,
    onModelSelected: (VehicleModel) -> Unit,
    isLoadingMakes: Boolean,
    isLoadingModels: Boolean
) {
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
                text = "Vehicle Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = plateNumber,
                onValueChange = onPlateNumberChange,
                label = { Text("Number Plate") },
                placeholder = { Text("e.g. AB12 CDE") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    keyboardType = KeyboardType.Text
                ),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DropdownSelector(
                    modifier = Modifier.weight(1f),
                    label = "Make",
                    selectedItem = selectedMake?.name ?: "",
                    items = availableMakes,
                    onItemSelected = onMakeSelected,
                    displayText = { it.name },
                    isLoading = isLoadingMakes,
                    placeholder = "Select make"
                )
                
                DropdownSelector(
                    modifier = Modifier.weight(1f),
                    label = "Model",
                    selectedItem = selectedModel?.name ?: "",
                    items = availableModels,
                    onItemSelected = onModelSelected,
                    displayText = { it.name },
                    isLoading = isLoadingModels,
                    placeholder = "Select model",
                    enabled = selectedMake != null
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = vehicleColour,
                onValueChange = onVehicleColourChange,
                label = { Text("Vehicle Colour") },
                placeholder = { Text("e.g. Red, Blue, White") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words
                ),
                singleLine = true
            )
        }
    }
}

@Composable
private fun LocationSection(
    selectedStreet: Street?,
    location: String,
    onLocationChange: (String) -> Unit,
    availableStreets: List<Street>,
    onStreetSelected: (Street) -> Unit,
    isLoadingStreets: Boolean
) {
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
                text = "Location",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            DropdownSelector(
                modifier = Modifier.fillMaxWidth(),
                label = "Street",
                selectedItem = selectedStreet?.name ?: "",
                items = availableStreets,
                onItemSelected = onStreetSelected,
                displayText = { "${it.name} (${it.areaCode})" },
                isLoading = isLoadingStreets,
                placeholder = "Select street"
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = location,
                onValueChange = onLocationChange,
                label = { Text("Specific Location") },
                placeholder = { Text("e.g. Outside No. 24, Near junction") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                maxLines = 2
            )
        }
    }
}

@Composable
private fun ContraventionSection(
    selectedContravention: Contravention?,
    availableContraventions: List<Contravention>,
    onContraventionSelected: (Contravention) -> Unit,
    isLoadingContraventions: Boolean
) {
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
                text = "Contravention",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            DropdownSelector(
                modifier = Modifier.fillMaxWidth(),
                label = "Contravention Type",
                selectedItem = selectedContravention?.let { "${it.code} - ${it.description}" } ?: "",
                items = availableContraventions,
                onItemSelected = onContraventionSelected,
                displayText = { "${it.code} - ${it.description}" },
                isLoading = isLoadingContraventions,
                placeholder = "Select contravention"
            )
            
            if (selectedContravention != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Observation Period: ${selectedContravention.observationPeriodMinutes} minutes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PhotoSection(
    photoFilename: String?,
    onNavigateToCamera: () -> Unit,
    onRemovePhoto: () -> Unit
) {
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
                text = "Photo Evidence",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (photoFilename != null) {
                PhotoPreview(
                    photoFilename = photoFilename,
                    onRemovePhoto = onRemovePhoto
                )
            } else {
                OutlinedButton(
                    onClick = onNavigateToCamera,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Camera, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Take Photo")
                }
            }
        }
    }
}

@Composable
private fun PhotoPreview(
    photoFilename: String,
    onRemovePhoto: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Photo captured",
                    style = MaterialTheme.typography.bodyMedium
                )
                IconButton(onClick = onRemovePhoto) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove photo")
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data("file:///android_asset/photos/$photoFilename")
                        .build(),
                    contentDescription = "Captured photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
private fun NotesSection(
    notes: String,
    onNotesChange: (String) -> Unit
) {
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
                text = "Notes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = { Text("Additional Notes") },
                placeholder = { Text("Any additional observations or details...") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                maxLines = 4,
                minLines = 3
            )
        }
    }
}

@Composable
private fun ErrorSection(
    errorMessage: String,
    onDismissError: () -> Unit
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
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            TextButton(
                onClick = onDismissError,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Dismiss")
            }
        }
    }
}

@Composable
private fun <T> DropdownSelector(
    modifier: Modifier = Modifier,
    label: String,
    selectedItem: String,
    items: List<T>,
    onItemSelected: (T) -> Unit,
    displayText: (T) -> String,
    isLoading: Boolean = false,
    placeholder: String = "Select...",
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        OutlinedTextField(
            value = selectedItem,
            onValueChange = { },
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled && !isLoading,
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { if (enabled && !isLoading) expanded = true }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (isLoading) {
                DropdownMenuItem(
                    text = { Text("Loading...") },
                    onClick = { }
                )
            } else {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(displayText(item)) },
                        onClick = {
                            onItemSelected(item)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}