package com.lab.lab4.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.lab.lab4.data.local.AppDatabase
import com.lab.lab4.data.local.entity.GpsGoogleEntity
import com.lab.lab4.data.local.entity.GpsSensorsEntity
import com.lab.lab4.data.repository.GpsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlinx.coroutines.cancel

class GpsCaptureService : Service() {

    companion object {
        private const val INTERVAL_MS = 10_000L
        private const val SENSOR_TIMEOUT_MS = 5_000L
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "gps_capture_channel"
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var captureJob: Job? = null

    private val gpsRepo by lazy {
        val db = AppDatabase.getDatabase(applicationContext)
        GpsRepository(db.gpsGoogleDao(), db.gpsSensorsDao())
    }

    private val fusedClient by lazy {
        LocationServices.getFusedLocationProviderClient(applicationContext)
    }

    private val locationManager by lazy {
        getSystemService(LOCATION_SERVICE) as LocationManager
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (captureJob == null) {
            captureJob = scope.launch {
                while (isActive) {
                    performCaptures()
                    delay(INTERVAL_MS)
                }
            }
        }

        return START_STICKY
    }

    private suspend fun performCaptures() {
        val hasFineLocation =
            checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED

        val hasCoarseLocation =
            checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED

        if (!hasFineLocation && !hasCoarseLocation) {
            return
        }

        val now = System.currentTimeMillis()

        try {
            val loc = fusedClient
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .await()

            loc?.let {
                gpsRepo.saveGooglePoint(
                    GpsGoogleEntity(
                        latitude = it.latitude,
                        longitude = it.longitude,
                        accuracy = it.accuracy,
                        speed = if (it.hasSpeed()) it.speed else null,
                        bearing = if (it.hasBearing()) it.bearing else null,
                        timestamp = now
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val sensorLoc = withTimeoutOrNull(SENSOR_TIMEOUT_MS) {
                getRawGpsLocation()
            }

            gpsRepo.saveSensorsPoint(
                GpsSensorsEntity(
                    latitude = sensorLoc?.latitude,
                    longitude = sensorLoc?.longitude,
                    provider = LocationManager.GPS_PROVIDER,
                    altitude = if (sensorLoc?.hasAltitude() == true) sensorLoc.altitude else null,
                    timestamp = now
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    @SuppressLint("MissingPermission")
    private suspend fun getRawGpsLocation(): Location? = suspendCancellableCoroutine { continuation ->

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                locationManager.removeUpdates(this)

                if (continuation.isActive) {
                    continuation.resume(location)
                }
            }
        }

        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0L,
                0f,
                listener,
                mainLooper
            )
        } catch (e: Exception) {
            if (continuation.isActive) {
                continuation.resume(null)
            }
        }

        continuation.invokeOnCancellation {
            locationManager.removeUpdates(listener)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        captureJob?.cancel()
        scope.cancel()
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Captura GNSS Activa")
            .setContentText("Registrando coordenadas en paralelo cada 10s...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Servicio GNSS",
                NotificationManager.IMPORTANCE_LOW
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
}