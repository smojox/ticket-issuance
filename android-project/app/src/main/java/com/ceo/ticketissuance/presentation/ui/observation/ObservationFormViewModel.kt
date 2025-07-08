package com.ceo.ticketissuance.presentation.ui.observation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ceo.ticketissuance.core.camera.PhotoManager
import com.ceo.ticketissuance.core.camera.PhotoResult
import com.ceo.ticketissuance.core.draft.DraftManager
import com.ceo.ticketissuance.core.draft.ObservationDraft
import com.ceo.ticketissuance.core.util.Result
import com.ceo.ticketissuance.domain.model.Contravention
import com.ceo.ticketissuance.domain.model.Street
import com.ceo.ticketissuance.domain.model.VehicleMake
import com.ceo.ticketissuance.domain.model.VehicleModel
import com.ceo.ticketissuance.domain.repository.ContraventionRepository
import com.ceo.ticketissuance.domain.repository.StreetRepository
import com.ceo.ticketissuance.domain.repository.VehicleRepository
import com.ceo.ticketissuance.domain.usecase.CreateObservationUseCase
import com.ceo.ticketissuance.domain.usecase.SaveObservationWithTimerUseCase
import com.ceo.ticketissuance.domain.usecase.ValidateUkPlateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ObservationFormViewModel @Inject constructor(
    private val streetRepository: StreetRepository,
    private val vehicleRepository: VehicleRepository,
    private val contraventionRepository: ContraventionRepository,
    private val createObservationUseCase: CreateObservationUseCase,
    private val saveObservationWithTimerUseCase: SaveObservationWithTimerUseCase,
    private val validateUkPlateUseCase: ValidateUkPlateUseCase,
    private val photoManager: PhotoManager,
    private val draftManager: DraftManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ObservationFormUiState())
    val uiState: StateFlow<ObservationFormUiState> = _uiState.asStateFlow()
    
    init {
        loadInitialData()
    }
    
    private fun loadInitialData() {
        viewModelScope.launch {
            // Load streets
            _uiState.value = _uiState.value.copy(isLoadingStreets = true)
            streetRepository.getAllStreets().first().let { streets ->
                _uiState.value = _uiState.value.copy(
                    availableStreets = streets,
                    isLoadingStreets = false
                )
            }
            
            // Load vehicle makes
            _uiState.value = _uiState.value.copy(isLoadingVehicleMakes = true)
            vehicleRepository.getAllVehicleMakes().first().let { makes ->
                _uiState.value = _uiState.value.copy(
                    availableVehicleMakes = makes,
                    isLoadingVehicleMakes = false
                )
            }
            
            // Load contraventions
            _uiState.value = _uiState.value.copy(isLoadingContraventions = true)
            contraventionRepository.getAllContraventions().first().let { contraventions ->
                _uiState.value = _uiState.value.copy(
                    availableContraventions = contraventions,
                    isLoadingContraventions = false
                )
            }
        }
    }
    
    fun setPlateNumber(plateNumber: String) {
        _uiState.value = _uiState.value.copy(plateNumber = plateNumber.uppercase())
    }
    
    fun setVehicleMake(make: VehicleMake) {
        _uiState.value = _uiState.value.copy(
            selectedVehicleMake = make,
            selectedVehicleModel = null, // Reset model when make changes
            availableVehicleModels = emptyList()
        )
        
        // Load models for selected make
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingVehicleModels = true)
            vehicleRepository.getVehicleModelsByMake(make.id).first().let { models ->
                _uiState.value = _uiState.value.copy(
                    availableVehicleModels = models,
                    isLoadingVehicleModels = false
                )
            }
        }
    }
    
    fun setVehicleModel(model: VehicleModel) {
        _uiState.value = _uiState.value.copy(selectedVehicleModel = model)
    }
    
    fun setVehicleColour(colour: String) {
        _uiState.value = _uiState.value.copy(vehicleColour = colour)
    }
    
    fun setStreet(street: Street) {
        _uiState.value = _uiState.value.copy(
            selectedStreet = street,
            location = street.name // Pre-populate location with street name
        )
    }
    
    fun setLocation(location: String) {
        _uiState.value = _uiState.value.copy(location = location)
    }
    
    fun setContravention(contravention: Contravention) {
        _uiState.value = _uiState.value.copy(selectedContravention = contravention)
    }
    
    fun setNotes(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }
    
    fun setPhotoFilename(filename: String) {
        _uiState.value = _uiState.value.copy(photoFilename = filename)
    }
    
    fun removePhoto() {
        val currentFilename = _uiState.value.photoFilename
        if (currentFilename != null) {
            // Delete the photo file
            photoManager.deletePhoto(currentFilename)
        }
        _uiState.value = _uiState.value.copy(photoFilename = null)
    }
    
    fun getPhotosForCurrentPlate(): List<PhotoResult> {
        val plateNumber = _uiState.value.plateNumber
        return if (plateNumber.isNotBlank()) {
            photoManager.getPhotosForPlate(plateNumber)
        } else {
            emptyList()
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    fun saveObservation(onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            val currentState = _uiState.value
            
            // Validate form
            val validationResult = validateForm(currentState)
            if (validationResult != null) {
                _uiState.value = currentState.copy(errorMessage = validationResult)
                return@launch
            }
            
            _uiState.value = currentState.copy(isSaving = true)
            
            val result = saveObservationWithTimerUseCase(
                plateNumber = currentState.plateNumber,
                streetId = currentState.selectedStreet!!.id,
                contraventionId = currentState.selectedContravention!!.id,
                observationPeriodMinutes = currentState.selectedContravention!!.observationPeriodMinutes,
                photoFilename = currentState.photoFilename,
                notes = currentState.notes,
                location = currentState.location,
                vehicleMake = currentState.selectedVehicleMake?.name,
                vehicleModel = currentState.selectedVehicleModel?.name,
                vehicleColour = currentState.vehicleColour.takeIf { it.isNotBlank() }
            )
            
            when (result) {
                is Result.Success -> {
                    _uiState.value = currentState.copy(
                        isSaving = false,
                        isSaved = true
                    )
                    onSuccess(result.data.observationId)
                }
                is Result.Error -> {
                    _uiState.value = currentState.copy(
                        isSaving = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }
    
    private fun validateForm(state: ObservationFormUiState): String? {
        // Validate plate number
        if (state.plateNumber.isBlank()) {
            return "Please enter a number plate"
        }
        
        when (val plateValidation = validateUkPlateUseCase(state.plateNumber)) {
            is Result.Success -> {
                if (plateValidation.data == null) {
                    return "Please enter a valid UK number plate"
                }
            }
            is Result.Error -> return plateValidation.message
        }
        
        // Validate street selection
        if (state.selectedStreet == null) {
            return "Please select a street"
        }
        
        // Validate contravention selection
        if (state.selectedContravention == null) {
            return "Please select a contravention type"
        }
        
        // Validate location
        if (state.location.isBlank()) {
            return "Please enter a specific location"
        }
        
        return null
    }
    
    fun saveDraft() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val draft = draftManager.createDraftFromCurrentForm(
                plateNumber = currentState.plateNumber,
                streetId = currentState.selectedStreet?.id,
                streetName = currentState.selectedStreet?.name,
                contraventionId = currentState.selectedContravention?.id,
                contraventionCode = currentState.selectedContravention?.code,
                vehicleMakeId = currentState.selectedVehicleMake?.id,
                vehicleMakeName = currentState.selectedVehicleMake?.name,
                vehicleModelId = currentState.selectedVehicleModel?.id,
                vehicleModelName = currentState.selectedVehicleModel?.name,
                vehicleColour = currentState.vehicleColour,
                location = currentState.location,
                notes = currentState.notes,
                photoFilename = currentState.photoFilename
            )
            
            draftManager.saveDraft(draft)
        }
    }
    
    fun autoSave() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val draft = draftManager.createDraftFromCurrentForm(
                plateNumber = currentState.plateNumber,
                streetId = currentState.selectedStreet?.id,
                streetName = currentState.selectedStreet?.name,
                contraventionId = currentState.selectedContravention?.id,
                contraventionCode = currentState.selectedContravention?.code,
                vehicleMakeId = currentState.selectedVehicleMake?.id,
                vehicleMakeName = currentState.selectedVehicleMake?.name,
                vehicleModelId = currentState.selectedVehicleModel?.id,
                vehicleModelName = currentState.selectedVehicleModel?.name,
                vehicleColour = currentState.vehicleColour,
                location = currentState.location,
                notes = currentState.notes,
                photoFilename = currentState.photoFilename
            )
            
            draftManager.autoSaveIfSignificant(draft)
        }
    }
    
    fun loadDraft(draftId: String) {
        viewModelScope.launch {
            val draft = draftManager.getDraft(draftId)
            if (draft != null) {
                // Load street if available
                val street = draft.streetId?.let { streetId ->
                    _uiState.value.availableStreets.find { it.id == streetId }
                }
                
                // Load contravention if available
                val contravention = draft.contraventionId?.let { contraventionId ->
                    _uiState.value.availableContraventions.find { it.id == contraventionId }
                }
                
                // Load vehicle make if available
                val vehicleMake = draft.vehicleMakeId?.let { makeId ->
                    _uiState.value.availableVehicleMakes.find { it.id == makeId }
                }
                
                // Load vehicle model if available
                val vehicleModel = draft.vehicleModelId?.let { modelId ->
                    _uiState.value.availableVehicleModels.find { it.id == modelId }
                }
                
                _uiState.value = _uiState.value.copy(
                    plateNumber = draft.plateNumber,
                    selectedStreet = street,
                    selectedContravention = contravention,
                    selectedVehicleMake = vehicleMake,
                    selectedVehicleModel = vehicleModel,
                    vehicleColour = draft.vehicleColour,
                    location = draft.location,
                    notes = draft.notes,
                    photoFilename = draft.photoFilename
                )
            }
        }
    }
    
    fun deleteDraft(draftId: String) {
        draftManager.deleteDraft(draftId)
    }
}

data class ObservationFormUiState(
    val plateNumber: String = "",
    val selectedVehicleMake: VehicleMake? = null,
    val selectedVehicleModel: VehicleModel? = null,
    val vehicleColour: String = "",
    val selectedStreet: Street? = null,
    val location: String = "",
    val selectedContravention: Contravention? = null,
    val notes: String = "",
    val photoFilename: String? = null,
    
    // Data loading states
    val availableVehicleMakes: List<VehicleMake> = emptyList(),
    val availableVehicleModels: List<VehicleModel> = emptyList(),
    val availableStreets: List<Street> = emptyList(),
    val availableContraventions: List<Contravention> = emptyList(),
    
    val isLoadingVehicleMakes: Boolean = false,
    val isLoadingVehicleModels: Boolean = false,
    val isLoadingStreets: Boolean = false,
    val isLoadingContraventions: Boolean = false,
    
    // Form states
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)