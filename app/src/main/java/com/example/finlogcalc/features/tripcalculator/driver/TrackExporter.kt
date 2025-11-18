package com.example.finlogcalc.features.tripcalculator.driver

import android.location.Location
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Экспорт GPS-трека в форматы GPX и KML
 */
class TrackExporter {
    
    /**
     * Экспорт трека в формат GPX
     */
    fun exportToGpx(
        trackPoints: List<Location>,
        trackName: String = "Trip Track",
        outputFile: File
    ): Boolean {
        return try {
            FileWriter(outputFile).use { writer ->
                writer.appendln("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                writer.appendln("<gpx version=\"1.1\" creator=\"FinLogCalc\">")
                writer.appendln("  <trk>")
                writer.appendln("    <name>$trackName</name>")
                writer.appendln("    <trkseg>")
                
                trackPoints.forEach { location ->
                    val time = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }.format(Date(location.time))
                    
                    writer.appendln("      <trkpt lat=\"${location.latitude}\" lon=\"${location.longitude}\">")
                    writer.appendln("        <ele>${location.altitude}</ele>")
                    writer.appendln("        <time>$time</time>")
                    if (location.hasSpeed()) {
                        writer.appendln("        <extensions>")
                        writer.appendln("          <speed>${location.speed * 3.6}</speed>") // м/с в км/ч
                        writer.appendln("        </extensions>")
                    }
                    writer.appendln("      </trkpt>")
                }
                
                writer.appendln("    </trkseg>")
                writer.appendln("  </trk>")
                writer.appendln("</gpx>")
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Экспорт трека в формат KML
     */
    fun exportToKml(
        trackPoints: List<Location>,
        trackName: String = "Trip Track",
        outputFile: File
    ): Boolean {
        return try {
            FileWriter(outputFile).use { writer ->
                writer.appendln("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                writer.appendln("<kml xmlns=\"http://www.opengis.net/kml/2.2\">")
                writer.appendln("  <Document>")
                writer.appendln("    <name>$trackName</name>")
                writer.appendln("    <Placemark>")
                writer.appendln("      <name>$trackName</name>")
                writer.appendln("      <LineString>")
                writer.appendln("        <tessellate>1</tessellate>")
                writer.appendln("        <coordinates>")
                
                trackPoints.forEach { location ->
                    writer.append("${location.longitude},${location.latitude},${location.altitude} ")
                }
                
                writer.appendln()
                writer.appendln("        </coordinates>")
                writer.appendln("      </LineString>")
                writer.appendln("    </Placemark>")
                writer.appendln("  </Document>")
                writer.appendln("</kml>")
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Экспорт трека в CSV
     */
    fun exportToCsv(
        trackPoints: List<Location>,
        outputFile: File
    ): Boolean {
        return try {
            FileWriter(outputFile).use { writer ->
                writer.appendln("Latitude,Longitude,Altitude,Speed,Time,Accuracy")
                
                trackPoints.forEach { location ->
                    val time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(location.time))
                    writer.appendln(
                        "${location.latitude}," +
                        "${location.longitude}," +
                        "${location.altitude}," +
                        "${if (location.hasSpeed()) location.speed * 3.6 else 0}," +
                        "$time," +
                        "${location.accuracy}"
                    )
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

