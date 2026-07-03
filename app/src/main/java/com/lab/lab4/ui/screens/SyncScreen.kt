package com.lab.lab4.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lab.lab4.Lab4App
import com.lab.lab4.ui.viewmodel.SyncCounts
import com.lab.lab4.ui.viewmodel.SyncViewModel

@Composable
fun SyncScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as Lab4App
    val viewModel: SyncViewModel = viewModel(
        factory = SyncViewModel.Factory(
            gpsRepository = app.gpsRepository,
            mediaRepository = app.mediaRepository,
            audioRepository = app.audioRepository
        )
    )
    val counts by viewModel.counts.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SyncCountsCard(counts)
        Button(
            onClick = {
                Toast.makeText(context, "Sincronización simulada: ${counts.total} registros", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.CloudSync, contentDescription = null)
            Text(" Simular sincronización")
        }
    }
}

@Composable
private fun SyncCountsCard(counts: SyncCounts) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SyncRow("Google GNSS", counts.gpsGoogle)
            SyncRow("Sensores GNSS", counts.gpsSensors)
            SyncRow("Fotos", counts.photos)
            SyncRow("Videos", counts.videos)
            SyncRow("Audios", counts.audios)
            SyncRow("Total", counts.total, highlight = true)
        }
    }
}

@Composable
private fun SyncRow(label: String, value: Int, highlight: Boolean = false) {
    Text(
        text = "$label: $value",
        style = if (highlight) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
        fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal
    )
}
