package com.example.finlogcalc.features.tripcalculator.driver

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.rememberNavController
import com.example.finlogcalc.R
import com.example.finlogcalc.features.tripcalculator.driver.menu.TripCalculatorNavHost
import com.example.finlogcalc.ui.theme.FinLogCalcTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import java.util.Locale

enum class TripStatus {
    NOT_STARTED, IN_PROGRESS, PAUSED, COMPLETED
}

data class DriverTripUiState(
    val tripStatus: TripStatus = TripStatus.NOT_STARTED,
    val totalTripTimeSeconds: Long = 0L,
    val distance: Double = 0.0, // In km or miles based on useKilometers
    val movingTimeSeconds: Long = 0L,
    val idleTimeSeconds: Long = 0L,
    val currentSpeed: Double = 0.0, // In km/h or mph based on useKilometers
    val isPreciseTracking: Boolean = true,
    val idleThresholdMinutes: Int = 5,
    val useKilometers: Boolean = true,
    val locationPermissionsGranted: Boolean = false,
    val notificationPermissionGranted: Boolean = false // Specifically for Android 13+
)

open class DriverTripViewModel(private val applicationContext: Context) : ViewModel() {
    protected val _uiState = MutableStateFlow(DriverTripUiState())
    open val uiState: StateFlow<DriverTripUiState> = _uiState.asStateFlow()

    private val tripTrack = mutableListOf<Location>()
    private var previousLocationForCalc: Location? = null
    private var lastSignificantMoveLocation: Location? = null
    private var timeOfLastSignificantMoveMs: Long = 0L

    private var tripStartTimeMillis: Long = 0L
    private var maxSpeedKmh = 0.0
    private val detectedStops = mutableListOf<TripStop>()
    private data class StopInProgress(val startTimeMillis: Long, val startLocation: Location?)
    private var currentStopInProgress: StopInProgress? = null

    private val _lastTripReport = MutableStateFlow<TripReport?>(null)
    val lastTripReport: StateFlow<TripReport?> = _lastTripReport.asStateFlow()

    private val speedReadings = mutableListOf<Double>()

    companion object {
        private const val SPEED_SMOOTHING_WINDOW = 3
    }

    init {
        Log.d("DriverTripVM", "ViewModel initialized")
        checkInitialPermissions()
    }

    open fun requestPermissionsIfNeeded(onPermissionsGranted: () -> Unit, onPermissionsDenied: () -> Unit) {
        if (uiState.value.locationPermissionsGranted && uiState.value.notificationPermissionGranted) {
            onPermissionsGranted()
        } else {
            onPermissionsDenied()
        }
    }

    fun startObservingLocation(locationFlow: SharedFlow<Location>) {
        Log.d("DriverTripVM", "startObservingLocation called")
        viewModelScope.launch {
            locationFlow.collect { location ->
                Log.d("DriverTripVM", "Collected location in ViewModel: ${location.latitude}, ${location.longitude}")
                onRawLocationUpdate(location)
            }
        }
    }

