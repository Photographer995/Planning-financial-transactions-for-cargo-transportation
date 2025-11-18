package com.example.finlogcalc.features.tripcalculator.driver

import android.location.Location
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File

/**
 * Диалог для экспорта трека
 */
@Composable
fun ExportTrackDialog(
    onDismiss: () -> Unit,
    onExport: (String, File) -> Unit,
    trackPoints: List<Location>
) {
    var selectedFormat by remember { mutableStateOf("gpx") }
    var isExporting by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Экспорт трека") },
        text = {
            Column {
                Text("Выберите формат экспорта:")
                Spacer(modifier = Modifier.height(16.dp))
                
                RadioButtonGroup(
                    options = listOf("GPX", "KML", "CSV"),
                    selectedOption = selectedFormat.uppercase(),
                    onOptionSelected = { selectedFormat = it.lowercase() }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                Text("Точек в треке: ${trackPoints.size}")
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isExporting = true
                    // TODO: Выбрать файл через FilePicker
                    // onExport(selectedFormat, file)
                },
                enabled = !isExporting
            ) {
                Text(if (isExporting) "Экспорт..." else "Экспорт")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}

@Composable
fun RadioButtonGroup(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Column {
        options.forEach { option ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedOption == option,
                    onClick = { onOptionSelected(option) }
                )
                Text(
                    text = option,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

