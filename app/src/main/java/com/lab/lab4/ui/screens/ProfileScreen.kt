package com.lab.lab4.ui.screens

import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lab.lab4.Lab4App
import com.lab.lab4.data.local.entity.MediaType
import com.lab.lab4.ui.viewmodel.SessionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class RecordsSource { LOCAL, REMOTE, ALL }

data class ActivityItem(
    val id: String,
    val title: String,
    val detail: String,
    val timestamp: Long,
    val category: ActivityCategory,
    val isRemote: Boolean
)

enum class ActivityCategory(val label: String, val icon: ImageVector) {
    ALL("Todos", Icons.Default.Receipt),
    GNSS("GNSS", Icons.Default.Place),
    PHOTOS("Fotos", Icons.Default.Photo),
    VIDEOS("Videos", Icons.Default.Videocam),
    AUDIOS("Audios", Icons.Default.LibraryMusic)
}

private sealed class ProfileViewState {
    data object Menu : ProfileViewState()
    data object MyProfile : ProfileViewState()
    data class Records(val source: RecordsSource) : ProfileViewState()
    data object Sync : ProfileViewState()
    data object Notifications : ProfileViewState()
}

private data class ProfileOption(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun ProfileScreen(sessionVm: SessionViewModel, onLogout: () -> Unit) {
    var viewState by remember { mutableStateOf<ProfileViewState>(ProfileViewState.Menu) }
    val username by sessionVm.username.collectAsStateWithLifecycle()

    when (val state = viewState) {
        ProfileViewState.Menu -> ProfileMenu(
            username = username ?: "Usuario",
            onNavigateToProfile = { viewState = ProfileViewState.MyProfile },
            onNavigateToLocalRecords = { viewState = ProfileViewState.Records(RecordsSource.LOCAL) },
            onNavigateToAllRecords = { viewState = ProfileViewState.Records(RecordsSource.ALL) },
            onNavigateToSync = { viewState = ProfileViewState.Sync },
            onNavigateToNotifications = { viewState = ProfileViewState.Notifications },
            onLogoutClick = onLogout
        )
        ProfileViewState.MyProfile -> MyProfileScreen(
            sessionVm = sessionVm,
            username = username ?: "N/A",
            onBack = { viewState = ProfileViewState.Menu }
        )
        is ProfileViewState.Records -> RecordsExplorerScreen(
            initialSource = state.source,
            onBack = { viewState = ProfileViewState.Menu }
        )
        ProfileViewState.Sync -> NestedScreen(
            title = "Sincronización",
            onBack = { viewState = ProfileViewState.Menu }
        ) {
            SyncScreen()
        }
        ProfileViewState.Notifications -> NestedScreen(
            title = "Notificaciones",
            onBack = { viewState = ProfileViewState.Menu }
        ) {
            NotificationsScreen()
        }
    }
}

@Composable
private fun ProfileMenu(
    username: String,
    onNavigateToProfile: () -> Unit,
    onNavigateToLocalRecords: () -> Unit,
    onNavigateToAllRecords: () -> Unit,
    onNavigateToSync: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    val options = listOf(
        ProfileOption("Mi Perfil", "Datos de sesión, dispositivo y tema", Icons.Default.Person, onNavigateToProfile),
        ProfileOption("Registros locales", "Actividad guardada en Room y archivos", Icons.Default.Storage, onNavigateToLocalRecords),
        ProfileOption("Todos los registros", "Actividad local y registros de nube", Icons.Default.Receipt, onNavigateToAllRecords),
        ProfileOption("Sincronización", "Herramientas de sincronización del Lab 5", Icons.Default.CloudSync, onNavigateToSync),
        ProfileOption("Notificaciones", "Configuración y pruebas de notificaciones", Icons.Default.Notifications, onNavigateToNotifications)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = username,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Platform API - Lab 7",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        options.forEach { option ->
            ProfileOptionRow(option)
        }

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            onClick = { showLogoutDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Cerrar sesión")
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("¿Confirmar cierre de sesión?") },
            text = { Text("Se borrarán los tokens de sesión y se conservará el modo oscuro.") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogoutClick()
                }) {
                    Text("Sí, cerrar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ProfileOptionRow(option: ProfileOption) {
    Card(
        onClick = option.onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        ListItem(
            headlineContent = { Text(option.title, fontWeight = FontWeight.SemiBold) },
            supportingContent = { Text(option.description) },
            leadingContent = {
                Icon(option.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            trailingContent = {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            },
            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        )
    }
}

@Composable
private fun MyProfileScreen(sessionVm: SessionViewModel, username: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val isDarkModePref by sessionVm.isDarkMode.collectAsStateWithLifecycle()
    val isDark = isDarkModePref ?: isSystemInDarkTheme()
    val androidId = remember {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: "unknown_device"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ProfileHeader(title = "Mi Perfil", onBack = onBack)

        ProfileMetadataItem("Usuario", username)
        ProfileMetadataItem("Directorio local", context.filesDir.absolutePath)
        ProfileMetadataItem("Dispositivo", "${Build.MANUFACTURER} ${Build.MODEL}")
        ProfileMetadataItem("Versión Android", Build.VERSION.RELEASE)
        ProfileMetadataItem("API Level", Build.VERSION.SDK_INT.toString())
        ProfileMetadataItem("Android ID", androidId)

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            ListItem(
                headlineContent = { Text("Modo oscuro", fontWeight = FontWeight.SemiBold) },
                supportingContent = { Text("Control persistente en DataStore") },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordsExplorerScreen(initialSource: RecordsSource, onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as Lab4App
    val googlePoints by app.gpsRepository.googlePoints.collectAsStateWithLifecycle(initialValue = emptyList())
    val sensorsPoints by app.gpsRepository.sensorsPoints.collectAsStateWithLifecycle(initialValue = emptyList())
    val mediaItems by app.mediaRepository.allMedia.collectAsStateWithLifecycle(initialValue = emptyList())
    val audioItems by app.audioRepository.allAudios.collectAsStateWithLifecycle(initialValue = emptyList())
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()) }
    var source by remember { mutableStateOf(initialSource) }
    var category by remember { mutableStateOf(ActivityCategory.ALL) }

    val localItems = remember(googlePoints, sensorsPoints, mediaItems, audioItems) {
        buildList {
            googlePoints.forEach {
                add(
                    ActivityItem(
                        id = "google_${it.id}",
                        title = "GNSS Google",
                        detail = "Lat ${it.latitude}, Lon ${it.longitude}",
                        timestamp = it.timestamp,
                        category = ActivityCategory.GNSS,
                        isRemote = false
                    )
                )
            }
            sensorsPoints.forEach {
                val detail = if (it.latitude != null && it.longitude != null) {
                    "Lat ${it.latitude}, Lon ${it.longitude} (${it.provider})"
                } else {
                    "Sin fijación satelital (${it.provider})"
                }
                add(
                    ActivityItem(
                        id = "sensor_${it.id}",
                        title = "GNSS Sensores",
                        detail = detail,
                        timestamp = it.timestamp,
                        category = ActivityCategory.GNSS,
                        isRemote = false
                    )
                )
            }
            mediaItems.forEach {
                val isPhoto = it.type == MediaType.PHOTO.name
                add(
                    ActivityItem(
                        id = "media_${it.id}",
                        title = if (isPhoto) "Foto local" else "Video local",
                        detail = "${it.sizeBytes / 1024L} KB - ${it.filePath}",
                        timestamp = it.timestamp,
                        category = if (isPhoto) ActivityCategory.PHOTOS else ActivityCategory.VIDEOS,
                        isRemote = false
                    )
                )
            }
            audioItems.forEach {
                add(
                    ActivityItem(
                        id = "audio_${it.id}",
                        title = "Audio local",
                        detail = "${it.format.uppercase()} - ${it.durationMs / 1000L}s - ${it.sizeBytes / 1024L} KB",
                        timestamp = it.timestamp,
                        category = ActivityCategory.AUDIOS,
                        isRemote = false
                    )
                )
            }
        }
    }

    val remotePlaceholders = remember {
        // Placeholders hasta integrar endpoints reales de registros remotos en Platform API.
        val now = System.currentTimeMillis()
        listOf(
            ActivityItem("remote_gnss_1", "GNSS en nube", "Registro remoto sincronizado", now - 3_600_000L, ActivityCategory.GNSS, true),
            ActivityItem("remote_photo_1", "Foto en nube", "Archivo remoto pendiente de detalle API", now - 7_200_000L, ActivityCategory.PHOTOS, true),
            ActivityItem("remote_audio_1", "Audio en nube", "Muestra remota para filtro Nube", now - 10_800_000L, ActivityCategory.AUDIOS, true)
        )
    }

    val visibleItems = remember(source, category, localItems, remotePlaceholders) {
        val sourceItems = when (source) {
            RecordsSource.LOCAL -> localItems
            RecordsSource.REMOTE -> remotePlaceholders
            RecordsSource.ALL -> localItems + remotePlaceholders
        }
        sourceItems
            .filter { category == ActivityCategory.ALL || it.category == category }
            .sortedByDescending { it.timestamp }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ProfileHeader(title = "Registros", onBack = onBack)

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            listOf(
                RecordsSource.ALL to "Todo",
                RecordsSource.LOCAL to "Local",
                RecordsSource.REMOTE to "Nube"
            ).forEachIndexed { index, item ->
                SegmentedButton(
                    selected = source == item.first,
                    onClick = { source = item.first },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = 3)
                ) {
                    Text(item.second)
                }
            }
        }

        ScrollableTabRow(selectedTabIndex = ActivityCategory.entries.indexOf(category)) {
            ActivityCategory.entries.forEach { tab ->
                Tab(
                    selected = category == tab,
                    onClick = { category = tab },
                    text = { Text(tab.label) },
                    icon = { Icon(tab.icon, contentDescription = null) }
                )
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (visibleItems.isEmpty()) {
                item {
                    EmptyRecordsMessage()
                }
            } else {
                items(visibleItems, key = { it.id }) { item ->
                    ActivityRow(item = item, date = dateFormat.format(Date(item.timestamp)))
                }
            }
        }
    }
}

@Composable
private fun NestedScreen(
    title: String,
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
            }
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        HorizontalDivider()
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

@Composable
private fun ProfileHeader(title: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
        }
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ActivityRow(item: ActivityItem, date: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        ListItem(
            headlineContent = { Text(item.title, fontWeight = FontWeight.SemiBold) },
            supportingContent = { Text("${item.detail}\n$date") },
            leadingContent = {
                Icon(
                    imageVector = if (item.isRemote) Icons.Default.Cloud else item.category.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingContent = {
                FilterChip(
                    selected = item.isRemote,
                    onClick = {},
                    label = { Text(if (item.isRemote) "Nube" else "Local") }
                )
            },
            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
        )
    }
}

@Composable
private fun EmptyRecordsMessage() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No hay registros para este filtro.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProfileMetadataItem(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
    }
}
