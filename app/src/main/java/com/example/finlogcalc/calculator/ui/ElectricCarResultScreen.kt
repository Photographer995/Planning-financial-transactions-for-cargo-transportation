package com.example.finlogcalc.calculator.ui

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.finlogcalc.calculator.ElectricCarCalculatorViewModel
import com.example.finlogcalc.calculator.ElectricChargeMode

@Composable
fun ElectricCarResultScreen(
    navController: NavController,
    viewModel: ElectricCarCalculatorViewModel,
    mainAppNavController: NavController,
    scaffoldPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(
                start = scaffoldPadding.calculateStartPadding(layoutDirection) + 16.dp,
                end = scaffoldPadding.calculateEndPadding(layoutDirection) + 16.dp,
                top = scaffoldPadding.calculateTopPadding() + 16.dp,
                bottom = scaffoldPadding.calculateBottomPadding() + 16.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Результаты расчета зарядки",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Calculated Results
                    if (viewModel.currentChargeMode == ElectricChargeMode.CHARGE_TO_LEVEL) {
                        ResultRow(
                            label = "Время зарядки:",
                            value = viewModel.calculatedChargingTimeHours?.let { "${String.format(java.util.Locale.US, "%.2f", it)} часов" }
                                ?: "",
                            isError = viewModel.calculatedChargingTimeHours == null && (viewModel.batteryCapacity.text.isNotBlank() && viewModel.chargingPower.text.isNotBlank()),
                            errorText = "(не рассчитано)"
                        )
                    } else { // CHARGE_BY_TIME
                        ResultRow(
                            label = "Конечный уровень заряда:",
                            value = viewModel.calculatedFinalChargeLevelPercent?.let { "${String.format(java.util.Locale.US, "%.1f", it)}%" }
                                ?: "",
                            isError = viewModel.calculatedFinalChargeLevelPercent == null && (viewModel.batteryCapacity.text.isNotBlank() && viewModel.chargingPower.text.isNotBlank() && viewModel.chargeTimeHours.text.isNotBlank()),
                            errorText = "(не рассчитано)"
                        )
                    }

                    val priceIsEntered = viewModel.pricePerKwh.text.isNotBlank()
                    val priceIsZero = viewModel.pricePerKwh.text.toDoubleOrNull() == 0.0
                    val costErrorText = when {
                        viewModel.calculatedTotalCost != null -> ""
                        priceIsZero && priceIsEntered -> "" // Explicitly 0.00
                        !priceIsEntered -> "(не указано)"
                        else -> "(неверные данные для расчета)"
                    }

                    ResultRow(
                        label = "Стоимость зарядки:",
                        value = viewModel.calculatedTotalCost?.let { "${String.format(java.util.Locale.US, "%.2f", it)} ${viewModel.selectedCurrency.symbol}" }
                            ?: (if (priceIsZero && priceIsEntered) "0.00 ${viewModel.selectedCurrency.symbol}" else costErrorText),
                        isError = viewModel.calculatedTotalCost == null && priceIsEntered && !priceIsZero,
                        errorText = costErrorText
                    )

                    val consumptionIsEntered = viewModel.energyConsumption.text.isNotBlank()
                    val rangeErrorText = when {
                        viewModel.calculatedRangeKm != null -> ""
                        !consumptionIsEntered -> "(не указано)"
                        else -> "(не рассчитано)"
                    }
                    ResultRow(
                        label = "Запас хода:",
                        value = viewModel.calculatedRangeKm?.let { "${String.format(java.util.Locale.US, "%.0f", it)} км" } ?: rangeErrorText,
                        isError = viewModel.calculatedRangeKm == null && consumptionIsEntered,
                        errorText = rangeErrorText
                    )

                    // Input Data
                    Text(
                        "Исходные данные:",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                    )
                    ResultRow(
                        label = "Емкость батареи:",
                        value = viewModel.batteryCapacity.text.takeIf { it.isNotBlank() }
                            ?.let { "$it кВт·ч" } ?: "(не указано)"
                    )
                    ResultRow(
                        label = "Мощность зарядки:",
                        value = viewModel.chargingPower.text.takeIf { it.isNotBlank() }
                            ?.let { "$it кВт" } ?: "(не указано)"
                    )

                    if (viewModel.currentChargeMode == ElectricChargeMode.CHARGE_TO_LEVEL) {
                        ResultRow(
                            label = "Начальный уровень (ввод):",
                            value = "${viewModel.startChargePercent.text}%"
                        )
                        ResultRow(
                            label = "Конечный уровень (ввод):",
                            value = "${viewModel.endChargePercent.text}%"
                        )
                    } else { // CHARGE_BY_TIME
                        ResultRow(
                            label = "Время зарядки (ввод):",
                            value = viewModel.chargeTimeHours.text.takeIf { it.isNotBlank() }
                                ?.let { "$it ч" } ?: "(не указано)"
                        )
                    }

                    ResultRow(
                        label = "Расход (ввод):",
                        value = viewModel.energyConsumption.text.takeIf { it.isNotBlank() }
                            ?.let { "$it кВт·ч/100км" } ?: "(не указано)"
                    )

                    ResultRow(
                        label = "Цена 1 кВт·ч (ввод):",
                        value = if (viewModel.pricePerKwh.text.isNotBlank()) "${viewModel.pricePerKwh.text} ${viewModel.selectedCurrency.symbol}" else "(не указано)"
                    )

                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND)
                            shareIntent.type = "text/plain"
                            shareIntent.putExtra(Intent.EXTRA_TEXT, viewModel.getShareableResultText())
                            context.startActivity(Intent.createChooser(shareIntent, "Поделиться результатом"))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        border = BorderStroke(width = 2.dp, color = ElectricBorder),
                        shape = MaterialTheme.shapes.medium,
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Share, contentDescription = "Поделиться", tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Поделиться", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(MaterialTheme.colorScheme.primary, ElectricGreenDark)
                            ),
                            shape = MaterialTheme.shapes.medium
                        )
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Назад к расчетам",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { mainAppNavController.popBackStack() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(width = 2.dp, color = ElectricBorder),
                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Home,
                        contentDescription = "Главное меню",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Главное меню", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
