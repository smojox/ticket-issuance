package com.ceo.ticketissuance.presentation.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object Camera : Screen("camera")
    object Observation : Screen("observation/{plateNumber?}") {
        fun createRoute(plateNumber: String? = null) = if (plateNumber != null) {
            "observation/$plateNumber"
        } else {
            "observation/"
        }
    }
    object Countdown : Screen("countdown")
    object Issuance : Screen("issuance")
    object Queue : Screen("queue")
    object Sync : Screen("sync")
    object TicketHistory : Screen("ticket_history")
    
    // Parameterized routes
    object ObservationDetails : Screen("observation/{observationId}") {
        fun createRoute(observationId: Long) = "observation/$observationId"
    }
    
    object IssuanceFromObservation : Screen("issuance/{observationId}") {
        fun createRoute(observationId: Long) = "issuance/$observationId"
    }
    
    object CountdownDetails : Screen("countdown/{observationId}") {
        fun createRoute(observationId: Long) = "countdown/$observationId"
    }
}