    fun onRawLocationUpdate(newRawLocation: Location) {
        Log.d("DriverTripVM", "onRawLocationUpdate called with: ${newRawLocation.latitude}, ${newRawLocation.longitude}, accuracy: ${newRawLocation.accuracy}, time: ${newRawLocation.time}, elapsedNanos: ${newRawLocation.elapsedRealtimeNanos}")
        if (uiState.value.tripStatus != TripStatus.IN_PROGRESS) {
            if (uiState.value.tripStatus == TripStatus.PAUSED) {
                Log.d("DriverTripVM", "Trip is PAUSED, updating previousLocationForCalc only.")
                previousLocationForCalc = newRawLocation
            }
            return
        }

        val segment = TripCalculationUtils.calculateSegment(
            newLocation = newRawLocation,
            previousLocation = previousLocationForCalc,
            lastSignificantMoveLocation = lastSignificantMoveLocation,
            timeOfLastSignificantMoveMs = timeOfLastSignificantMoveMs,
            idleThresholdMinutes = uiState.value.idleThresholdMinutes
        )
        Log.d("DriverTripVM", "Segment calculated: $segment. PrevLocTime: ${previousLocationForCalc?.time}, PrevLocElapsedNanos: ${previousLocationForCalc?.elapsedRealtimeNanos}")

        if (!segment.isSpike) {
            tripTrack.add(newRawLocation)

            if (segment.currentSpeedKmh > maxSpeedKmh) {
                maxSpeedKmh = segment.currentSpeedKmh
            }

            val distanceToAdd = if (uiState.value.useKilometers) segment.distanceAddedKm else segment.distanceAddedKm * 0.621371

            val rawSpeedForSmoothing = if (uiState.value.useKilometers) segment.currentSpeedKmh else segment.currentSpeedKmh * 0.621371
            if (speedReadings.size >= SPEED_SMOOTHING_WINDOW) {
                speedReadings.removeAt(0)
            }
            speedReadings.add(rawSpeedForSmoothing)
            val smoothedSpeed = if (speedReadings.isNotEmpty()) speedReadings.average() else 0.0

            _uiState.update {
                val newMovingTime = it.movingTimeSeconds + segment.movingTimeDeltaSeconds
                val newIdleTime = it.idleTimeSeconds + segment.idleTimeDeltaSeconds
                val newTotalTime = newMovingTime + newIdleTime
                Log.d("DriverTripVM", "Updating UIState: DistToAdd: $distanceToAdd, MovTimeDelta: ${segment.movingTimeDeltaSeconds}, IdleTimeDelta: ${segment.idleTimeDeltaSeconds}, NewMovTime: $newMovingTime, NewIdleTime: $newIdleTime, TotalTime: $newTotalTime, SmoothedSpeed: $smoothedSpeed, RawSpeedKmh: ${segment.currentSpeedKmh}")
                it.copy(
                    distance = it.distance + distanceToAdd,
                    movingTimeSeconds = newMovingTime,
                    idleTimeSeconds = newIdleTime,
                    totalTripTimeSeconds = newTotalTime,
                    currentSpeed = smoothedSpeed
                )
            }

            if (segment.movingTimeDeltaSeconds > 0) {
                lastSignificantMoveLocation = newRawLocation
                timeOfLastSignificantMoveMs = System.currentTimeMillis()
            }
            previousLocationForCalc = newRawLocation
        } else {
            Log.d("DriverTripVM", "Spike detected, only updating idle time based on segment calculation.")
            _uiState.update {
                val newIdleTime = it.idleTimeSeconds + segment.idleTimeDeltaSeconds
                it.copy(
                    idleTimeSeconds = newIdleTime,
                    totalTripTimeSeconds = it.movingTimeSeconds + newIdleTime
                )
            }
            previousLocationForCalc = newRawLocation
        }
    }

    open fun onPermissionsResult(locationGranted: Boolean, notificationGranted: Boolean) {
        _uiState.update {
            it.copy(
                locationPermissionsGranted = locationGranted,
                notificationPermissionGranted = notificationGranted
            )
        }
    }

    private fun checkInitialPermissions() {
        val locGranted = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val notifGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        _uiState.update { it.copy(locationPermissionsGranted = locGranted, notificationPermissionGranted = notifGranted) }
    }

    open fun startTrip() {
        Log.d("DriverTripVM", "startTrip called. Permissions: loc=${uiState.value.locationPermissionsGranted}, notif=${uiState.value.notificationPermissionGranted}")
        if (!uiState.value.locationPermissionsGranted || !uiState.value.notificationPermissionGranted) {
            Log.w("DriverTripVM", "Start trip attempted without sufficient permissions.")
            return
        }

        tripTrack.clear()
        previousLocationForCalc = null
        lastSignificantMoveLocation = null
        timeOfLastSignificantMoveMs = System.currentTimeMillis()
        speedReadings.clear()

        tripStartTimeMillis = System.currentTimeMillis()
        maxSpeedKmh = 0.0
        detectedStops.clear()
        currentStopInProgress = null
        _lastTripReport.value = null

        _uiState.update {
            it.copy(
                tripStatus = TripStatus.IN_PROGRESS,
                totalTripTimeSeconds = 0L,
                distance = 0.0,
                movingTimeSeconds = 0L,
                idleTimeSeconds = 0L,
                currentSpeed = 0.0
            )
        }

        val intent = Intent(applicationContext, ForegroundLocationService::class.java).apply {
            action = ForegroundLocationService.ACTION_START_OR_RESUME_SERVICE
            putExtra(ForegroundLocationService.EXTRA_IS_PRECISE_TRACKING, uiState.value.isPreciseTracking)
            putExtra(ForegroundLocationService.EXTRA_TRIP_STATUS, TripStatus.IN_PROGRESS.name)
        }
        ContextCompat.startForegroundService(applicationContext, intent)
    }

