package com.ceo.ticketissuance.domain.usecase

import com.ceo.ticketissuance.core.util.Result
import com.ceo.ticketissuance.domain.model.Contravention
import com.ceo.ticketissuance.domain.model.Street
import javax.inject.Inject

class ValidateObservationFormUseCase @Inject constructor(
    private val validateUkPlateUseCase: ValidateUkPlateUseCase
) {
    
    operator fun invoke(formData: ObservationFormValidationData): Result<Unit> {
        val errors = mutableListOf<String>()
        
        // Validate plate number
        if (formData.plateNumber.isBlank()) {
            errors.add("Please enter a number plate")
        } else {
            when (val plateValidation = validateUkPlateUseCase(formData.plateNumber)) {
                is Result.Success -> {
                    if (plateValidation.data == null) {
                        errors.add("Please enter a valid UK number plate")
                    }
                }
                is Result.Error -> errors.add(plateValidation.message)
            }
        }
        
        // Validate street selection
        if (formData.selectedStreet == null) {
            errors.add("Please select a street")
        }
        
        // Validate contravention selection
        if (formData.selectedContravention == null) {
            errors.add("Please select a contravention type")
        }
        
        // Validate location
        if (formData.location.isBlank()) {
            errors.add("Please enter a specific location")
        } else if (formData.location.length < 3) {
            errors.add("Location must be at least 3 characters")
        }
        
        // Validate vehicle colour if provided
        if (formData.vehicleColour.isNotBlank() && formData.vehicleColour.length < 2) {
            errors.add("Vehicle colour must be at least 2 characters")
        }
        
        // Validate notes if provided
        if (formData.notes.length > 500) {
            errors.add("Notes cannot exceed 500 characters")
        }
        
        return if (errors.isEmpty()) {
            Result.Success(Unit)
        } else {
            Result.Error(errors.joinToString(", "))
        }
    }
}

data class ObservationFormValidationData(
    val plateNumber: String,
    val selectedStreet: Street?,
    val selectedContravention: Contravention?,
    val location: String,
    val vehicleColour: String,
    val notes: String
)

class ValidateVehicleDetailsUseCase @Inject constructor() {
    
    operator fun invoke(
        plateNumber: String,
        vehicleMake: String?,
        vehicleModel: String?,
        vehicleColour: String
    ): Result<Unit> {
        val errors = mutableListOf<String>()
        
        // Validate plate number format
        if (plateNumber.isBlank()) {
            errors.add("Plate number is required")
        } else if (plateNumber.length < 2) {
            errors.add("Plate number too short")
        } else if (plateNumber.length > 8) {
            errors.add("Plate number too long")
        }
        
        // Validate vehicle make and model consistency
        if (vehicleMake != null && vehicleModel == null) {
            errors.add("Please select a vehicle model")
        }
        
        // Validate vehicle colour format
        if (vehicleColour.isNotBlank()) {
            if (vehicleColour.length < 2) {
                errors.add("Vehicle colour too short")
            } else if (vehicleColour.length > 20) {
                errors.add("Vehicle colour too long")
            } else if (!vehicleColour.matches(Regex("^[a-zA-Z\\s]+$"))) {
                errors.add("Vehicle colour should only contain letters and spaces")
            }
        }
        
        return if (errors.isEmpty()) {
            Result.Success(Unit)
        } else {
            Result.Error(errors.joinToString(", "))
        }
    }
}

class ValidateLocationDataUseCase @Inject constructor() {
    
    operator fun invoke(
        street: Street?,
        location: String
    ): Result<Unit> {
        val errors = mutableListOf<String>()
        
        // Validate street selection
        if (street == null) {
            errors.add("Street selection is required")
        }
        
        // Validate specific location
        if (location.isBlank()) {
            errors.add("Specific location is required")
        } else if (location.length < 3) {
            errors.add("Location must be at least 3 characters")
        } else if (location.length > 100) {
            errors.add("Location cannot exceed 100 characters")
        }
        
        return if (errors.isEmpty()) {
            Result.Success(Unit)
        } else {
            Result.Error(errors.joinToString(", "))
        }
    }
}

class ValidateContraventionDataUseCase @Inject constructor() {
    
    operator fun invoke(
        contravention: Contravention?,
        notes: String
    ): Result<Unit> {
        val errors = mutableListOf<String>()
        
        // Validate contravention selection
        if (contravention == null) {
            errors.add("Contravention type is required")
        }
        
        // Validate notes
        if (notes.length > 500) {
            errors.add("Notes cannot exceed 500 characters")
        }
        
        // Check for specific contravention requirements
        contravention?.let { cv ->
            when (cv.code) {
                "01", "02" -> {
                    // Loading/unloading contraventions might require specific notes
                    if (notes.isBlank()) {
                        errors.add("Notes are recommended for loading/unloading contraventions")
                    }
                }
                "07" -> {
                    // Dangerous position might require specific location details
                    if (notes.isBlank()) {
                        errors.add("Please provide details about the dangerous position")
                    }
                }
                "30" -> {
                    // School keep clear might require time validation
                    val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                    if (currentHour < 8 || currentHour > 17) {
                        errors.add("School keep clear contraventions typically apply during school hours")
                    }
                }
            }
        }
        
        return if (errors.isEmpty()) {
            Result.Success(Unit)
        } else {
            Result.Error(errors.joinToString(", "))
        }
    }
}