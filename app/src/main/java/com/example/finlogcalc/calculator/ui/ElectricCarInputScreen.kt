package com.example.finlogcalc.calculator.ui

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Power
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.finlogcalc.R
import com.example.finlogcalc.calculator.ElectricCarCalculatorViewModel
import com.example.finlogcalc.calculator.ElectricChargeMode
import com.example.finlogcalc.calculator.ElectricCarCalculatorScreenRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElectricCarInputScreen(
    navController: NavController,
    viewModel: ElectricCarCalculatorViewModel,
    mainAppNavController: NavController,
    scaffoldPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
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
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(MaterialTheme.colorScheme.primary, ElectricGreenDark)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_electric_car),
                            contentDescription = "Electric Car Icon",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Column {
                        Text(
                            "Электромобиль",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Рассчитайте зарядку и запас хода",
                            style = MaterialTheme.typography.bodyLarge,
                            color = ElectricTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = viewModel.batteryCapacity,
                    onValueChange = { viewModel.batteryCapacity = it },
                    label = { Text("Емкость батареи (кВт·ч)") },
                    leadingIcon = { Icon(Icons.Filled.BatteryChargingFull, contentDescription = "Емкость батареи", tint = MaterialTheme.colorScheme.primary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    isError = viewModel.batteryCapacityError,
                    supportingText = { if (viewModel.batteryCapacityError) Text("Введите положительное число") },
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = ElectricBorder,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        errorContainerColor = MaterialTheme.colorScheme.surface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                OutlinedTextField(
                    value = viewModel.chargingPower,
                    onValueChange = { viewModel.chargingPower = it },
                    label = { Text("Мощность зарядки (кВт)") },
                    leadingIcon = { Icon(Icons.Filled.Power, contentDescription = "Мощность зарядки", tint = MaterialTheme.colorScheme.primary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    isError = viewModel.chargingPowerError,
                    supportingText = { if (viewModel.chargingPowerError) Text("Введите положительное число") },
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = ElectricBorder,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        errorContainerColor = MaterialTheme.colorScheme.surface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                TabRow(
                    selectedTabIndex = viewModel.currentChargeMode.ordinal,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    ElectricChargeMode.entries.forEachIndexed { index, mode ->
                        Tab(
                            selected = viewModel.currentChargeMode == mode,
                            onClick = { viewModel.currentChargeMode = mode },
                            text = { Text(mode.title) },
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = ElectricTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (viewModel.currentChargeMode == ElectricChargeMode.CHARGE_TO_LEVEL) {
                    Text("Зарядка до уровня:", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = viewModel.startChargePercent,
                            onValueChange = { viewModel.startChargePercent = it },
                            label = { Text("Начальный (%)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = MaterialTheme.shapes.medium,
                            isError = viewModel.startChargePercentError,
                            supportingText = { if (viewModel.startChargePercentError) Text("От 0 до 100%") },
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                unfocusedIndicatorColor = ElectricBorder,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                errorContainerColor = MaterialTheme.colorScheme.surface,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        OutlinedTextField(
                            value = viewModel.endChargePercent,
                            onValueChange = { viewModel.endChargePercent = it },
                            label = { Text("Конечный (%)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = MaterialTheme.shapes.medium,
                            isError = viewModel.endChargePercentError,
                            supportingText = { if (viewModel.endChargePercentError) Text("От 0 до 100%") },
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                unfocusedIndicatorColor = ElectricBorder,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                errorContainerColor = MaterialTheme.colorScheme.surface,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                    Slider(
                        value = viewModel.endChargePercent.text.toFloatOrNull() ?: 100f,
                        onValueChange = { viewModel.endChargePercent = TextFieldValue(it.toInt().toString()) },
                        valueRange = 0f..100f,
                        steps = 100,
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.secondary
                        )
                    )
                    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()){
                        Button(
                            onClick = {
                                viewModel.startChargePercent = TextFieldValue("20")
                                viewModel.endChargePercent = TextFieldValue("80")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                            shape = MaterialTheme.shapes.medium
                        ) { Text("20% – 80%") }
                        Button(
                            onClick = {
                                viewModel.startChargePercent = TextFieldValue("0")
                                viewModel.endChargePercent = TextFieldValue("100")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                            shape = MaterialTheme.shapes.medium
                        ) { Text("0% – 100%") }
                    }

                } else { // CHARGE_BY_TIME
                    Text("Зарядка по времени:", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                    OutlinedTextField(
                        value = viewModel.chargeTimeHours,
                        onValueChange = { viewModel.chargeTimeHours = it },
                        label = { Text("Время зарядки (часы)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        isError = viewModel.chargeTimeHoursError,
                        supportingText = { if (viewModel.chargeTimeHoursError) Text("Введите положительное число") },
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = ElectricBorder,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            errorContainerColor = MaterialTheme.colorScheme.surface,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Опциональные параметры:", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)

                OutlinedTextField(
                    value = viewModel.energyConsumption,
                    onValueChange = { viewModel.energyConsumption = it },
                    label = { Text("Расход (кВт·ч на 100 км)") },
                    leadingIcon = { Icon(Icons.Filled.EvStation, contentDescription = "Расход энергии", tint = MaterialTheme.colorScheme.primary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    isError = viewModel.energyConsumptionError,
                    supportingText = { if (viewModel.energyConsumptionError) Text("Введите положительное число, если указано") },
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = ElectricBorder,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        errorContainerColor = MaterialTheme.colorScheme.surface,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = viewModel.pricePerKwh,
                        onValueChange = { viewModel.pricePerKwh = it },
                        label = { Text("Цена 1 кВт·ч") },
                        leadingIcon = { Icon(Icons.Filled.MonetizationOn, contentDescription = "Цена", tint = MaterialTheme.colorScheme.primary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(0.65f),
                        shape = MaterialTheme.shapes.medium,
                        isError = viewModel.pricePerKwhError,
                        supportingText = { if (viewModel.pricePerKwhError) Text("Введите число, если указано") },
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = ElectricBorder,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            errorContainerColor = MaterialTheme.colorScheme.surface,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    ElectricCarCurrencySelector(viewModel, modifier = Modifier.weight(0.35f))
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (viewModel.calculate()) {
                            navController.navigate(ElectricCarCalculatorScreenRoute.Result.route)
                        }
                    },
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Calculate, contentDescription = "Рассчитать", tint = Color.White)
                            Spacer(Modifier.width(4.dp))
                            Text("РАССЧИТАТЬ", color = Color.White, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = { mainAppNavController.popBackStack() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(
                        width = 2.dp,
                        color = ElectricBorder
                    ),
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Home, contentDescription = "Главное меню", tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(4.dp))
                        Text("Главное меню", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
