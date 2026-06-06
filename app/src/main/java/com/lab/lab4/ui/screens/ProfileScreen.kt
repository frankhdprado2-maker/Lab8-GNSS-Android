package com.lab.lab4.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lab.lab4.ui.viewmodel.SessionViewModel
// Definición de las 3 sub-vistas internas del perfil usando sealed class
private sealed class ProfileViewState {
    object Menu       : ProfileViewState()
    object MyProfile  : ProfileViewState()
    object MyActivity : ProfileViewState()
}

data class OpcionPerfil(val id: Int, val titulo: String, val descripcion: String, val icono: ImageVector)

@Composable
fun ProfileScreen(sessionVm: SessionViewModel, onLogout: () -> Unit) {
    var viewState by remember { mutableStateOf<ProfileViewState>(ProfileViewState.Menu) }
    val username by sessionVm.username.collectAsStateWithLifecycle()

    // Máquina de estados interna para alternar entre sub-pantallas
    when (viewState) {
        is ProfileViewState.Menu -> ProfileMenu(
            username = username ?: "Estudiante San Marcos",
            onNavigateToProfile = { viewState = ProfileViewState.MyProfile },
            onNavigateToActivity = { viewState = ProfileViewState.MyActivity },
            onLogoutClick = onLogout
        )
        is ProfileViewState.MyProfile -> MyProfileScreen(
            sessionVm = sessionVm,
            username = username ?: "N/A",
            onBack = { viewState = ProfileViewState.Menu }
        )
        is ProfileViewState.MyActivity -> MyActivityScreen(
            onBack = { viewState = ProfileViewState.Menu }
        )
    }
}

// ── 11.1 SUB-PANTALLA: MENÚ PRINCIPAL DEL PERFIL ──
@Composable
private fun ProfileMenu(
    username: String,
    onNavigateToProfile: () -> Unit,
    onNavigateToActivity: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var mostrarDialogo by remember { mutableStateOf(false) }

    val opciones = remember {
        listOf(
            OpcionPerfil(1, "Mis datos", "Información del estudiante y dispositivo", Icons.Default.Person),
            OpcionPerfil(2, "Historial de Actividad", "Registros consolidados del sistema", Icons.Default.Receipt)
        )
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cabecera con Avatar Circular
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(36.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = username, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(text = "9no Ciclo — UNMSM", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }

        // Listado de opciones de menú
        opciones.forEach { opcion ->
            Card(
                onClick = { if (opcion.id == 1) onNavigateToProfile() else onNavigateToActivity() },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                ListItem(
                    headlineContent = { Text(opcion.titulo, fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text(opcion.descripcion) },
                    leadingContent = { Icon(opcion.icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Botón de Cerrar Sesión Semántico
        OutlinedButton(
            onClick = { mostrarDialogo = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar sesión")
        }
    }

    // Alerta de confirmación para evitar cierres accidentales
    if (mostrarDialogo) {
        AlertDialog(
            onDismissRequest = { mostrarDialogo = false },
            title = { Text("¿Confirmar cierre de sesión?") },
            text = { Text("Tus preferencias visuales del dispositivo se conservarán.") },
            confirmButton = {
                TextButton(onClick = { mostrarDialogo = false; onLogoutClick() }) {
                    Text("Sí, cerrar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogo = false }) { Text("Cancelar") }
            }
        )
    }
}

// ── 11.2 SUB-PANTALLA: VER MIS DATOS Y CONFIGURAR MODO OSCURO ──
@Composable
private fun MyProfileScreen(sessionVm: SessionViewModel, username: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val isDarkModePref by sessionVm.isDarkMode.collectAsStateWithLifecycle()
    val isDark = isDarkModePref ?: isSystemInDarkTheme()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Regresar") }
            Text("Mis Datos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        // Visualización pedagógica de Metadatos del Sistema y Almacenamiento Local
        ProfileMetadataItem("Nombre de Usuario", username)
        ProfileMetadataItem("Rol de Acceso", "Estudiante / Evaluador")
        ProfileMetadataItem("Directorio Local Interno", context.filesDir.absolutePath)
        ProfileMetadataItem("Fabricante del Equipo", Build.MANUFACTURER.uppercase())
        ProfileMetadataItem("Modelo del Dispositivo", Build.MODEL)
        ProfileMetadataItem("Versión de Android", "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Card para el control del Modo Oscuro Persistente
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            ListItem(
                headlineContent = { Text("Modo oscuro", fontWeight = FontWeight.SemiBold) },
                supportingContent = { Text("Forzar aspecto visual nocturno") },
                trailingContent = {
                    Switch(
                        checked = isDark,
                        onCheckedChange = { sessionVm.setDarkMode(it) }
                    )
                },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
            )
        }
    }
}

// ── 11.3 SUB-PANTALLA: LISTA DE ACTIVIDAD (REQUERIDA POR EL CHECKLIST) ──
@Composable
private fun MyActivityScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Regresar") }
            Text("Historial de Actividad", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        // En este paso se renderiza la vista estática unificada requerida
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = "No hay archivos multimedia registrados en este ciclo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ProfileMetadataItem(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
    }
}