    open fun pauseTrip() {
        Log.d("DriverTripVM", "pauseTrip called")
        if (uiState.value.tripStatus != TripStatus.IN_PROGRESS) return

        if (currentStopInProgress == null) {
            currentStopInProgress = StopInProgress(System.currentTimeMillis(), previousLocationForCalc ?: tripTrack.lastOrNull())
        }

        val intent = Intent(applicationContext, ForegroundLocationService::class.java).apply {
            action = ForegroundLocationService.ACTION_PAUSE_SERVICE
        }
        applicationContext.startService(intent)
        _uiState.update { it.copy(tripStatus = TripStatus.PAUSED, currentSpeed = 0.0) }
        speedReadings.clear()
    }

    open fun resumeTrip() {
        Log.d("DriverTripVM", "resumeTrip called")
        if (uiState.value.tripStatus != TripStatus.PAUSED) return

        currentStopInProgress?.let {
            val stopEndTimeMillis = System.currentTimeMillis()
            val stopDurationSeconds = (stopEndTimeMillis - it.startTimeMillis) / 1000
            if (stopDurationSeconds > 0) {
                detectedStops.add(
                    TripStop(
                        latitude = it.startLocation?.latitude ?: 0.0,
                        longitude = it.startLocation?.longitude ?: 0.0,
                        startTimeMillis = it.startTimeMillis,
                        endTimeMillis = stopEndTimeMillis,
                        durationSeconds = stopDurationSeconds
                    )
                )
            }
            currentStopInProgress = null
        }

        timeOfLastSignificantMoveMs = System.currentTimeMillis()

        val intent = Intent(applicationContext, ForegroundLocationService::class.java).apply {
            action = ForegroundLocationService.ACTION_START_OR_RESUME_SERVICE
            putExtra(ForegroundLocationService.EXTRA_IS_PRECISE_TRACKING, uiState.value.isPreciseTracking)
            putExtra(ForegroundLocationService.EXTRA_TRIP_STATUS, TripStatus.IN_PROGRESS.name)
        }
        ContextCompat.startForegroundService(applicationContext, intent)
        _uiState.update { it.copy(tripStatus = TripStatus.IN_PROGRESS) }
    }

    open fun endTrip() {
        Log.d("DriverTripVM", "endTrip called")
        if (uiState.value.tripStatus == TripStatus.COMPLETED) return

        val intent = Intent(applicationContext, ForegroundLocationService::class.java).apply {
            action = ForegroundLocationService.ACTION_STOP_SERVICE
        }
        applicationContext.startService(intent)

        val endTimeMillis = System.currentTimeMillis()

        currentStopInProgress?.let {
            val stopDurationSeconds = (endTimeMillis - it.startTimeMillis) / 1000
            if (stopDurationSeconds > 0) {
                detectedStops.add(
                    TripStop(
                        latitude = it.startLocation?.latitude ?: 0.0,
                        longitude = it.startLocation?.longitude ?: 0.0,
                        startTimeMillis = it.startTimeMillis,
                        endTimeMillis = endTimeMillis,
                        durationSeconds = stopDurationSeconds
                    )
                )
            }
            currentStopInProgress = null
        }

        val currentLocalUiState = uiState.value
        val finalTotalDistanceKm = if (currentLocalUiState.useKilometers) currentLocalUiState.distance else currentLocalUiState.distance * 1.60934
        val finalAverageSpeedKmh = if (currentLocalUiState.movingTimeSeconds > 0) {
            (finalTotalDistanceKm / (currentLocalUiState.movingTimeSeconds / 3600.0))
        } else {
            0.0
        }
        val finalTripTrackCoordinates = tripTrack.map { Pair(it.latitude, it.longitude) }

        val report = TripReport(
            startTimeMillis = tripStartTimeMillis,
            endTimeMillis = endTimeMillis,
            totalDistanceKm = finalTotalDistanceKm,
            totalTimeInMotionSeconds = currentLocalUiState.movingTimeSeconds,
            totalTimeIdleSeconds = currentLocalUiState.idleTimeSeconds,
            averageSpeedKmh = finalAverageSpeedKmh,
            maxSpeedKmh = this.maxSpeedKmh,
            stops = detectedStops.toList(),
            tripTrackCoordinates = finalTripTrackCoordinates,
        )
        _lastTripReport.value = report


        _uiState.update {
            it.copy(
                tripStatus = TripStatus.COMPLETED,
                currentSpeed = 0.0
            )
        }
        tripTrack.clear()
        detectedStops.clear()
        previousLocationForCalc = null
        lastSignificantMoveLocation = null
        timeOfLastSignificantMoveMs = 0L
        tripStartTimeMillis = 0L
        maxSpeedKmh = 0.0
        speedReadings.clear()
    }

