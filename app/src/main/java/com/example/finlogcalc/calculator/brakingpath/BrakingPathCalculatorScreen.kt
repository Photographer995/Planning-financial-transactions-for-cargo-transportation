package com.example.finlogcalc.calculator.brakingpath

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.util.Locale
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// Гравитация (м/с^2)
private const val G = 9.81

// Предустановленные дорожные условия и типичные коэффициенты сцепления (примерные)
private val ROAD_CONDITIONS = listOf(
    "Асфальт (сухой) — μ ≈ 0.8" to 0.8,
    "Асфальт (мокрый) — μ ≈ 0.5" to 0.5,
    "Грязь/снег — μ ≈ 0.25" to 0.25,
    "Гладкий лёд — μ ≈ 0.1" to 0.1,
    "Ввести вручную..." to null
)

private enum class CalculatorMode {
    NONE,
    SPEED_FROM_SKID_MARKS,
    STOPPING_DISTANCE
}

private enum class SpeedUnit { KMH, MS }

/**
 * Утилиты
 */
private fun parseDoubleLenient(s: String): Double? {
    val cleaned = s.trim().replace(',', '.')
    return try {
        if (cleaned.isEmpty()) null else cleaned.toDouble()
    } catch (e: Exception) {
        null
    }
}

private fun format2(v: Double): String = String.format(Locale.getDefault(), "%.2f", v)
private fun formatNullable(v: Double?): String = v?.let { format2(it) } ?: "-"


