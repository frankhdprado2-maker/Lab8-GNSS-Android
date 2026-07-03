package com.lab.lab4.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.lab.lab4.Lab4App
import com.lab.lab4.ui.screens.AudioScreen
import com.lab.lab4.ui.screens.GpsScreen
import com.lab.lab4.ui.screens.LoginScreen
import com.lab.lab4.ui.screens.MediaScreen
import com.lab.lab4.ui.screens.ProfileScreen
import com.lab.lab4.ui.screens.RegisterScreen
import com.lab.lab4.ui.viewmodel.GpsViewModel
import com.lab.lab4.ui.viewmodel.SessionViewModel

private const val ROUTE_AUTH = "auth"
private const val ROUTE_LOGIN = "login"
private const val ROUTE_REGISTER = "register"
private const val ROUTE_MAIN = "main"

sealed class Ruta(val ruta: String, val etiqueta: String, val icono: ImageVector) {
    data object Gps : Ruta("gps", "GNSS", Icons.Default.Place)
    data object Media : Ruta("media", "Media", Icons.Default.PhotoCamera)
    data object Audio : Ruta("audio", "Audio", Icons.Default.Mic)
    data object Profile : Ruta("profile", "Perfil", Icons.Default.Person)
}

@Composable
fun AppNavigation(sessionViewModel: SessionViewModel) {
    val isLoggedIn by sessionViewModel.isLoggedIn.collectAsStateWithLifecycle()
    val rootNavController = rememberNavController()
    val rootBackStack by rootNavController.currentBackStackEntryAsState()
    val currentDestination = rootBackStack?.destination
    val isInAuthGraph = currentDestination?.hierarchy?.any { it.route == ROUTE_AUTH } == true
    val isInMainGraph = currentDestination?.hierarchy?.any { it.route == ROUTE_MAIN } == true

    LaunchedEffect(isLoggedIn, isInAuthGraph, isInMainGraph) {
        if (isLoggedIn && !isInMainGraph) {
            rootNavController.navigate(ROUTE_MAIN) {
                popUpTo(ROUTE_AUTH) { inclusive = true }
                launchSingleTop = true
            }
        } else if (!isLoggedIn && !isInAuthGraph) {
            rootNavController.navigate(ROUTE_AUTH) {
                popUpTo(ROUTE_MAIN) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = rootNavController,
        startDestination = ROUTE_AUTH
    ) {
        navigation(
            startDestination = ROUTE_LOGIN,
            route = ROUTE_AUTH
        ) {
            composable(ROUTE_LOGIN) {
                LoginScreen(
                    sessionViewModel = sessionViewModel,
                    onRegisterNavigate = { rootNavController.navigate(ROUTE_REGISTER) }
                )
            }
            composable(ROUTE_REGISTER) {
                RegisterScreen(
                    onBack = { rootNavController.popBackStack() },
                    onSubmit = { email, password, onResult ->
                        sessionViewModel.register(email, password, onResult)
                    }
                )
            }
        }

        composable(ROUTE_MAIN) {
            MainScaffold(sessionViewModel = sessionViewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainScaffold(sessionViewModel: SessionViewModel) {
    val context = LocalContext.current
    val app = context.applicationContext as Lab4App
    val gpsViewModel: GpsViewModel = viewModel(
        factory = GpsViewModel.Factory(app.gpsRepository)
    )

    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStack?.destination
    val tabs = listOf(Ruta.Gps, Ruta.Media, Ruta.Audio, Ruta.Profile)

    val title = when (currentDestination?.route) {
        Ruta.Gps.ruta -> "GNSS"
        Ruta.Media.ruta -> "Media"
        Ruta.Audio.ruta -> "Audio"
        Ruta.Profile.ruta -> "Perfil"
        else -> "Lab 7"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                tabs.forEach { tab ->
                    val selected = currentDestination?.hierarchy?.any { it.route == tab.ruta } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.ruta) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icono, contentDescription = tab.etiqueta) },
                        label = { Text(tab.etiqueta) }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Ruta.Gps.ruta,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Ruta.Gps.ruta) {
                GpsScreen(viewModel = gpsViewModel)
            }
            composable(Ruta.Media.ruta) {
                MediaScreen()
            }
            composable(Ruta.Audio.ruta) {
                AudioScreen()
            }
            composable(Ruta.Profile.ruta) {
                ProfileScreen(
                    sessionVm = sessionViewModel,
                    onLogout = { sessionViewModel.logout() }
                )
            }
        }
    }
}
