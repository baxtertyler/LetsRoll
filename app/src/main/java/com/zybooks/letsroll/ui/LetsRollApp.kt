package com.zybooks.letsroll.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable

sealed class Routes {
    @Serializable
    data object HomeScreen

    @Serializable
    data object GameScreen
}

@Composable
fun LetsRollApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HomeScreen
    ) {
        composable<Routes.HomeScreen> {
            HomeScreen(startGame = {navController.navigate(Routes.GameScreen)})
        }
        composable<Routes.GameScreen> {
            GameScreen()
        }
    }
}