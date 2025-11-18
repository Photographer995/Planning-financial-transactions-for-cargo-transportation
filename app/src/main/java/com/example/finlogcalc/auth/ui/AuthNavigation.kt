package com.example.finlogcalc.auth.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AuthNavigation(rootNavController: NavController) {
    val authNavController = rememberNavController()
    NavHost(navController = authNavController, startDestination = "login") {
        composable("login") {
            LoginScreen(navController = authNavController, rootNavController = rootNavController)
        }
        composable("registration") {
            RegistrationScreen(navController = authNavController, rootNavController = rootNavController)
        }
    }
}