    fun getTripReportAsCsv(): String? {
        return _lastTripReport.value?.let { report ->
            val header = "ID,StartTime,EndTime,DistanceKm,TimeMotionSec,TimeIdleSec,AvgSpeedKmh,MaxSpeedKmh,StopsCount,TrackPointsCount\n"
            val data = "${report.id},${report.startTimeMillis},${report.endTimeMillis},${report.totalDistanceKm}," +
                    "${report.totalTimeInMotionSeconds},${report.totalTimeIdleSeconds},${report.averageSpeedKmh},"+
                    "${report.maxSpeedKmh},${report.stops.size},${report.tripTrackCoordinates.size}\n"
            header + data
        }
    }

    fun getTripReportAsJson(): String? {
        return _lastTripReport.value?.let { report ->
            "{\n" +
                    "  \"id\": \"${report.id}\",\n" +
                    "  \"startTimeMillis\": ${report.startTimeMillis},\n" +
                    "  \"endTimeMillis\": ${report.endTimeMillis},\n" +
                    "  \"totalDistanceKm\": ${report.totalDistanceKm},\n" +
                    "  \"totalTimeInMotionSeconds\": ${report.totalTimeInMotionSeconds},\n" +
                    "  \"totalTimeIdleSeconds\": ${report.totalTimeIdleSeconds},\n" +
                    "  \"averageSpeedKmh\": ${report.averageSpeedKmh},\n" +
                    "  \"maxSpeedKmh\": ${report.maxSpeedKmh},\n" +
                    "  \"stopsCount\": ${report.stops.size},\n" +
                    "  \"tripTrackPointsCount\": ${report.tripTrackCoordinates.size}\n" +
                    "}"
        }
    }

    fun logManualStop() {
    }
    
    /**
     * Получить текущий трек для экспорта
     */
    fun getCurrentTrack(): List<Location> {
        return tripTrack.toList()
    }
    
    /**
     * Экспорт трека в файл
     */
    fun exportTrack(format: String, file: java.io.File): Boolean {
        val exporter = TrackExporter()
        return when (format.lowercase()) {
            "gpx" -> exporter.exportToGpx(tripTrack, "Trip Track", file)
            "kml" -> exporter.exportToKml(tripTrack, "Trip Track", file)
            "csv" -> exporter.exportToCsv(tripTrack, file)
            else -> false
        }
    }

    fun setPreciseTracking(isPrecise: Boolean) {
        _uiState.update { it.copy(isPreciseTracking = isPrecise) }
        if (uiState.value.tripStatus == TripStatus.IN_PROGRESS || uiState.value.tripStatus == TripStatus.PAUSED) {
            val intent = Intent(applicationContext, ForegroundLocationService::class.java).apply {
                action = ForegroundLocationService.ACTION_UPDATE_SETTINGS
                putExtra(ForegroundLocationService.EXTRA_IS_PRECISE_TRACKING, isPrecise)
            }
            applicationContext.startService(intent)
        }
    }

    fun setIdleThreshold(minutes: Int) {
        _uiState.update { it.copy(idleThresholdMinutes = minutes) }
    }

    fun setUseKilometers(useKm: Boolean) {
        _uiState.update { currentState ->
            val newDistance = if (useKm) {
                if (!currentState.useKilometers) currentState.distance * 1.60934
                else currentState.distance
            } else {
                if (currentState.useKilometers) currentState.distance * 0.621371
                else currentState.distance
            }
            val newSpeed = if (useKm) {
                if (!currentState.useKilometers) currentState.currentSpeed * 1.60934
                else currentState.currentSpeed
            } else {
                if (currentState.useKilometers) currentState.currentSpeed * 0.621371
                else currentState.currentSpeed
            }
            currentState.copy(useKilometers = useKm, distance = newDistance, currentSpeed = newSpeed)
        }
    }

