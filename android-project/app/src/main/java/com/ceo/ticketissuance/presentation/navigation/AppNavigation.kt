package com.ceo.ticketissuance.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.ceo.ticketissuance.presentation.ui.camera.CameraScreen
import com.ceo.ticketissuance.presentation.ui.common.PlaceholderScreen
import com.ceo.ticketissuance.presentation.ui.countdown.CountdownScreen
import com.ceo.ticketissuance.presentation.ui.dashboard.DashboardScreen
import com.ceo.ticketissuance.presentation.ui.login.LoginScreen
import com.ceo.ticketissuance.presentation.ui.observation.ObservationFormScreen
import com.ceo.ticketissuance.presentation.ui.splash.SplashScreen
import com.ceo.ticketissuance.presentation.ui.ticketissuance.TicketIssuanceScreen
import com.ceo.ticketissuance.presentation.ui.tickethistory.TicketHistoryScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToObservation = { navController.navigate(Screen.Camera.route) },
                onNavigateToIssuance = { navController.navigate(Screen.Issuance.route) },
                onNavigateToCountdown = { navController.navigate(Screen.Countdown.route) },
                onNavigateToQueue = { navController.navigate(Screen.Queue.route) },
                onNavigateToSync = { navController.navigate(Screen.Sync.route) },
                onNavigateToTicketHistory = { navController.navigate(Screen.TicketHistory.route) },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Camera screen for ANPR
        composable(Screen.Camera.route) {
            CameraScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToObservationForm = { detectedPlate ->
                    navController.navigate(Screen.Observation.createRoute(detectedPlate))
                }
            )
        }
        
        // Observation form screen
        composable(
            route = Screen.Observation.route,
            arguments = listOf(
                navArgument("plateNumber") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val plateNumber = backStackEntry.arguments?.getString("plateNumber")
            ObservationFormScreen(
                plateNumber = plateNumber,
                onNavigateBack = { navController.navigateUp() },
                onNavigateToCamera = { navController.navigate(Screen.Camera.route) },
                onNavigateToCountdown = { observationId ->
                    navController.navigate(Screen.CountdownDetails.createRoute(observationId))
                }
            )
        }
        
        composable(Screen.Issuance.route) {
            PlaceholderScreen("Issuance Screen - Coming in Phase 6")
        }
        
        // Ticket issuance from observation
        composable(
            route = Screen.IssuanceFromObservation.route,
            arguments = listOf(
                navArgument("observationId") { 
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val observationId = backStackEntry.arguments?.getLong("observationId") ?: -1L
            TicketIssuanceScreen(
                observationId = observationId,
                onNavigateBack = { navController.navigateUp() },
                onNavigateToHistory = { navController.navigate(Screen.TicketHistory.route) }
            )
        }
        
        // Countdown details screen
        composable(
            route = Screen.CountdownDetails.route,
            arguments = listOf(
                navArgument("observationId") { 
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val observationId = backStackEntry.arguments?.getLong("observationId") ?: -1L
            CountdownScreen(
                observationId = observationId,
                onNavigateBack = { navController.navigateUp() },
                onNavigateToIssuance = { obsId ->
                    navController.navigate(Screen.IssuanceFromObservation.createRoute(obsId))
                }
            )
        }
        
        composable(Screen.Countdown.route) {
            PlaceholderScreen("Countdown Screen - Coming in Phase 5")
        }
        
        composable(Screen.Queue.route) {
            PlaceholderScreen("Queue Screen - Coming in Phase 7")
        }
        
        composable(Screen.Sync.route) {
            PlaceholderScreen("Sync Screen - Coming in Phase 7")
        }
        
        composable(Screen.TicketHistory.route) {
            TicketHistoryScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToTicketDetail = { ticketId ->
                    // TODO: Navigate to ticket detail screen
                }
            )
        }
    }
}