/**
 * Экраны / UI
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrakingPathCalculatorScreen() {
    val context = LocalContext.current
    var selectedMode by rememberSaveable { mutableStateOf(CalculatorMode.NONE) }

    val topBarTitle = when (selectedMode) {
        CalculatorMode.NONE -> "Выберите калькулятор"
        CalculatorMode.SPEED_FROM_SKID_MARKS -> "Определение скорости по тормозному пути"
        CalculatorMode.STOPPING_DISTANCE -> "Калькулятор остановочного пути"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(topBarTitle) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                navigationIcon = {
                    if (selectedMode != CalculatorMode.NONE) {
                        IconButton(onClick = { selectedMode = CalculatorMode.NONE }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Назад"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        val commonModifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()) // Добавим скролл для всего контента под TopAppBar

        when (selectedMode) {
            CalculatorMode.NONE -> {
                Column(
                    modifier = Modifier // Используем fillMaxSize от родителя, но позволяем commonModifier управлять padding
                        .fillMaxSize()
                        .padding(paddingValues) // Только padding от Scaffold
                        .padding(16.dp), // Внешний padding для SelectionScreen
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    SelectionScreen(
                        onSelectSpeedFromSkid = { selectedMode = CalculatorMode.SPEED_FROM_SKID_MARKS },
                        onSelectStoppingDistance = { selectedMode = CalculatorMode.STOPPING_DISTANCE }
                    )
                }
            }
            CalculatorMode.SPEED_FROM_SKID_MARKS -> {
                Column(
                    modifier = commonModifier, // Этот Column уже имеет verticalScroll
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    SpeedFromSkidMarksSection(context)
                    GeneralNote()
                }
            }
            CalculatorMode.STOPPING_DISTANCE -> {
                Column(
                    modifier = commonModifier, // Этот Column уже имеет verticalScroll
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    StoppingDistanceSection(context)
                    GeneralNote()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // For Card onClick
@Composable
private fun SelectionScreen(
    onSelectSpeedFromSkid: () -> Unit,
    onSelectStoppingDistance: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Card(
            onClick = onSelectSpeedFromSkid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Speed,
                    contentDescription = "Определение скорости по тормозному пути",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Определение скорости по тормозному пути",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }

        Card(
            onClick = onSelectStoppingDistance,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Calculate,
                    contentDescription = "Калькулятор остановочного пути",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Калькулятор остановочного пути",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}


@Composable
private fun SpeedResultDialog(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    resultSpeedKmh: Double?,
    skidLength: String,
    slopePercent: String,
    isUphill: Boolean,
    roadConditionText: String,
    manualMu: String?, 
    context: Context
) {
    if (showDialog && resultSpeedKmh != null) {
        val slopeDirStr = if (slopePercent.trim() == "0" || slopePercent.toDoubleOrNull() == 0.0) "" else if (isUphill) " (вверх)" else " (вниз)"
        val muValueDisplay = ROAD_CONDITIONS.find { it.first == roadConditionText }?.second?.toString() ?: manualMu ?: "Не указан"
        
        val shareText = buildString {
            append("Оценочная скорость по тормозному следу: ${format2(resultSpeedKmh)} км/ч\n")
            append("Входные данные:\n")
            append("  Длина тормозного следа: $skidLength м\n")
            append("  Уклон дороги: $slopePercent%$slopeDirStr\n")
            append("  Состояние дороги: $roadConditionText (μ ≈ $muValueDisplay)\n")
            if (ROAD_CONDITIONS.find { it.first == roadConditionText }?.second == null && manualMu != null && manualMu.isNotBlank()) {
                 append("  Коэффициент трения μ (вручную): $manualMu\n")
            }
            append("Формула: v = sqrt(s·2·g·(μ·cos(α) ± sin(α)))")
        }

        AlertDialog(
            onDismissRequest = onDismissRequest,
            icon = { Icon(Icons.Filled.Speed, contentDescription = "Результат расчета скорости") },
            title = { Text("Результат: Скорость по следу") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Входные данные:", style = MaterialTheme.typography.titleSmall)
                    Text("  Длина следа: $skidLength м", style = MaterialTheme.typography.bodyMedium)
                    Text("  Уклон: $slopePercent %$slopeDirStr", style = MaterialTheme.typography.bodyMedium)
                    Text("  Состояние: $roadConditionText (μ ≈ $muValueDisplay)", style = MaterialTheme.typography.bodyMedium)
                    if (ROAD_CONDITIONS.find { it.first == roadConditionText }?.second == null && manualMu != null && manualMu.isNotBlank()) {
                        Text("  Коэфф. трения μ (вручную): $manualMu", style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Расчетная скорость:", style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = "${format2(resultSpeedKmh)} км/ч",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Формула: v = sqrt(s·2·g·(μ·cos(α) ± sin(α)))",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        val chooser = Intent.createChooser(intent, "Поделиться результатом")
                        try {
                            context.startActivity(chooser)
                        } catch (e: Exception) {
                            // Игнорируем ошибки
                        }
                        onDismissRequest()
                    }
                ) {
                    Text("Поделиться")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text("Закрыть")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpeedFromSkidMarksSection(
    context: Context
) {
    var skidLengthText by rememberSaveable { mutableStateOf("") }
    var chosenConditionIndex by rememberSaveable { mutableStateOf(0) }
    var manualMuText by rememberSaveable { mutableStateOf("") }
    var slopePercentTextSkid by rememberSaveable { mutableStateOf("0") }
    var isUphillSkid by rememberSaveable { mutableStateOf(true) }
    var lastSpeedKmh by remember { mutableStateOf<Double?>(null) }
    var error1 by remember { mutableStateOf<String?>(null) }
    var expandedSkidMenu by remember { mutableStateOf(false) }
    var showSpeedResultDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            OutlinedTextField(
                value = skidLengthText,
                onValueChange = { skidLengthText = it; error1 = null; lastSpeedKmh = null },
                label = { Text("Длина тормозного следа, м") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = error1?.contains("длину") == true
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = slopePercentTextSkid,
                onValueChange = { slopePercentTextSkid = it; error1 = null; lastSpeedKmh = null },
                label = { Text("Уклон дороги, % (0 для ровной)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = error1?.contains("уклон") == true
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                TextButton(onClick = { isUphillSkid = true }) {
                    Text("Движение вверх", color = if (isUphillSkid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                TextButton(onClick = { isUphillSkid = false }) {
                    Text("Движение вниз", color = if (!isUphillSkid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text("Состояние дороги:", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom=4.dp))
            ExposedDropdownMenuBox(
                expanded = expandedSkidMenu,
                onExpandedChange = { expandedSkidMenu = !expandedSkidMenu },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = ROAD_CONDITIONS[chosenConditionIndex].first,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Выберите условие") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedSkidMenu) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    isError = error1?.contains("μ") == true && ROAD_CONDITIONS[chosenConditionIndex].second != null
                )
                ExposedDropdownMenu(
                    expanded = expandedSkidMenu,
                    onDismissRequest = { expandedSkidMenu = false }
                ) {
                    ROAD_CONDITIONS.forEachIndexed { index, condition ->
                        DropdownMenuItem(
                            text = { Text(condition.first) },
                            onClick = {
                                chosenConditionIndex = index
                                expandedSkidMenu = false
                                error1 = null
                                lastSpeedKmh = null
                                if (condition.second != null) {
                                    manualMuText = ""
                                }
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))

            if (ROAD_CONDITIONS[chosenConditionIndex].second == null) {
                OutlinedTextField(
                    value = manualMuText,
                    onValueChange = { manualMuText = it; error1 = null; lastSpeedKmh = null },
                    label = { Text("ИЛИ Коэффициент трения μ (например 0.7)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = error1?.contains("μ") == true
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = {
                    error1 = null
                    val s = parseDoubleLenient(skidLengthText)
                    val slopePercent = parseDoubleLenient(slopePercentTextSkid)
                    val muFromList = ROAD_CONDITIONS[chosenConditionIndex].second
                    val muManual = parseDoubleLenient(manualMuText)
                    val mu = muFromList ?: muManual

                    if (s == null || s <= 0.0) {
                        error1 = "Введите корректную длину (> 0)"
                        return@Button
                    }
                    if (mu == null || mu <= 0.0) {
                        error1 = "Введите корректный коэффициент трения μ (> 0)"
                        return@Button
                    }
                    if (slopePercent == null) {
                        error1 = "Введите корректный уклон (число)"
                        return@Button
                    }
                    val alpha = atan(slopePercent / 100.0)
                    val gFactor = mu * cos(alpha) + (if (isUphillSkid) sin(alpha) else -sin(alpha))

                    if (gFactor <= 0) {
                        error1 = "Расчет невозможен (слишком крутой спуск/низкое трение)"
                        lastSpeedKmh = null
                        return@Button
                    }
                    val vms = sqrt(s * 2.0 * G * gFactor)
                    val vkmh = vms * 3.6
                    lastSpeedKmh = vkmh
                    showSpeedResultDialog = true
                }) {
                    Text("Рассчитать скорость")
                }
                Button(onClick = {
                    skidLengthText = ""
                    manualMuText = ""
                    slopePercentTextSkid = "0"
                    isUphillSkid = true
                    chosenConditionIndex = 0
                    lastSpeedKmh = null
                    error1 = null
                    expandedSkidMenu = false
                    showSpeedResultDialog = false
                }) {
                    Text("Сбросить")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            error1?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

            SpeedResultDialog(
                showDialog = showSpeedResultDialog,
                onDismissRequest = { showSpeedResultDialog = false },
                resultSpeedKmh = lastSpeedKmh,
                skidLength = skidLengthText,
                slopePercent = slopePercentTextSkid,
                isUphill = isUphillSkid,
                roadConditionText = ROAD_CONDITIONS[chosenConditionIndex].first,
                manualMu = if (ROAD_CONDITIONS[chosenConditionIndex].second == null) manualMuText else null,
                context = context
            )
        }
    }
}

@Composable
private fun StoppingDistanceResultDialog(
    showDialog: Boolean,
    onDismissRequest: () -> Unit,
    context: Context,
    // Входные данные
    speedValue: String,
    selectedSpeedUnit: SpeedUnit,
    perceptionTime: String,
    driverReactionTime: String,
    brakeSystemTime: String,
    slopePercent: String,
    isUphill: Boolean,
    roadConditionText: String,
    manualMu: String?,
    // Расчетные значения
    finalResultStopDistance: Double?,
    finalBrakingDistance: Double?,
    finalReactionDistance: Double?,
    finalTotalReactionTime: Double?
) {
    if (showDialog && finalResultStopDistance != null) {
        val unitStr = if (selectedSpeedUnit == SpeedUnit.KMH) "км/ч" else "м/с"
        val slopeDirStr = if (slopePercent.trim() == "0" || slopePercent.toDoubleOrNull() == 0.0) "" else if (isUphill) " (вверх)" else " (вниз)"
        val muValueDisplay = ROAD_CONDITIONS.find { it.first == roadConditionText }?.second?.toString() ?: manualMu ?: "Не указан"

        val shareText = buildString {
            append("Расчет остановочного пути:\n")
            append("  Общий остановочный путь: ${formatNullable(finalResultStopDistance)} м\n")
            append("Подробности:\n")
            append("  Начальная скорость: $speedValue $unitStr\n")
            append("  Время восприятия (t_hp): $perceptionTime с\n")
            append("  Время реакции водителя (t_hr): $driverReactionTime с\n")
            append("  Время срабатывания тормозов (t_brl): $brakeSystemTime с\n")
            append("  Общее время до начала торможения: ${formatNullable(finalTotalReactionTime)} с\n")
            append("  Путь за время реакции: ${formatNullable(finalReactionDistance)} м\n")
            append("  Уклон дороги: $slopePercent%$slopeDirStr\n")
            append("  Состояние дороги: $roadConditionText (μ ≈ $muValueDisplay)\n")
            if (ROAD_CONDITIONS.find { it.first == roadConditionText }?.second == null && manualMu != null && manualMu.isNotBlank()) {
                append("  Коэффициент трения μ (вручную): $manualMu\n")
            }
            append("  Тормозной путь (с учетом уклона): ${formatNullable(finalBrakingDistance)} м")
        }

        AlertDialog(
            onDismissRequest = onDismissRequest,
            icon = { Icon(Icons.Filled.Calculate, contentDescription = "Результат расчета остановочного пути") },
            title = { Text("Результат: Остановочный путь") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Входные данные:", style = MaterialTheme.typography.titleSmall)
                    Text("  Скорость: $speedValue $unitStr", style = MaterialTheme.typography.bodyMedium)
                    Text("  Время реакции (общ.): ${formatNullable(finalTotalReactionTime)} с (воспр.: ${perceptionTime}с, водителя: ${driverReactionTime}с, сист.: ${brakeSystemTime}с)", style = MaterialTheme.typography.bodyMedium)
                    Text("  Уклон: $slopePercent%$slopeDirStr", style = MaterialTheme.typography.bodyMedium)
                    Text("  Состояние: $roadConditionText (μ ≈ $muValueDisplay)", style = MaterialTheme.typography.bodyMedium)
                    if (ROAD_CONDITIONS.find { it.first == roadConditionText }?.second == null && manualMu != null && manualMu.isNotBlank()) {
                         Text("  Коэфф. трения μ (вручную): $manualMu", style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Результаты расчета:", style = MaterialTheme.typography.titleSmall)
                    Text("  Путь за время реакции: ${formatNullable(finalReactionDistance)} м", style = MaterialTheme.typography.bodyMedium)
                    Text("  Тормозной путь: ${formatNullable(finalBrakingDistance)} м", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Общий остановочный путь: ${formatNullable(finalResultStopDistance)} м",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        val chooser = Intent.createChooser(intent, "Поделиться результатом")
                        try {
                            context.startActivity(chooser)
                        } catch (e: Exception) { /* Игнорируем */ }
                        onDismissRequest()
                    }
                ) {
                    Text("Поделиться")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text("Закрыть")
                }
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StoppingDistanceSection(
    context: Context
) {
    var speedValueText by rememberSaveable { mutableStateOf("") }
    var selectedSpeedUnit by rememberSaveable { mutableStateOf(SpeedUnit.KMH) }
    var perceptionTimeText by rememberSaveable { mutableStateOf("1.0") }
    var driverReactionTimeText by rememberSaveable { mutableStateOf("0.4") }
    var brakeSystemTimeText by rememberSaveable { mutableStateOf("0.1") }
    var slopePercentText by rememberSaveable { mutableStateOf("0") }
    var isUphill by rememberSaveable { mutableStateOf(true) }
    var chosenConditionIndex2 by rememberSaveable { mutableStateOf(0) }
    var manualMuText2 by rememberSaveable { mutableStateOf("") }
    
    // Состояния для хранения результатов, чтобы передать в диалог
    var resultStopDistanceState by remember { mutableStateOf<Double?>(null) }
    var brakingDistanceState by remember { mutableStateOf<Double?>(null) }
    var reactionDistanceState by remember { mutableStateOf<Double?>(null) }
    var calculatedTotalReactionTimeState by remember { mutableStateOf<Double?>(null) }
    
    var error2 by remember { mutableStateOf<String?>(null) }
    var expandedStopMenu by remember { mutableStateOf(false) }
    var showStoppingDistanceResultDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = speedValueText,
                    onValueChange = { speedValueText = it; error2 = null },
                    label = { Text("Начальная скорость") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    isError = error2?.contains("скорость") == true
                )
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Column {
                    TextButton(onClick = { selectedSpeedUnit = SpeedUnit.KMH }) {
                        Text("км/ч", color = if (selectedSpeedUnit == SpeedUnit.KMH) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    TextButton(onClick = { selectedSpeedUnit = SpeedUnit.MS }) {
                        Text("м/с", color = if (selectedSpeedUnit == SpeedUnit.MS) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = perceptionTimeText,
                onValueChange = { perceptionTimeText = it; error2 = null },
                label = { Text("Время восприятия опасности, с (t_hp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = error2?.contains("восприятия") == true
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = driverReactionTimeText,
                onValueChange = { driverReactionTimeText = it; error2 = null },
                label = { Text("Время реакции водителя, с (t_hr)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = error2?.contains("реакции водителя") == true
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = brakeSystemTimeText,
                onValueChange = { brakeSystemTimeText = it; error2 = null },
                label = { Text("Время срабатывания тормозов, с (t_brl)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = error2?.contains("срабатывания тормозов") == true
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = slopePercentText,
                onValueChange = { slopePercentText = it; error2 = null },
                label = { Text("Уклон дороги, % (0 для ровной)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = error2?.contains("уклон") == true
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                TextButton(onClick = { isUphill = true }) {
                    Text("Движение вверх", color = if (isUphill) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                TextButton(onClick = { isUphill = false }) {
                    Text("Движение вниз", color = if (!isUphill) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text("Состояние дороги:", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom=4.dp))
            ExposedDropdownMenuBox(
                expanded = expandedStopMenu,
                onExpandedChange = { expandedStopMenu = !expandedStopMenu },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = ROAD_CONDITIONS[chosenConditionIndex2].first,
                    onValueChange = {}, 
                    readOnly = true,
                    label = { Text("Выберите условие") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStopMenu) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    isError = error2?.contains("μ") == true && ROAD_CONDITIONS[chosenConditionIndex2].second != null
                )
                ExposedDropdownMenu(
                    expanded = expandedStopMenu,
                    onDismissRequest = { expandedStopMenu = false }
                ) {
                    ROAD_CONDITIONS.forEachIndexed { index, condition ->
                        DropdownMenuItem(
                            text = { Text(condition.first) },
                            onClick = {
                                chosenConditionIndex2 = index
                                expandedStopMenu = false
                                error2 = null
                                if (condition.second != null) {
                                    manualMuText2 = ""
                                }
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))

            if (ROAD_CONDITIONS[chosenConditionIndex2].second == null) { 
                OutlinedTextField(
                    value = manualMuText2,
                    onValueChange = { manualMuText2 = it; error2 = null },
                    label = { Text("ИЛИ Коэффициент трения μ (например 0.7)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    isError = error2?.contains("μ") == true
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = {
                    error2 = null
                    val speedInput = parseDoubleLenient(speedValueText)
                    val t_hp = parseDoubleLenient(perceptionTimeText)
                    val t_hr = parseDoubleLenient(driverReactionTimeText)
                    val t_brl = parseDoubleLenient(brakeSystemTimeText)
                    val slopePercentVal = parseDoubleLenient(slopePercentText)
                    val muFromList = ROAD_CONDITIONS[chosenConditionIndex2].second
                    val muManual = parseDoubleLenient(manualMuText2)
                    val mu = muFromList ?: muManual

                    if (speedInput == null || speedInput <= 0.0) {
                        error2 = "Введите корректную скорость (> 0)"
                        return@Button
                    }
                    if (t_hp == null || t_hp < 0.0) {
                        error2 = "Введите корректное время восприятия (≥ 0)"
                        return@Button
                    }
                    if (t_hr == null || t_hr < 0.0) {
                        error2 = "Введите корректное время реакции водителя (≥ 0)"
                        return@Button
                    }
                    if (t_brl == null || t_brl < 0.0) {
                        error2 = "Введите корректное время срабатывания тормозов (≥ 0)"
                        return@Button
                    }
                    if (mu == null || mu <= 0.0) {
                        error2 = "Введите корректный μ (> 0)"
                        return@Button
                    }
                    if (slopePercentVal == null) {
                        error2 = "Введите корректный уклон (число)"
                        return@Button
                    }

                    val vms = if (selectedSpeedUnit == SpeedUnit.KMH) speedInput / 3.6 else speedInput
                    val totalReactionTime = t_hp + t_hr + t_brl
                    
                    calculatedTotalReactionTimeState = totalReactionTime
                    reactionDistanceState = vms * totalReactionTime

                    val alpha = atan(slopePercentVal / 100.0)
                    val gFactor = mu * cos(alpha) + (if (isUphill) sin(alpha) else -sin(alpha))

                    if (gFactor <= 0) {
                        error2 = "Торможение невозможно (слишком крутой спуск/низкое трение)"
                        brakingDistanceState = Double.POSITIVE_INFINITY
                        resultStopDistanceState = Double.POSITIVE_INFINITY
                        showStoppingDistanceResultDialog = true // Показываем диалог даже с ошибкой невозможности
                        return@Button
                    }

                    brakingDistanceState = (vms * vms) / (2.0 * G * gFactor)
                    resultStopDistanceState = reactionDistanceState!! + brakingDistanceState!!
                    showStoppingDistanceResultDialog = true

                }) { Text("Рассчитать") }

                Button(onClick = {
                    speedValueText = ""
                    perceptionTimeText = "1.0"
                    driverReactionTimeText = "0.4"
                    brakeSystemTimeText = "0.1"
                    slopePercentText = "0"
                    isUphill = true
                    selectedSpeedUnit = SpeedUnit.KMH
                    manualMuText2 = ""
                    chosenConditionIndex2 = 0
                    resultStopDistanceState = null
                    brakingDistanceState = null
                    reactionDistanceState = null
                    calculatedTotalReactionTimeState = null
                    error2 = null
                    expandedStopMenu = false
                    showStoppingDistanceResultDialog = false
                }) { Text("Сбросить") }
            }
            Spacer(modifier = Modifier.height(8.dp))
            error2?.let { 
                if (!showStoppingDistanceResultDialog || (resultStopDistanceState != Double.POSITIVE_INFINITY && resultStopDistanceState != null)) {
                     Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }

            StoppingDistanceResultDialog(
                showDialog = showStoppingDistanceResultDialog,
                onDismissRequest = { showStoppingDistanceResultDialog = false },
                context = context,
                speedValue = speedValueText,
                selectedSpeedUnit = selectedSpeedUnit,
                perceptionTime = perceptionTimeText,
                driverReactionTime = driverReactionTimeText,
                brakeSystemTime = brakeSystemTimeText,
                slopePercent = slopePercentText,
                isUphill = isUphill,
                roadConditionText = ROAD_CONDITIONS[chosenConditionIndex2].first,
                manualMu = if (ROAD_CONDITIONS[chosenConditionIndex2].second == null) manualMuText2 else null,
                finalResultStopDistance = resultStopDistanceState,
                finalBrakingDistance = brakingDistanceState,
                finalReactionDistance = reactionDistanceState,
                finalTotalReactionTime = calculatedTotalReactionTimeState
            )
        }
    }
}


@Composable
private fun GeneralNote() {
    Text(
        text = "Примечание: формулы являются приближенными. Реальная скорость и пути зависят от множества факторов (состояние шин, уклон, ABS, температура и т.д.).",
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(top = 16.dp, bottom = 16.dp) // Добавил и нижний отступ, чтобы было виднее при скролле
    )
}

@Preview(showBackground = true, locale = "ru")
@Composable
fun BrakingPathCalculatorScreenPreview() {
    Surface {
        BrakingPathCalculatorScreen()
    }
}