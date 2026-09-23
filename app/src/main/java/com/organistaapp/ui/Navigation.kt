package com.organistaapp.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.organistaapp.ui.screens.calendar.CalendarioScreen
import com.organistaapp.ui.screens.home.HomeScreen
import com.organistaapp.ui.screens.profile.PerfilScreen
import com.organistaapp.ui.screens.upload.UploadScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Início", Icons.Filled.Home)
    object Upload : Screen("upload", "Escala", Icons.Filled.Upload)
    object Calendario : Screen("calendario", "Calendário", Icons.Filled.CalendarMonth)
    object Perfil : Screen("perfil", "Perfil", Icons.Filled.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganistaNavHost() {
    val navController = rememberNavController()
    val items = listOf(Screen.Home, Screen.Upload, Screen.Calendario, Screen.Perfil)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onVerCalendario = { navController.navigate(Screen.Calendario.route) },
                    onUpload = { navController.navigate(Screen.Upload.route) }
                )
            }
            composable(Screen.Upload.route) {
                UploadScreen()
            }
            composable(Screen.Calendario.route) {
                CalendarioScreen()
            }
            composable(Screen.Perfil.route) {
                PerfilScreen()
            }
        }
    }
}
