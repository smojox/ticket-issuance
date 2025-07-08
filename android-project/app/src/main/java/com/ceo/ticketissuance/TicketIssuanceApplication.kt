package com.ceo.ticketissuance

import android.app.Application
import com.ceo.ticketissuance.data.database.DatabasePopulator
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class TicketIssuanceApplication : Application() {
    
    @Inject
    lateinit var databasePopulator: DatabasePopulator
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize database with dummy data
        applicationScope.launch {
            databasePopulator.populateDatabase()
        }
    }
}