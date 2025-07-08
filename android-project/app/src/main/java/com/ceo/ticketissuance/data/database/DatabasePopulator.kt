package com.ceo.ticketissuance.data.database

import com.ceo.ticketissuance.data.database.entity.ContraventionEntity
import com.ceo.ticketissuance.data.database.entity.StreetEntity
import com.ceo.ticketissuance.data.database.entity.UserEntity
import com.ceo.ticketissuance.data.database.entity.VehicleMakeEntity
import com.ceo.ticketissuance.data.database.entity.VehicleModelEntity
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabasePopulator @Inject constructor(
    private val database: AppDatabase
) {
    
    suspend fun populateDatabase() {
        // Check if database is already populated
        val existingUser = database.userDao().getUserByUsername("Test")
        if (existingUser != null) {
            return // Already populated
        }
        
        populateUsers()
        populateStreets()
        populateContraventions()
        populateVehicleMakes()
        populateVehicleModels()
    }
    
    private suspend fun populateUsers() {
        val now = LocalDateTime.now()
        val testUser = UserEntity(
            username = "Test",
            password = "Test",
            fullName = "Test Officer",
            badgeNumber = "CEO001",
            createdAt = now,
            updatedAt = now
        )
        database.userDao().insertUser(testUser)
    }
    
    private suspend fun populateStreets() {
        val now = LocalDateTime.now()
        val streets = listOf(
            StreetEntity(name = "High Street", areaCode = "HS01", createdAt = now, updatedAt = now),
            StreetEntity(name = "Market Square", areaCode = "MS01", createdAt = now, updatedAt = now),
            StreetEntity(name = "Victoria Road", areaCode = "VR01", createdAt = now, updatedAt = now),
            StreetEntity(name = "Church Lane", areaCode = "CL01", createdAt = now, updatedAt = now),
            StreetEntity(name = "King Street", areaCode = "KS01", createdAt = now, updatedAt = now),
            StreetEntity(name = "Queen Avenue", areaCode = "QA01", createdAt = now, updatedAt = now),
            StreetEntity(name = "Mill Road", areaCode = "MR01", createdAt = now, updatedAt = now),
            StreetEntity(name = "Station Road", areaCode = "SR01", createdAt = now, updatedAt = now),
            StreetEntity(name = "Park Street", areaCode = "PS01", createdAt = now, updatedAt = now),
            StreetEntity(name = "Castle Street", areaCode = "CS01", createdAt = now, updatedAt = now)
        )
        database.streetDao().insertStreets(streets)
    }
    
    private suspend fun populateContraventions() {
        val now = LocalDateTime.now()
        val contraventions = mutableListOf<ContraventionEntity>()
        
        // Get all streets to create contraventions for each
        val streets = database.streetDao().getAllStreets()
        
        // Create 10 contraventions for each street
        for (streetId in 1L..10L) {
            contraventions.addAll(
                listOf(
                    ContraventionEntity(
                        code = "01",
                        description = "Parked in a restricted street during prescribed hours",
                        observationTimeMinutes = 5,
                        penaltyAmount = BigDecimal("70.00"),
                        streetId = streetId,
                        createdAt = now,
                        updatedAt = now
                    ),
                    ContraventionEntity(
                        code = "02",
                        description = "Parked or loading/unloading in a restricted street",
                        observationTimeMinutes = 5,
                        penaltyAmount = BigDecimal("70.00"),
                        streetId = streetId,
                        createdAt = now,
                        updatedAt = now
                    ),
                    ContraventionEntity(
                        code = "06",
                        description = "Parked without clearly displaying a valid pay & display ticket",
                        observationTimeMinutes = 0,
                        penaltyAmount = BigDecimal("70.00"),
                        streetId = streetId,
                        createdAt = now,
                        updatedAt = now
                    ),
                    ContraventionEntity(
                        code = "07",
                        description = "Parked with payment made to extend the stay beyond initial time",
                        observationTimeMinutes = 0,
                        penaltyAmount = BigDecimal("70.00"),
                        streetId = streetId,
                        createdAt = now,
                        updatedAt = now
                    ),
                    ContraventionEntity(
                        code = "12",
                        description = "Parked in a residents parking place without a valid permit",
                        observationTimeMinutes = 0,
                        penaltyAmount = BigDecimal("70.00"),
                        streetId = streetId,
                        createdAt = now,
                        updatedAt = now
                    ),
                    ContraventionEntity(
                        code = "16",
                        description = "Parked in a permit space without displaying a valid permit",
                        observationTimeMinutes = 0,
                        penaltyAmount = BigDecimal("70.00"),
                        streetId = streetId,
                        createdAt = now,
                        updatedAt = now
                    ),
                    ContraventionEntity(
                        code = "19",
                        description = "Parked in a residents parking place displaying an invalid permit",
                        observationTimeMinutes = 0,
                        penaltyAmount = BigDecimal("70.00"),
                        streetId = streetId,
                        createdAt = now,
                        updatedAt = now
                    ),
                    ContraventionEntity(
                        code = "23",
                        description = "Parked in a parking place not designated for that class of vehicle",
                        observationTimeMinutes = 0,
                        penaltyAmount = BigDecimal("70.00"),
                        streetId = streetId,
                        createdAt = now,
                        updatedAt = now
                    ),
                    ContraventionEntity(
                        code = "25",
                        description = "Parked in a loading place during restricted hours",
                        observationTimeMinutes = 5,
                        penaltyAmount = BigDecimal("70.00"),
                        streetId = streetId,
                        createdAt = now,
                        updatedAt = now
                    ),
                    ContraventionEntity(
                        code = "30",
                        description = "Parked for longer than permitted",
                        observationTimeMinutes = 10,
                        penaltyAmount = BigDecimal("70.00"),
                        streetId = streetId,
                        createdAt = now,
                        updatedAt = now
                    )
                )
            )
        }
        
        database.contraventionDao().insertContraventions(contraventions)
    }
    
    private suspend fun populateVehicleMakes() {
        val now = LocalDateTime.now()
        val makes = listOf(
            VehicleMakeEntity(name = "Ford", createdAt = now, updatedAt = now),
            VehicleMakeEntity(name = "Volkswagen", createdAt = now, updatedAt = now),
            VehicleMakeEntity(name = "BMW", createdAt = now, updatedAt = now),
            VehicleMakeEntity(name = "Mercedes", createdAt = now, updatedAt = now),
            VehicleMakeEntity(name = "Toyota", createdAt = now, updatedAt = now)
        )
        database.vehicleDao().insertMakes(makes)
    }
    
    private suspend fun populateVehicleModels() {
        val now = LocalDateTime.now()
        val models = listOf(
            // Ford models (makeId = 1)
            VehicleModelEntity(name = "Focus", makeId = 1, createdAt = now, updatedAt = now),
            VehicleModelEntity(name = "Fiesta", makeId = 1, createdAt = now, updatedAt = now),
            VehicleModelEntity(name = "Mondeo", makeId = 1, createdAt = now, updatedAt = now),
            VehicleModelEntity(name = "Kuga", makeId = 1, createdAt = now, updatedAt = now),
            VehicleModelEntity(name = "Transit", makeId = 1, createdAt = now, updatedAt = now),
            
            // Volkswagen models (makeId = 2)
            VehicleModelEntity(name = "Golf", makeId = 2, createdAt = now, updatedAt = now),
            VehicleModelEntity(name = "Polo", makeId = 2, createdAt = now, updatedAt = now),
            VehicleModelEntity(name = "Passat", makeId = 2, createdAt = now, updatedAt = now),
            VehicleModelEntity(name = "Tiguan", makeId = 2, createdAt = now, updatedAt = now),
            VehicleModelEntity(name = "Arteon", makeId = 2, createdAt = now, updatedAt = now),
            
            // BMW models (makeId = 3)
            VehicleModelEntity(name = "3 Series", makeId = 3, createdAt = now, updatedAt = now),
            VehicleModelEntity(name = "5 Series", makeId = 3, createdAt = now, updatedAt = now),
            VehicleModelEntity(name = "X3", makeId = 3, createdAt = now, updatedAt = now),
            VehicleModelEntity(name = "X5", makeId = 3, createdAt = now, updatedAt = now),
            VehicleModelEntity(name = "i3", makeId = 3, createdAt = now, updatedAt = now),
            
            // Mercedes models (makeId = 4)
            VehicleModelEntity(name = "C-Class", makeId = 4, createdAt = now, updatedAt = now),
            VehicleModelEntity(name = "E-Class", makeId = 4, createdAt = now, updatedAt = now),
            VehicleModelEntity(name = "A-Class", makeId = 4, createdAt = now, updatedAt = now),
            VehicleModelEntity(name = "GLC", makeId = 4, createdAt = now, updatedAt = now),
            VehicleModelEntity(name = "GLE", makeId = 4, createdAt = now, updatedAt = now),
            
            // Toyota models (makeId = 5)
            VehicleModelEntity(name = "Corolla", makeId = 5, createdAt = now, updatedAt = now),
            VehicleModelEntity(name = "Camry", makeId = 5, createdAt = now, updatedAt = now),
            VehicleModelEntity(name = "RAV4", makeId = 5, createdAt = now, updatedAt = now),
            VehicleModelEntity(name = "Prius", makeId = 5, createdAt = now, updatedAt = now),
            VehicleModelEntity(name = "Yaris", makeId = 5, createdAt = now, updatedAt = now)
        )
        database.vehicleDao().insertModels(models)
    }
}