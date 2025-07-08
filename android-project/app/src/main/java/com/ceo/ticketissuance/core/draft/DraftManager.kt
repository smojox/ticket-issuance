package com.ceo.ticketissuance.core.draft

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DraftManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "observation_drafts", 
        Context.MODE_PRIVATE
    )
    
    private val _drafts = MutableStateFlow<List<ObservationDraft>>(emptyList())
    val drafts: StateFlow<List<ObservationDraft>> = _drafts.asStateFlow()
    
    init {
        loadDrafts()
    }
    
    fun saveDraft(draft: ObservationDraft) {
        val currentDrafts = _drafts.value.toMutableList()
        
        // Remove existing draft with same ID if it exists
        currentDrafts.removeAll { it.id == draft.id }
        
        // Add the new/updated draft
        currentDrafts.add(draft.copy(lastModified = System.currentTimeMillis()))
        
        // Keep only the most recent 10 drafts
        val sortedDrafts = currentDrafts.sortedByDescending { it.lastModified }.take(10)
        
        _drafts.value = sortedDrafts
        persistDrafts(sortedDrafts)
    }
    
    fun deleteDraft(draftId: String) {
        val currentDrafts = _drafts.value.toMutableList()
        currentDrafts.removeAll { it.id == draftId }
        
        _drafts.value = currentDrafts
        persistDrafts(currentDrafts)
    }
    
    fun getDraft(draftId: String): ObservationDraft? {
        return _drafts.value.find { it.id == draftId }
    }
    
    fun createDraftFromCurrentForm(
        plateNumber: String,
        streetId: Long?,
        streetName: String?,
        contraventionId: Long?,
        contraventionCode: String?,
        vehicleMakeId: Long?,
        vehicleMakeName: String?,
        vehicleModelId: Long?,
        vehicleModelName: String?,
        vehicleColour: String,
        location: String,
        notes: String,
        photoFilename: String?
    ): ObservationDraft {
        return ObservationDraft(
            id = generateDraftId(plateNumber),
            plateNumber = plateNumber,
            streetId = streetId,
            streetName = streetName,
            contraventionId = contraventionId,
            contraventionCode = contraventionCode,
            vehicleMakeId = vehicleMakeId,
            vehicleMakeName = vehicleMakeName,
            vehicleModelId = vehicleModelId,
            vehicleModelName = vehicleModelName,
            vehicleColour = vehicleColour,
            location = location,
            notes = notes,
            photoFilename = photoFilename,
            created = System.currentTimeMillis(),
            lastModified = System.currentTimeMillis()
        )
    }
    
    fun autoSaveIfSignificant(draft: ObservationDraft): Boolean {
        // Only auto-save if the draft has significant content
        val hasSignificantContent = draft.plateNumber.length >= 3 ||
                draft.streetId != null ||
                draft.contraventionId != null ||
                draft.location.length >= 5 ||
                draft.notes.length >= 10 ||
                draft.photoFilename != null
        
        if (hasSignificantContent) {
            saveDraft(draft)
            return true
        }
        
        return false
    }
    
    fun cleanupOldDrafts(maxAgeHours: Int = 24) {
        val cutoffTime = System.currentTimeMillis() - (maxAgeHours * 60 * 60 * 1000)
        val currentDrafts = _drafts.value.filter { it.lastModified > cutoffTime }
        
        _drafts.value = currentDrafts
        persistDrafts(currentDrafts)
    }
    
    private fun generateDraftId(plateNumber: String): String {
        val timestamp = System.currentTimeMillis()
        val cleanPlate = plateNumber.replace("[^A-Z0-9]".toRegex(), "")
        return "draft_${cleanPlate}_$timestamp"
    }
    
    private fun loadDrafts() {
        try {
            val draftsJson = prefs.getString("drafts", null)
            if (draftsJson != null) {
                val type = object : TypeToken<List<ObservationDraft>>() {}.type
                val loadedDrafts: List<ObservationDraft> = gson.fromJson(draftsJson, type)
                _drafts.value = loadedDrafts
            }
        } catch (e: Exception) {
            // Handle JSON parsing errors gracefully
            _drafts.value = emptyList()
        }
    }
    
    private fun persistDrafts(drafts: List<ObservationDraft>) {
        try {
            val draftsJson = gson.toJson(drafts)
            prefs.edit().putString("drafts", draftsJson).apply()
        } catch (e: Exception) {
            // Handle serialization errors gracefully
        }
    }
}

data class ObservationDraft(
    val id: String,
    val plateNumber: String,
    val streetId: Long?,
    val streetName: String?,
    val contraventionId: Long?,
    val contraventionCode: String?,
    val vehicleMakeId: Long?,
    val vehicleMakeName: String?,
    val vehicleModelId: Long?,
    val vehicleModelName: String?,
    val vehicleColour: String,
    val location: String,
    val notes: String,
    val photoFilename: String?,
    val created: Long,
    val lastModified: Long
) {
    val completionPercentage: Float
        get() {
            var completed = 0
            var total = 7 // Total required fields
            
            if (plateNumber.isNotBlank()) completed++
            if (streetId != null) completed++
            if (contraventionId != null) completed++
            if (location.isNotBlank()) completed++
            if (vehicleColour.isNotBlank()) completed++
            if (notes.isNotBlank()) completed++
            if (photoFilename != null) completed++
            
            return completed.toFloat() / total.toFloat()
        }
    
    val displayName: String
        get() = if (plateNumber.isNotBlank()) {
            "$plateNumber (${(completionPercentage * 100).toInt()}%)"
        } else {
            "Draft ${(completionPercentage * 100).toInt()}%"
        }
    
    val isComplete: Boolean
        get() = plateNumber.isNotBlank() && 
                streetId != null && 
                contraventionId != null && 
                location.isNotBlank()
}