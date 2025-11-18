package com.example.finlogcalc.features.tripcalculator.driver.menu

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.finlogcalc.features.tripcalculator.driver.ActiveTripScreen
import com.example.finlogcalc.features.tripcalculator.driver.DriverTripViewModel
import com.example.finlogcalc.features.tripcalculator.driver.TripStatus
import com.example.finlogcalc.features.tripcalculator.driver.mytrips.MyTripsScreen
import com.example.finlogcalc.features.tripcalculator.driver.newtrip.NewTripScreen
import com.example.finlogcalc.features.tripcalculator.driver.reports.ReportsScreen
import com.example.finlogcalc.features.tripcalculator.driver.routes.RoutesScreen

@Composable
fun TripCalculatorNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    requestPermissions: () -> Unit,
    viewModel: DriverTripViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Автоматическая навигация на экран активного рейса
    LaunchedEffect(uiState.tripStatus) {
        when (uiState.tripStatus) {
            TripStatus.IN_PROGRESS, TripStatus.PAUSED -> {
                if (navController.currentDestination?.route != TripCalculatorDestinations.ACTIVE_TRIP_ROUTE) {
                    navController.navigate(TripCalculatorDestinations.ACTIVE_TRIP_ROUTE) {
                        popUpTo(TripCalculatorDestinations.MAIN_MENU_ROUTE) { inclusive = false }
                    }
                }
            }
            TripStatus.COMPLETED, TripStatus.NOT_STARTED -> {
                if (navController.currentDestination?.route == TripCalculatorDestinations.ACTIVE_TRIP_ROUTE) {
                    navController.popBackStack()
                }
            }
        }
    }
    
    NavHost(
        navController = navController,
        startDestination = TripCalculatorDestinations.MAIN_MENU_ROUTE,
        modifier = modifier
    ) {
        composable(TripCalculatorDestinations.MAIN_MENU_ROUTE) {
            TripMainMenuScreen(
                navController = navController,
                viewModel = viewModel,
                requestPermissions = requestPermissions
            )
        }
        composable(TripCalculatorDestinations.NEW_TRIP_ROUTE) {
            NewTripScreen(navController = navController, viewModel = viewModel, requestPermissions = requestPermissions)
        }
        composable(TripCalculatorDestinations.ACTIVE_TRIP_ROUTE) {
            ActiveTripScreen(
                navController = navController,
                viewModel = viewModel,
                uiState = uiState
            )
        }
        composable(TripCalculatorDestinations.MY_TRIPS_ROUTE) {
            MyTripsScreen(navController = navController)
        }
        composable(TripCalculatorDestinations.ROUTES_ROUTE) {
            RoutesScreen(navController = navController)
        }
        composable(TripCalculatorDestinations.REPORTS_ROUTE) {
            ReportsScreen(navController = navController)
        }
    }
}
