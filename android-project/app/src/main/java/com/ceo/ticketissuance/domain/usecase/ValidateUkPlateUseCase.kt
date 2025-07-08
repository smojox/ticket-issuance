package com.ceo.ticketissuance.domain.usecase

import com.ceo.ticketissuance.core.util.Result
import javax.inject.Inject

class ValidateUkPlateUseCase @Inject constructor() {
    
    private val ukPlatePatterns = listOf(
        // Current format: AB12 CDE
        Regex("^[A-Z]{2}[0-9]{2}\\s?[A-Z]{3}$"),
        // Older format: A123 BCD
        Regex("^[A-Z][0-9]{3}\\s?[A-Z]{3}$"),
        // Older format: AB12 CDE
        Regex("^[A-Z][0-9]{1,3}\\s?[A-Z]{3}$"),
        // Dateless format: ABC 123D
        Regex("^[A-Z]{3}\\s?[0-9]{1,3}[A-Z]$"),
        // Northern Ireland format: ABC 1234
        Regex("^[A-Z]{1,3}\\s?[0-9]{1,4}$"),
        // Personal plates: Various formats
        Regex("^[A-Z]{1,3}\\s?[0-9]{1,4}\\s?[A-Z]{0,3}$")
    )
    
    operator fun invoke(text: String): Result<String?> {
        return try {
            val cleanedText = text.trim().uppercase()
            
            // Check each pattern
            for (pattern in ukPlatePatterns) {
                if (pattern.matches(cleanedText)) {
                    // Format the plate consistently (with space)
                    val formattedPlate = formatPlate(cleanedText)
                    return Result.Success(formattedPlate)
                }
            }
            
            // No valid UK plate pattern found
            Result.Success(null)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error validating plate")
        }
    }
    
    private fun formatPlate(plate: String): String {
        val cleanPlate = plate.replace("\\s+".toRegex(), "")
        
        // Format current style plates: AB12CDE -> AB12 CDE
        if (cleanPlate.length == 7 && cleanPlate.matches(Regex("^[A-Z]{2}[0-9]{2}[A-Z]{3}$"))) {
            return "${cleanPlate.substring(0, 4)} ${cleanPlate.substring(4)}"
        }
        
        // Format older style plates: A123BCD -> A123 BCD
        if (cleanPlate.length == 7 && cleanPlate.matches(Regex("^[A-Z][0-9]{3}[A-Z]{3}$"))) {
            return "${cleanPlate.substring(0, 4)} ${cleanPlate.substring(4)}"
        }
        
        // Format shorter plates: A12BCD -> A12 BCD
        if (cleanPlate.length == 6 && cleanPlate.matches(Regex("^[A-Z][0-9]{2}[A-Z]{3}$"))) {
            return "${cleanPlate.substring(0, 3)} ${cleanPlate.substring(3)}"
        }
        
        // Format dateless plates: ABC123D -> ABC 123D
        if (cleanPlate.matches(Regex("^[A-Z]{3}[0-9]{1,3}[A-Z]$"))) {
            val lettersPart = cleanPlate.substring(0, 3)
            val numbersPart = cleanPlate.substring(3, cleanPlate.length - 1)
            val lastLetter = cleanPlate.last()
            return "$lettersPart $numbersPart$lastLetter"
        }
        
        // For other formats, try to add space in logical places
        return when {
            cleanPlate.length <= 4 -> cleanPlate
            cleanPlate.length <= 7 -> {
                // Try to split at logical boundary
                val firstPart = cleanPlate.substring(0, cleanPlate.length / 2 + 1)
                val secondPart = cleanPlate.substring(cleanPlate.length / 2 + 1)
                "$firstPart $secondPart"
            }
            else -> cleanPlate
        }
    }
}