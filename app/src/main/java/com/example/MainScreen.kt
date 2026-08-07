package com.example

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Help
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

@Composable
fun MainScreen(
    onStartIncident: () -> Unit,
    googleSignInClient: Any? = null // simplified for now
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = currentRoute == "home",
                    onClick = {
                        navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Star, contentDescription = "Premium") },
                    label = { Text("Premium") },
                    selected = currentRoute == "premium",
                    onClick = {
                        navController.navigate("premium") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.CreditCard, contentDescription = "ID Card") },
                    label = { Text("ID Card") },
                    selected = currentRoute == "id_card",
                    onClick = {
                        navController.navigate("id_card") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = currentRoute == "settings",
                    onClick = {
                        navController.navigate("settings") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Help, contentDescription = "Help") },
                    label = { Text("Help") },
                    selected = currentRoute == "help",
                    onClick = {
                        navController.navigate("help") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                fadeIn(animationSpec = tween(400)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(400)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(400))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(400)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(400)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(400))
            }
        ) {
            composable("home") {
                DashboardScreen(
                    onStartIncident = onStartIncident,
                    onShowInsuranceCard = { navController.navigate("id_card") }
                )
            }
            composable("premium") {
                PremiumServicesScreen()
            }
            composable("id_card") {
                InsuranceCardScreen()
            }
            composable("settings") {
                SettingsScreen()
            }
            composable("help") {
                HelpScreen()
            }
        }
    }
}
