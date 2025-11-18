package com.example.finlogcalc.features.tripcalculator.driver

import java.util.UUID

/**
 * Represents a detected stop during a trip.
 */
data class TripStop(
    val latitude: Double,
    val longitude: Double,
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val durationSeconds: Long
)

/**
 * Comprehensive report for a completed trip.
 */
data class TripReport(
    val id: String = UUID.randomUUID().toString(), // Unique ID for the report
    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val totalDistanceKm: Double,
    val totalTimeInMotionSeconds: Long,
    val totalTimeIdleSeconds: Long,
    val averageSpeedKmh: Double,      // Average speed during motion
    val maxSpeedKmh: Double,
    val stops: List<TripStop>,
    val tripTrackCoordinates: List<Pair<Double, Double>>, // Simplified lat/lon for the polyline
    val fuelEstimateLiters: Double? = null, // Optional
    val comments: String? = null,           // Optional
    val photoUris: List<String>? = null     // Optional, list of content URIs
)
