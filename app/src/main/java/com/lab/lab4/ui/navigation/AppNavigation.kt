package com.lab.lab4.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
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
import com.lab.lab4.Lab4App
import com.lab.lab4.ui.screens.AudioScreen
import com.lab.lab4.ui.screens.GpsScreen
import com.lab.lab4.ui.screens.LoginScreen
import com.lab.lab4.ui.screens.MediaScreen
import com.lab.lab4.ui.screens.NotificationsScreen
import com.lab.lab4.ui.screens.ProfileScreen
import com.lab.lab4.ui.screens.SyncScreen
import com.lab.lab4.ui.viewmodel.GpsViewModel
import com.lab.lab4.ui.viewmodel.SessionViewModel

sealed class Ruta(val ruta: String, val etiqueta: String, val icono: ImageVector) {
    data object Gps : Ruta("gps", "GNSS", Icons.Default.Place)
    data object Media : Ruta("media", "Multimedia", Icons.Default.PhotoCamera)
    data object Audio : Ruta("audio", "Audio", Icons.Default.Mic)
    data object Sync : Ruta("sync", "Sync", Icons.Default.CloudSync)
    data object Notif : Ruta("notif", "Notif", Icons.Default.Notifications)
    data object Profile : Ruta("profile", "Perfil", Icons.Default.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(sessionViewModel: SessionViewModel) {
    val isLoggedIn by sessionViewModel.isLoggedIn.collectAsStateWithLifecycle()

    if (!isLoggedIn) {
        LoginScreen(sessionViewModel = sessionViewModel)
        return
    }

    val context = LocalContext.current
    val app = context.applicationContext as Lab4App
    val gpsViewModel: GpsViewModel = viewModel(
        factory = GpsViewModel.Factory(app.gpsRepository)
    )

    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStack?.destination
    val tabs = listOf(Ruta.Gps, Ruta.Media, Ruta.Audio, Ruta.Sync, Ruta.Notif, Ruta.Profile)

    val title = when (currentDestination?.route) {
        Ruta.Gps.ruta -> "GNSS"
        Ruta.Media.ruta -> "Multimedia"
        Ruta.Audio.ruta -> "Audio"
        Ruta.Sync.ruta -> "Sync"
        Ruta.Notif.ruta -> "Notificaciones"
        Ruta.Profile.ruta -> "Perfil"
        else -> "Lab 5"
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
            composable(Ruta.Sync.ruta) {
                SyncScreen()
            }
            composable(Ruta.Notif.ruta) {
                NotificationsScreen()
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
