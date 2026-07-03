package com.lab.lab4.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.lab.lab4.Lab4App
import com.lab.lab4.data.local.entity.MediaEntity
import com.lab.lab4.data.local.entity.MediaType
import com.lab.lab4.ui.viewmodel.MediaViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MediaScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as Lab4App
    val viewModel: MediaViewModel = viewModel(
        factory = MediaViewModel.Factory(app.mediaRepository)
    )
    val media by viewModel.media.collectAsStateWithLifecycle()

    var hasCameraPermission by remember { mutableStateOf(false) }
    var pendingFile by remember { mutableStateOf<File?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { hasCameraPermission = it }
    )

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val file = pendingFile
        if (success && file != null) {
            viewModel.registerPhoto(file)
        } else {
            viewModel.deletePending(file)
        }
        pendingFile = null
    }

    val captureVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        val file = pendingFile
        if (success && file != null) {
            viewModel.registerVideo(file)
        } else {
            viewModel.deletePending(file)
        }
        pendingFile = null
    }

    fun fileUri(file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    if (!hasCameraPermission) {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                        return@Button
                    }
                    val file = viewModel.newPhotoFile()
                    pendingFile = file
                    takePictureLauncher.launch(fileUri(file))
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text("Foto")
            }

            OutlinedButton(
                onClick = {
                    if (!hasCameraPermission) {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                        return@OutlinedButton
                    }
                    val file = viewModel.newVideoFile()
                    pendingFile = file
                    captureVideoLauncher.launch(fileUri(file))
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Videocam, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text("Video")
            }
        }

        Text("Capturas multimedia", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(media, key = { it.id }) { item ->
                MediaItemCard(item = item, onDelete = { viewModel.delete(item) })
            }
        }
    }
}

@Composable
private fun MediaItemCard(item: MediaEntity, onDelete: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.type == MediaType.PHOTO.name) {
                AsyncImage(
                    model = File(item.filePath),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(88.dp)
                        .aspectRatio(1f)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(item.type, fontWeight = FontWeight.Bold)
                Text("Tamaño: ${formatBytes(item.sizeBytes)}", style = MaterialTheme.typography.bodySmall)
                item.durationMs?.let {
                    Text("Duración: ${formatDuration(it)}", style = MaterialTheme.typography.bodySmall)
                }
                if (item.widthPx != null && item.heightPx != null) {
                    Text("${item.widthPx} x ${item.heightPx}px", style = MaterialTheme.typography.bodySmall)
                }
                Text(dateFormat.format(Date(item.timestamp)), style = MaterialTheme.typography.bodySmall)
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
            }
        }
    }
}

private fun formatBytes(bytes: Long): String =
    if (bytes < 1024L * 1024L) "${bytes / 1024L} KB" else "%.2f MB".format(bytes / 1024.0 / 1024.0)

private fun formatDuration(durationMs: Long): String {
    val seconds = durationMs / 1_000L
    return "%02d:%02d".format(seconds / 60L, seconds % 60L)
}