    class DriverTripViewModelFactory(private val applicationContext: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DriverTripViewModel::class.java)) {
                return DriverTripViewModel(applicationContext) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

class DriverTripActivity : ComponentActivity() {

    private val viewModel: DriverTripViewModel by viewModels {
        DriverTripViewModel.DriverTripViewModelFactory(applicationContext)
    }
    private var showPermissionRationaleDialog by mutableStateOf(false)
    private var showSettingsRedirectDialog by mutableStateOf(false)
    private var permissionDeniedMessage by mutableStateOf("")

    private var foregroundLocationService: ForegroundLocationService? = null
    private var isServiceBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d("DriverTripActivity", "Service connected")
            val binder = service as ForegroundLocationService.LocalBinder
            foregroundLocationService = binder.getService()
            isServiceBound = true
            foregroundLocationService?.locationUpdates?.let {
                Log.d("DriverTripActivity", "Passing locationUpdates Flow to ViewModel")
                viewModel.startObservingLocation(it)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d("DriverTripActivity", "Service disconnected")
            foregroundLocationService = null
            isServiceBound = false
        }
    }

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val postNotificationsGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions[Manifest.permission.POST_NOTIFICATIONS] ?: false
            } else {
                true
            }

            viewModel.onPermissionsResult(fineLocationGranted, postNotificationsGranted)

            if (!fineLocationGranted) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    permissionDeniedMessage = getString(R.string.permission_location_denied_permanently_message)
                    showSettingsRedirectDialog = true
                } else {
                    permissionDeniedMessage = getString(R.string.permission_location_rationale_message)
                    showPermissionRationaleDialog = true
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !postNotificationsGranted) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    permissionDeniedMessage = getString(R.string.permission_notification_denied_permanently_message)
                    showSettingsRedirectDialog = true
                } else {
                    permissionDeniedMessage = getString(R.string.permission_notification_rationale_message)
                    showPermissionRationaleDialog = true
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("DriverTripActivity", "onCreate")
        setContent {
            FinLogCalcTheme {
                val navController = rememberNavController()
                TripCalculatorNavHost(
                    navController = navController,
                    requestPermissions = ::requestRequiredPermissions,
                    viewModel = viewModel
                )

                if (showPermissionRationaleDialog) {
                    PermissionRationaleOrRedirectDialog(
                        title = stringResource(id = R.string.permission_required_title),
                        message = permissionDeniedMessage,
                        confirmButtonText = stringResource(id = R.string.dialog_button_ok),
                        onConfirm = {
                            showPermissionRationaleDialog = false
                            requestRequiredPermissions()
                        },
                        onDismiss = { showPermissionRationaleDialog = false }
                    )
                }

                if (showSettingsRedirectDialog) {
                    PermissionRationaleOrRedirectDialog(
                        title = stringResource(id = R.string.permission_required_title),
                        message = permissionDeniedMessage,
                        confirmButtonText = stringResource(id = R.string.dialog_button_open_settings),
                        onConfirm = {
                            showSettingsRedirectDialog = false
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also {
                                val uri = Uri.fromParts("package", packageName, null)
                                it.data = uri
                                startActivity(it)
                            }
                        },
                        onDismiss = { showSettingsRedirectDialog = false }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("DriverTripActivity", "onStart - Binding to service")
        Intent(this, ForegroundLocationService::class.java).also { intent ->
            bindService(intent, serviceConnection, BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d("DriverTripActivity", "onStop - Unbinding from service")
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
            foregroundLocationService = null
        }
    }

    private fun requestRequiredPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        requestPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverTripScreenContent(
    viewModel: DriverTripViewModel,
    uiState: DriverTripUiState,
    onNavigateBack: () -> Unit,
    onStartTripClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.driver_trip_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.common_back))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TripStatusSection(status = uiState.tripStatus)
            TripCountersSection(uiState = uiState)
            TripControlsSection(
                tripStatus = uiState.tripStatus,
                permissionsGranted = uiState.locationPermissionsGranted && uiState.notificationPermissionGranted,
                onStart = onStartTripClick,
                onPause = viewModel::pauseTrip,
                onResume = viewModel::resumeTrip,
                onEnd = viewModel::endTrip,
                onLogStop = viewModel::logManualStop
            )
            TripSettingsSection(
                uiState = uiState,
                onTrackingModeChange = viewModel::setPreciseTracking,
                onIdleThresholdChange = viewModel::setIdleThreshold,
                onUnitsChange = viewModel::setUseKilometers
            )
        }
    }
}

@Composable
fun PermissionRationaleOrRedirectDialog(
    title: String,
    message: String,
    confirmButtonText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(id = R.string.common_cancel))
            }
        }
    )
}


