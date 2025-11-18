package com.example.finlogcalc.features.tripcalculator.driver

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.example.finlogcalc.MainActivity
import com.example.finlogcalc.R
import com.google.android.gms.location.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class ForegroundLocationService : LifecycleService() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var locationRequest: LocationRequest? = null

    private var currentTripStatus: TripStatus = TripStatus.NOT_STARTED
    private var totalTripTimeSeconds: Long = 0L
    private var distanceMeters: Double = 0.0
    private var isPreciseTrackingEnabled: Boolean = true

    private val _locationUpdates = MutableSharedFlow<Location>()
    val locationUpdates: SharedFlow<Location> = _locationUpdates.asSharedFlow()

    private var serviceJob: Job? = null
    private var timeUpdateJob: Job? = null

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): ForegroundLocationService = this@ForegroundLocationService
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        Log.d("FG_SVC", "onBind called")
        return binder
    }

    companion object {
        const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE"
        const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
        const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
        const val ACTION_UPDATE_SETTINGS = "ACTION_UPDATE_SETTINGS"

        const val EXTRA_IS_PRECISE_TRACKING = "EXTRA_IS_PRECISE_TRACKING"
        const val EXTRA_TRIP_STATUS = "EXTRA_TRIP_STATUS"

        private const val NOTIFICATION_CHANNEL_ID = "driver_trip_channel"
        private const val NOTIFICATION_ID = 1
        var isServiceRunning = false
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("FG_SVC", "onCreate called") 
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                Log.d("FG_SVC", "onLocationResult: ${locationResult.lastLocation}") 
                locationResult.lastLocation?.let {
                    lifecycleScope.launch {
                        Log.d("FG_SVC", "Emitting location to SharedFlow: ${it.latitude}, ${it.longitude}") 
                        _locationUpdates.emit(it)
                    }
                    updateNotification()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        Log.d("FG_SVC", "onStartCommand received action: ${intent?.action}")
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    isPreciseTrackingEnabled = it.getBooleanExtra(EXTRA_IS_PRECISE_TRACKING, true)
                    val statusString = it.getStringExtra(EXTRA_TRIP_STATUS) ?: TripStatus.IN_PROGRESS.name
                    currentTripStatus = TripStatus.valueOf(statusString)
                    startForegroundServiceInternal()
                    isServiceRunning = true
                }
                ACTION_PAUSE_SERVICE -> {
                    currentTripStatus = TripStatus.PAUSED
                    pauseLocationUpdatesInternal()
                    updateNotification()
                }
                ACTION_STOP_SERVICE -> {
                    stopForegroundServiceInternal()
                }
                ACTION_UPDATE_SETTINGS -> {
                    isPreciseTrackingEnabled = it.getBooleanExtra(EXTRA_IS_PRECISE_TRACKING, isPreciseTrackingEnabled)
                    if (currentTripStatus == TripStatus.IN_PROGRESS) {
                         stopLocationUpdates() // Stop with old settings
                         startLocationUpdates() // Restart with new settings
                    }
                    updateNotification() // Update notification reflecting potential changes
                }
            }
        }
        return START_STICKY
    }

    private fun startForegroundServiceInternal() {
        Log.d("FG_SVC", "startForegroundServiceInternal called")
        startLocationUpdates()
        startForeground(NOTIFICATION_ID, getNotificationBuilder().build())
        startTimerForNotification()
    }

    private fun pauseLocationUpdatesInternal() {
        Log.d("FG_SVC", "pauseLocationUpdatesInternal called")
        stopLocationUpdates()
        timeUpdateJob?.cancel() 
        updateNotification()
    }

    private fun stopForegroundServiceInternal() {
        Log.d("FG_SVC", "stopForegroundServiceInternal called")
        stopLocationUpdates()
        timeUpdateJob?.cancel()
        stopForeground(true)
        stopSelf()
        isServiceRunning = false
        currentTripStatus = TripStatus.COMPLETED 
    }

    private fun startLocationUpdates() {
        Log.d("FG_SVC", "startLocationUpdates called, isPrecise: $isPreciseTrackingEnabled")
        locationRequest = createLocationRequest(isPreciseTrackingEnabled)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("FG_SVC", "Location permissions not granted in startLocationUpdates. Stopping service.")
            stopForegroundServiceInternal()
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest!!, locationCallback, Looper.getMainLooper())
        Log.d("FG_SVC", "Requested location updates from FusedLocationProviderClient with interval: ${locationRequest?.interval}ms, minInterval: ${locationRequest?.minUpdateIntervalMillis}ms")
        if(currentTripStatus != TripStatus.PAUSED) currentTripStatus = TripStatus.IN_PROGRESS
        updateNotification()
    }

    private fun stopLocationUpdates() {
        Log.d("FG_SVC", "stopLocationUpdates called")
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun startTimerForNotification() {
        Log.d("FG_SVC", "startTimerForNotification called")
        timeUpdateJob?.cancel()
        timeUpdateJob = lifecycleScope.launch {
            while (currentTripStatus == TripStatus.IN_PROGRESS || currentTripStatus == TripStatus.PAUSED) {
                delay(1000)
                if (currentTripStatus == TripStatus.IN_PROGRESS || currentTripStatus == TripStatus.PAUSED) {
                     totalTripTimeSeconds++ 
                }
                updateNotification()
            }
            Log.d("FG_SVC", "Notification timer loop ended. Status: $currentTripStatus")
        }
    }

    private fun createLocationRequest(isPrecise: Boolean): LocationRequest {
        Log.d("FG_SVC", "Creating LocationRequest. Precise: $isPrecise")
        return LocationRequest.Builder(
            if (isPrecise) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            if (isPrecise) 1000L else 10000L // Interval: 1s for precise, 10s for balanced
        ).apply {
            setMinUpdateIntervalMillis(if (isPrecise) 1000L else 5000L) // Min Interval: 1s for precise, 5s for balanced
            setMinUpdateDistanceMeters(if (isPrecise) 5f else 25f) // Min Distance
            //setWaitForAccurateLocation(true) // Consider this for precise mode if initial points are off
        }.build()
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, getNotificationBuilder().build())
    }

    private fun getNotificationBuilder(): NotificationCompat.Builder {
        val currentDistanceKmForNotification = distanceMeters / 1000.0

        val notificationText = when(currentTripStatus) {
            TripStatus.IN_PROGRESS -> getString(R.string.notification_text_in_progress, formatTime(totalTripTimeSeconds), currentDistanceKmForNotification)
            TripStatus.PAUSED -> getString(R.string.notification_text_paused, formatTime(totalTripTimeSeconds))
            else -> getString(R.string.app_name)
        }

        val stopIntent = Intent(this, ForegroundLocationService::class.java).apply {
            action = ACTION_STOP_SERVICE
        }
        val stopPendingIntent = PendingIntent.getService(this, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val activityPendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, DriverTripActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(android.R.drawable.ic_dialog_info) 
            .setContentTitle(getString(R.string.driver_trip_notification_title))
            .setContentText(notificationText)
            .setContentIntent(activityPendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, getString(R.string.button_end_trip), stopPendingIntent) 
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                getString(R.string.driver_trip_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
            channel.description = "Channel for driver trip tracking notifications"
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun formatTime(totalSeconds: Long): String {
        val hours = TimeUnit.SECONDS.toHours(totalSeconds)
        val minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) % 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("FG_SVC", "onDestroy called")
        serviceJob?.cancel()
        timeUpdateJob?.cancel()
        stopLocationUpdates()
        isServiceRunning = false
    }
}
