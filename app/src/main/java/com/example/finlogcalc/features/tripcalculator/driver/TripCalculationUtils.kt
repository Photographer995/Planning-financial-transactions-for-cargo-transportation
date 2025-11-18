package com.example.finlogcalc.features.tripcalculator.driver

import android.location.Location
import java.util.concurrent.TimeUnit

// Constants for trip calculation logic
private const val SPIKE_SPEED_THRESHOLD_MPS = 70.0 // Approx 250 km/h, adjust as needed
private const val IDLE_SPEED_THRESHOLD_MPS = 0.8 // Approx 3 km/h, adjust as needed
private const val MIN_SIGNIFICANT_MOVE_METERS = 10.0 // Min distance to be considered a significant move from idle

data class ProcessedLocationSegment(
    val distanceAddedKm: Double,
    val movingTimeDeltaSeconds: Long,
    val idleTimeDeltaSeconds: Long,
    val currentSpeedKmh: Double,
    val isSpike: Boolean,
    val newLocationTime: Long // For updating previous location time
)

object TripCalculationUtils {

    fun calculateSegment(
        newLocation: Location,
        previousLocation: Location?,
        lastSignificantMoveLocation: Location?, // Last location where user was confirmed moving
        timeOfLastSignificantMoveMs: Long,     // System time when user was last confirmed moving
        idleThresholdMinutes: Int
    ): ProcessedLocationSegment {
        if (previousLocation == null) {
            // First point of the trip
            return ProcessedLocationSegment(
                distanceAddedKm = 0.0,
                movingTimeDeltaSeconds = 0L,
                idleTimeDeltaSeconds = 0L,
                currentSpeedKmh = 0.0,
                isSpike = false,
                newLocationTime = newLocation.time
            )
        }

        val segmentDurationNanos = newLocation.elapsedRealtimeNanos - previousLocation.elapsedRealtimeNanos
        val segmentDurationSeconds = TimeUnit.NANOSECONDS.toMillis(segmentDurationNanos) / 1000.0
        
        if (segmentDurationSeconds <= 0.1) { // Ignore very short or invalid intervals
            return ProcessedLocationSegment(0.0, 0L, 0L, 0.0, isSpike = false, newLocation.time)
        }

        val segmentDistanceMeters = previousLocation.distanceTo(newLocation).toDouble()
        val currentSpeedMps = segmentDistanceMeters / segmentDurationSeconds
        val currentSpeedKmh = currentSpeedMps * 3.6

        // 1. Spike Detection
        if (currentSpeedMps > SPIKE_SPEED_THRESHOLD_MPS) {
            return ProcessedLocationSegment(
                distanceAddedKm = 0.0, // Don't add distance for spikes
                movingTimeDeltaSeconds = 0L,
                idleTimeDeltaSeconds = segmentDurationSeconds.toLong(), // Attribute spike time to idle
                currentSpeedKmh = 0.0, // Or previous valid speed
                isSpike = true,
                newLocationTime = newLocation.time
            )
        }

        // 2. Idle vs. Moving Time
        var movingTimeDelta: Long = 0L
        var idleTimeDelta: Long = 0L

        val isSpeedConsideredIdle = currentSpeedMps < IDLE_SPEED_THRESHOLD_MPS
        
        // Check significant movement since last confirmed "moving" point
        val displacementSinceLastMoveMeters = lastSignificantMoveLocation?.distanceTo(newLocation)?.toDouble() ?: segmentDistanceMeters

        if (isSpeedConsideredIdle) {
            if (displacementSinceLastMoveMeters < MIN_SIGNIFICANT_MOVE_METERS) {
                 // Speed is low AND no significant displacement since last known movement
                 // Check if idle duration threshold is met
                val systemTimeNowMs = System.currentTimeMillis() // Using system time for idle duration check against threshold
                val timeSinceLastSignificantMoveMs = systemTimeNowMs - timeOfLastSignificantMoveMs
                if (timeSinceLastSignificantMoveMs >= TimeUnit.MINUTES.toMillis(idleThresholdMinutes.toLong())) {
                    idleTimeDelta = segmentDurationSeconds.toLong()
                } else {
                    // Still within idle grace period, or a very short stop. Count as moving for now.
                    // Or, could attribute to a brief stop if more detailed states are needed.
                    movingTimeDelta = segmentDurationSeconds.toLong()
                }
            } else {
                // Speed is low, but there was significant displacement (e.g. slow crawling)
                movingTimeDelta = segmentDurationSeconds.toLong()
            }
        } else {
            // Speed indicates movement
            movingTimeDelta = segmentDurationSeconds.toLong()
        }
        
        return ProcessedLocationSegment(
            distanceAddedKm = segmentDistanceMeters / 1000.0,
            movingTimeDeltaSeconds = movingTimeDelta,
            idleTimeDeltaSeconds = idleTimeDelta,
            currentSpeedKmh = currentSpeedKmh,
            isSpike = false,
            newLocationTime = newLocation.time
        )
    }
}