@Composable
fun TripStatusSection(status: TripStatus) {
    val statusText = when (status) {
        TripStatus.NOT_STARTED -> stringResource(R.string.trip_status_not_started)
        TripStatus.IN_PROGRESS -> stringResource(R.string.trip_status_in_progress)
        TripStatus.PAUSED -> stringResource(R.string.trip_status_paused)
        TripStatus.COMPLETED -> stringResource(R.string.trip_status_completed)
    }
    Text(
        text = "${stringResource(R.string.trip_status_label)}: $statusText",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun TripCountersSection(uiState: DriverTripUiState) {
    val distanceUnit = if (uiState.useKilometers) stringResource(R.string.unit_km_short) else stringResource(R.string.unit_miles_short)
    val speedUnit = if (uiState.useKilometers) stringResource(R.string.unit_km_hr) else stringResource(R.string.unit_miles_hr)

    fun formatTime(totalSeconds: Long): String {
        val hours = TimeUnit.SECONDS.toHours(totalSeconds)
        val minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) % 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }

    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
        CounterRow(label = stringResource(R.string.trip_time_label), value = formatTime(uiState.totalTripTimeSeconds))
        CounterRow(label = stringResource(R.string.distance_label), value = "${"%.1f".format(uiState.distance)} $distanceUnit")
        CounterRow(label = stringResource(R.string.moving_time_label), value = formatTime(uiState.movingTimeSeconds))
        CounterRow(label = stringResource(R.string.idle_time_label), value = formatTime(uiState.idleTimeSeconds))
        CounterRow(label = stringResource(R.string.current_speed_label), value = "${"%.1f".format(uiState.currentSpeed)} $speedUnit")
    }
}

@Composable
fun CounterRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun TripControlsSection(
    tripStatus: TripStatus,
    permissionsGranted: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onEnd: () -> Unit,
    onLogStop: () -> Unit
) {
    val startButtonEnabled = permissionsGranted && tripStatus == TripStatus.NOT_STARTED
    val pauseButtonEnabled = permissionsGranted && tripStatus == TripStatus.IN_PROGRESS
    val resumeButtonEnabled = permissionsGranted && tripStatus == TripStatus.PAUSED
    val endLogStopEnabled = permissionsGranted && (tripStatus == TripStatus.IN_PROGRESS || tripStatus == TripStatus.PAUSED)

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (tripStatus == TripStatus.NOT_STARTED) {
            Button(onClick = onStart, modifier = Modifier.fillMaxWidth(), enabled = startButtonEnabled) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.button_start_trip))
            }
        }

        if (tripStatus == TripStatus.IN_PROGRESS) {
            Button(onClick = onPause, modifier = Modifier.fillMaxWidth(), enabled = pauseButtonEnabled) {
                Icon(Icons.Filled.Pause, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.button_pause_trip))
            }
        }

        if (tripStatus == TripStatus.PAUSED) {
            Button(onClick = onResume, modifier = Modifier.fillMaxWidth(), enabled = resumeButtonEnabled) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.button_resume_trip))
            }
        }

        if (tripStatus == TripStatus.IN_PROGRESS || tripStatus == TripStatus.PAUSED) {
            Button(onClick = onEnd, modifier = Modifier.fillMaxWidth(), enabled = endLogStopEnabled) {
                Icon(Icons.Filled.Stop, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.button_end_trip))
            }
            Button(onClick = onLogStop, modifier = Modifier.fillMaxWidth(), enabled = endLogStopEnabled && tripStatus == TripStatus.IN_PROGRESS) {
                Icon(Icons.Filled.PanTool, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.button_log_stop))
            }
        }
    }
}

