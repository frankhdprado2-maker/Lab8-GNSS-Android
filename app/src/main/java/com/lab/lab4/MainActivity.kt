package com.lab.lab4

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lab.lab4.data.local.AppDatabase
import com.lab.lab4.data.repository.GpsRepository
import com.lab.lab4.data.session.SessionManager
import com.lab.lab4.ui.navigation.AppNavigation
import com.lab.lab4.ui.theme.AppTheme
import com.lab.lab4.ui.viewmodel.GpsViewModel
import com.lab.lab4.ui.viewmodel.SessionViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(applicationContext)
        val gpsRepository = GpsRepository(database.gpsGoogleDao(), database.gpsSensorsDao())
        val sessionManager = SessionManager(applicationContext)

        val gpsViewModel = GpsViewModel(gpsRepository)
        val sessionViewModel = SessionViewModel(sessionManager)

        setContent {
            val isDarkModePref by sessionViewModel.isDarkMode.collectAsStateWithLifecycle()
            val usarModoOscuro = isDarkModePref ?: isSystemInDarkTheme()

            AppTheme(
                darkTheme = usarModoOscuro,
                dynamicColor = false
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        gpsViewModel = gpsViewModel,
                        sessionViewModel = sessionViewModel
                    )
                }
            }
        }
    }
}