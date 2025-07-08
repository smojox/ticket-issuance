package com.ceo.ticketissuance.presentation.ui.camera

import androidx.camera.core.ImageCapture
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ceo.ticketissuance.core.camera.PhotoManager
import com.ceo.ticketissuance.core.camera.PhotoResult
import com.ceo.ticketissuance.core.util.Result
import com.ceo.ticketissuance.domain.usecase.ValidateUkPlateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val validateUkPlateUseCase: ValidateUkPlateUseCase,
    private val photoManager: PhotoManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()
    
    fun capturePhoto(imageCapture: ImageCapture) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCapturing = true)
            
            when (val result = photoManager.capturePhoto(imageCapture, _uiState.value.detectedPlate)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isCapturing = false,
                        lastCapturedPhoto = result.data,
                        lastCaptureTime = System.currentTimeMillis()
                    )
                }
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isCapturing = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }
    
    fun toggleFlash() {
        _uiState.value = _uiState.value.copy(
            isFlashEnabled = !_uiState.value.isFlashEnabled
        )
    }
    
    fun processRecognizedText(text: String) {
        viewModelScope.launch {
            when (val result = validateUkPlateUseCase(text)) {
                is Result.Success -> {
                    if (result.data != null) {
                        _uiState.value = _uiState.value.copy(
                            detectedPlate = result.data,
                            lastDetectionTime = System.currentTimeMillis()
                        )
                    }
                }
                is Result.Error -> {
                    // Text not recognized as valid UK plate - continue scanning
                    _uiState.value = _uiState.value.copy(
                        detectedPlate = null
                    )
                }
            }
        }
    }
    
    fun clearDetectedPlate() {
        _uiState.value = _uiState.value.copy(detectedPlate = null)
    }
}

data class CameraUiState(
    val isCapturing: Boolean = false,
    val isFlashEnabled: Boolean = false,
    val detectedPlate: String? = null,
    val lastCapturedPhoto: PhotoResult? = null,
    val lastCaptureTime: Long = 0L,
    val lastDetectionTime: Long = 0L,
    val errorMessage: String? = null
)