@Composable
fun TripSettingsSection(
    uiState: DriverTripUiState,
    onTrackingModeChange: (Boolean) -> Unit,
    onIdleThresholdChange: (Int) -> Unit,
    onUnitsChange: (Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

        SettingSwitchRow(
            label = stringResource(R.string.setting_tracking_mode_label),
            checked = uiState.isPreciseTracking,
            onCheckedChange = onTrackingModeChange,
            checkedText = stringResource(R.string.tracking_mode_precise),
            uncheckedText = stringResource(R.string.tracking_mode_power_saving)
        )

        SettingSwitchRow(
            label = stringResource(R.string.setting_units_label),
            checked = uiState.useKilometers,
            onCheckedChange = onUnitsChange,
            checkedText = stringResource(R.string.unit_km),
            uncheckedText = stringResource(R.string.unit_miles)
        )

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.setting_idle_threshold_label, uiState.idleThresholdMinutes))
            }
            Slider(
                value = uiState.idleThresholdMinutes.toFloat(),
                onValueChange = { onIdleThresholdChange(it.toInt()) },
                valueRange = 1f..60f,
                steps = 58,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun SettingSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    checkedText: String,
    uncheckedText: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(if (checked) checkedText else uncheckedText, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.width(8.dp))
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}


@Preview(showBackground = true, name = "Driver Trip Screen - Not Started")
@Composable
fun PreviewDriverTripScreenNotStarted() {
    FinLogCalcTheme {
        val context = LocalContext.current
        val viewModel: DriverTripViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
            factory = DriverTripViewModel.DriverTripViewModelFactory(context.applicationContext)
        )
        val uiState by viewModel.uiState.collectAsState()

        DriverTripScreenContent(
            viewModel = viewModel,
            uiState = uiState,
            onNavigateBack = {},
            onStartTripClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Driver Trip Screen - In Progress")
@Composable
fun PreviewDriverTripScreenInProgress() {
    FinLogCalcTheme {
        val context = LocalContext.current
        val viewModel: DriverTripViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
            factory = DriverTripViewModel.DriverTripViewModelFactory(context.applicationContext)
        )
        val previewUiState = DriverTripUiState(
            tripStatus = TripStatus.IN_PROGRESS,
            totalTripTimeSeconds = 3665,
            distance = 12.3,
            movingTimeSeconds = 3000,
            idleTimeSeconds = 665,
            currentSpeed = 55.0,
            locationPermissionsGranted = true,
            notificationPermissionGranted = true
        )

        DriverTripScreenContent(
            viewModel = viewModel,
            uiState = previewUiState,
            onNavigateBack = {},
            onStartTripClick = { viewModel.startTrip() }
        )
    }
}

@Preview(showBackground = true, name = "Driver Trip Screen - Paused")
@Composable
fun PreviewDriverTripScreenPaused() {
    FinLogCalcTheme {
        val context = LocalContext.current
        val viewModel: DriverTripViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
            factory = DriverTripViewModel.DriverTripViewModelFactory(context.applicationContext)
        )
        val previewUiState = DriverTripUiState(
            tripStatus = TripStatus.PAUSED,
            totalTripTimeSeconds = 7200,
            distance = 25.5,
            movingTimeSeconds = 6000,
            idleTimeSeconds = 1200,
            currentSpeed = 0.0,
            useKilometers = false,
            idleThresholdMinutes = 15,
            locationPermissionsGranted = true,
            notificationPermissionGranted = true
        )

        DriverTripScreenContent(
            viewModel = viewModel,
            uiState = previewUiState,
            onNavigateBack = {},
            onStartTripClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Permission Rationale Dialog")
@Composable
fun PreviewPermissionRationaleDialog() {
    FinLogCalcTheme {
        PermissionRationaleOrRedirectDialog(
            title = "Permission Required",
            message = "This app needs location access to track your trip. Please grant the permission.",
            confirmButtonText = "OK",
            onConfirm = {  },
            onDismiss = {  }
        )
    }
}

@Preview(showBackground = true, name = "Settings Redirect Dialog")
@Composable
fun PreviewSettingsRedirectDialog() {
    FinLogCalcTheme {
        PermissionRationaleOrRedirectDialog(
            title = "Permission Required",
            message = "Location permission was permanently denied. Please enable it in app settings to use this feature.",
            confirmButtonText = "Open Settings",
            onConfirm = {  },
            onDismiss = {  }
        )
    }
}
