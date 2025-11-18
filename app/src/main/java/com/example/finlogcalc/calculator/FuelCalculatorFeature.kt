package com.example.finlogcalc.calculator

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.finlogcalc.R
import com.example.finlogcalc.calculator.ui.BorderLight
import com.example.finlogcalc.calculator.ui.CardLight
import com.example.finlogcalc.calculator.ui.Emerald400
import com.example.finlogcalc.calculator.ui.FuelCalculatorTheme
import com.example.finlogcalc.calculator.ui.InputBackgroundLight
import com.example.finlogcalc.calculator.ui.LightOrange
import com.example.finlogcalc.calculator.ui.LightPink
import com.example.finlogcalc.calculator.ui.LightRose
import com.example.finlogcalc.calculator.ui.Orange400
import com.example.finlogcalc.calculator.ui.Pink500
import com.example.finlogcalc.calculator.ui.Rose500
import com.example.finlogcalc.calculator.ui.TextPrimary
import com.example.finlogcalc.calculator.ui.TextSecondary
import java.util.Locale
import androidx.compose.foundation.BorderStroke

// --- Enums ---
enum class Currency(val symbol: String, val displayName: String) {
    RUB("₽", "Рубль"),
    USD("$", "Доллар"),
    EUR("€", "Евро"),
    KZT("₸", "Тенге"),
    BYN("Br", "Белорусский рубль");
}

enum class CalculationMode(val title: String) {
    CONSUMPTION_AND_COST("Общий"),
    AVERAGE_CONSUMPTION_GIVEN("Средний"),
    ODOMETER_TRIP("По одометру и баку")
}

enum class FuelUnit(val displayName: String, val perUnitName: String, val toLitersFactor: Double) {
    LITERS("Литры", "литр", 1.0),
    US_GALLONS("Галлоны США", "галлон США", 3.78541),
    IMPERIAL_GALLONS("Имперские галлоны", "имп. галлон", 4.54609);
}

enum class DistanceUnit(val displayName: String, val shortName: String, val toKilometersFactor: Double) {
    KILOMETERS("Километры", "км", 1.0),
    MILES("Мили", "ми", 1.60934);
}

// --- ViewModel ---
class FuelCalculatorViewModel : ViewModel() {
    var fuelUsed by mutableStateOf(TextFieldValue(""))
    var distance by mutableStateOf(TextFieldValue(""))
    var pricePerUnit by mutableStateOf(TextFieldValue(""))
    var averageConsumptionInput by mutableStateOf(TextFieldValue(""))

    // Fields for ODOMETER_TRIP mode
    var odometerStart by mutableStateOf(TextFieldValue(""))
    var odometerEnd by mutableStateOf(TextFieldValue(""))
    var fuelInTankStart by mutableStateOf(TextFieldValue(""))
    var fuelInTankEnd by mutableStateOf(TextFieldValue(""))

    var selectedCurrency by mutableStateOf(Currency.RUB)
    var calculationMode by mutableStateOf(CalculationMode.CONSUMPTION_AND_COST)

    var selectedFuelUnit by mutableStateOf(FuelUnit.LITERS)
    var selectedDistanceUnit by mutableStateOf(DistanceUnit.KILOMETERS)

    var calculatedAverageConsumptionResult by mutableStateOf<Double?>(null)
    var calculatedTotalCostResult by mutableStateOf<Double?>(null)
    var calculatedFuelNeededResult by mutableStateOf<Double?>(null)
    var calculatedDistanceResult by mutableStateOf<Double?>(null)
    var calculatedFuelSpentResult by mutableStateOf<Double?>(null)

    fun calculate() {
        val priceInput = pricePerUnit.text.toDoubleOrNull()

        calculatedAverageConsumptionResult = null
        calculatedTotalCostResult = null
        calculatedFuelNeededResult = null
        calculatedDistanceResult = null
        calculatedFuelSpentResult = null

        val pricePerLiter = if (priceInput != null && selectedFuelUnit.toLitersFactor > 0) {
            priceInput / selectedFuelUnit.toLitersFactor
        } else {
            null
        }

        when (calculationMode) {
            CalculationMode.CONSUMPTION_AND_COST -> {
                val fuelInput = fuelUsed.text.toDoubleOrNull()
                val distanceInput = distance.text.toDoubleOrNull()
                val distanceInKm = distanceInput?.let { it * selectedDistanceUnit.toKilometersFactor }
                val fuelInLiters = fuelInput?.let { it * selectedFuelUnit.toLitersFactor }

                if (distanceInKm != null && distanceInKm > 0 && fuelInLiters != null && fuelInLiters > 0) {
                    if (selectedFuelUnit.toLitersFactor > 0 && selectedDistanceUnit.toKilometersFactor > 0) {
                        val consumptionInLitersPer100Km = fuelInLiters / distanceInKm * 100
                        calculatedAverageConsumptionResult = (consumptionInLitersPer100Km / selectedFuelUnit.toLitersFactor) * (selectedDistanceUnit.toKilometersFactor)
                    }

                    if (priceInput == 0.0) {
                        calculatedTotalCostResult = 0.0
                    } else if (pricePerLiter != null) {
                        calculatedTotalCostResult = fuelInLiters * pricePerLiter
                    }
                }
            }
            CalculationMode.AVERAGE_CONSUMPTION_GIVEN -> {
                val distanceInput = distance.text.toDoubleOrNull()
                val avgConsumptionInputFromForm = averageConsumptionInput.text.toDoubleOrNull()
                val distanceInKm = distanceInput?.let { it * selectedDistanceUnit.toKilometersFactor }

                if (avgConsumptionInputFromForm != null && avgConsumptionInputFromForm > 0 && distanceInKm != null && distanceInKm > 0) {
                    val avgInputLiters = avgConsumptionInputFromForm * selectedFuelUnit.toLitersFactor
                    val avgInputDistanceKm = 100.0 * selectedDistanceUnit.toKilometersFactor

                    val avgConsLitersPerKm = if (avgInputDistanceKm > 0) {
                        avgInputLiters / avgInputDistanceKm
                    } else {
                        0.0
                    }

                    val fuelNeededInLiters = avgConsLitersPerKm * distanceInKm

                    if (selectedFuelUnit.toLitersFactor > 0) {
                        calculatedFuelNeededResult = fuelNeededInLiters / selectedFuelUnit.toLitersFactor
                    }

                    calculatedAverageConsumptionResult = avgConsumptionInputFromForm

                    if (priceInput == 0.0) {
                        calculatedTotalCostResult = 0.0
                    } else if (pricePerLiter != null) {
                        calculatedTotalCostResult = fuelNeededInLiters * pricePerLiter
                    }
                }
            }
            CalculationMode.ODOMETER_TRIP -> {
                val odoStart = odometerStart.text.toDoubleOrNull()
                val odoEnd = odometerEnd.text.toDoubleOrNull()
                val fuelStart = fuelInTankStart.text.toDoubleOrNull()
                val fuelEnd = fuelInTankEnd.text.toDoubleOrNull()

                if (odoStart != null && odoEnd != null && odoEnd > odoStart &&
                    fuelStart != null && fuelEnd != null && fuelStart > fuelEnd) {

                    val distTraveledInSelectedUnits = odoEnd - odoStart
                    calculatedDistanceResult = distTraveledInSelectedUnits
                    val distanceInKm = distTraveledInSelectedUnits * selectedDistanceUnit.toKilometersFactor

                    val fuelSpentInSelectedUnits = fuelStart - fuelEnd
                    calculatedFuelSpentResult = fuelSpentInSelectedUnits
                    val fuelInLiters = fuelSpentInSelectedUnits * selectedFuelUnit.toLitersFactor

                    if (distanceInKm > 0 && fuelInLiters > 0) {
                        val consumptionInLitersPer100Km = fuelInLiters / distanceInKm * 100
                        calculatedAverageConsumptionResult = (consumptionInLitersPer100Km / selectedFuelUnit.toLitersFactor) * (selectedDistanceUnit.toKilometersFactor)

                        if (priceInput == 0.0) {
                            calculatedTotalCostResult = 0.0
                        } else if (pricePerLiter != null) {
                            calculatedTotalCostResult = fuelInLiters * pricePerLiter
                        }
                    }
                }
            }
        }
    }

    fun clearResults() {
        calculatedAverageConsumptionResult = null
        calculatedTotalCostResult = null
        calculatedFuelNeededResult = null
        calculatedDistanceResult = null
        calculatedFuelSpentResult = null
    }

    fun getShareableResultText(): String {
        val sb = StringBuilder()
        sb.append("Результаты расчета топлива:\n")

        when (calculationMode) {
            CalculationMode.CONSUMPTION_AND_COST -> {
                calculatedAverageConsumptionResult?.let { sb.append("Средний расход: ${it.format(2)} ${selectedFuelUnit.perUnitName}/100 ${selectedDistanceUnit.shortName}\n") }
                calculatedTotalCostResult?.let { sb.append("Общая стоимость: ${it.format(2)} ${selectedCurrency.symbol}\n") }
                sb.append("Израсходовано топлива: ${fuelUsed.text} ${selectedFuelUnit.displayName.lowercase()}\n")
                sb.append("Пройденное расстояние: ${distance.text} ${selectedDistanceUnit.shortName}\n")
            }
            CalculationMode.AVERAGE_CONSUMPTION_GIVEN -> {
                calculatedFuelNeededResult?.let { sb.append("Необходимо топлива: ${it.format(2)} ${selectedFuelUnit.displayName.lowercase()}\n") }
                calculatedTotalCostResult?.let { sb.append("Общая стоимость поездки: ${it.format(2)} ${selectedCurrency.symbol}\n") }
                sb.append("Средний расход (ввод): ${averageConsumptionInput.text} ${selectedFuelUnit.perUnitName}/100 ${selectedDistanceUnit.shortName}\n")
                sb.append("Пройденное расстояние: ${distance.text} ${selectedDistanceUnit.shortName}\n")
            }
            CalculationMode.ODOMETER_TRIP -> {
                calculatedDistanceResult?.let { sb.append("Пройденное расстояние: ${it.format(2)} ${selectedDistanceUnit.shortName}\n") }
                calculatedFuelSpentResult?.let { sb.append("Потрачено топлива: ${it.format(2)} ${selectedFuelUnit.displayName.lowercase()}\n") }
                calculatedAverageConsumptionResult?.let { sb.append("Средний расход: ${it.format(2)} ${selectedFuelUnit.perUnitName}/100 ${selectedDistanceUnit.shortName}\n") }
                calculatedTotalCostResult?.let { sb.append("Общая стоимость: ${it.format(2)} ${selectedCurrency.symbol}\n") }
                sb.append("Одометр (начало): ${odometerStart.text} ${selectedDistanceUnit.shortName}\n")
                sb.append("Одометр (конец): ${odometerEnd.text} ${selectedDistanceUnit.shortName}\n")
                sb.append("Топливо (начало): ${fuelInTankStart.text} ${selectedFuelUnit.displayName.lowercase()}\n")
                sb.append("Топливо (конец): ${fuelInTankEnd.text} ${selectedFuelUnit.displayName.lowercase()}\n")
            }
        }
        if (pricePerUnit.text.isNotBlank()) {
            sb.append("Цена за ${selectedFuelUnit.perUnitName}: ${pricePerUnit.text} ${selectedCurrency.symbol}\n")
        } else {
            sb.append("Цена за ${selectedFuelUnit.perUnitName}: 0 ${selectedCurrency.symbol}\n")
        }
        return sb.toString()
    }
}

// --- Navigation for this feature ---
sealed class FuelCalculatorScreenRoute(val route: String) {
    object Input : FuelCalculatorScreenRoute("fuel_calculator_input")
    object Result : FuelCalculatorScreenRoute("fuel_calculator_result")
}

// --- Main NavHost for the Fuel Calculator Feature ---
@Composable
fun FuelCalculatorFeatureNavHost(
    mainNavController: NavController,
    scaffoldPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val calculatorViewModel: FuelCalculatorViewModel = viewModel()
    val internalNavController = rememberNavController()

    FuelCalculatorTheme { // Apply the new theme here
        NavHost(
            navController = internalNavController,
            startDestination = FuelCalculatorScreenRoute.Input.route,
            modifier = modifier
        ) {
            composable(FuelCalculatorScreenRoute.Input.route) {
                FuelCalculatorInputScreen(
                    navController = internalNavController,
                    viewModel = calculatorViewModel,
                    mainAppNavController = mainNavController,
                    scaffoldPadding = scaffoldPadding,
                    modifier = Modifier.fillMaxSize() // Fill max size for background
                )
            }
            composable(FuelCalculatorScreenRoute.Result.route) {
                FuelCalculatorResultScreen(
                    navController = internalNavController,
                    viewModel = calculatorViewModel,
                    mainAppNavController = mainNavController,
                    scaffoldPadding = scaffoldPadding,
                    modifier = Modifier.fillMaxSize() // Fill max size for background
                )
            }
        }
    }
}

// --- Helper for formatting ---
fun Double.format(digits: Int): String {
    return String.format(Locale.US, "%.${digits}f", this)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelCalculatorInputScreen(
    navController: NavController,
    viewModel: FuelCalculatorViewModel,
    mainAppNavController: NavController,
    scaffoldPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    var fuelUsedError by remember { mutableStateOf<String?>(null) }
    var averageConsumptionError by remember { mutableStateOf<String?>(null) }
    var distanceError by remember { mutableStateOf<String?>(null) }
    var odometerStartError by remember { mutableStateOf<String?>(null) }
    var odometerEndError by remember { mutableStateOf<String?>(null) }
    var fuelInTankStartError by remember { mutableStateOf<String?>(null) }
    var fuelInTankEndError by remember { mutableStateOf<String?>(null) }


    val emptyOrZeroErrorMessage = "Поле не может быть пустым или содержать 0"
    val endLessThanStartError = "Конечное значение должно быть больше начального"

    val layoutDirection = LocalLayoutDirection.current

    Box(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(LightOrange, LightRose, LightPink)
                )
            )
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
            colors = CardDefaults.cardColors(containerColor = CardLight.copy(alpha = 0.9f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp) // Changed to spacedBy
            ) {
                // Header
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
                                    colors = listOf(Orange400, Rose500)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_fuel), // Assuming you have a fuel icon
                            contentDescription = "Fuel Icon",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Column {
                        Text(
                            "Калькулятор топлива",
                            style = MaterialTheme.typography.titleLarge.copy(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Orange400, Rose500)
                                )
                            ),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Рассчитайте расход и стоимость топлива",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Calculation Mode
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Выберите режим расчета:",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextPrimary
                    )
                    CalculationMode.entries.forEach { mode ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .clickable {
                                    viewModel.calculationMode = mode
                                    viewModel.clearResults()
                                }
                                .background(if (viewModel.calculationMode == mode) LightOrange.copy(alpha = 0.5f) else Color.Transparent)
                                .border(
                                    2.dp,
                                    if (viewModel.calculationMode == mode) Orange400.copy(alpha = 0.5f) else Color.Transparent,
                                    MaterialTheme.shapes.medium
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            RadioButton(
                                selected = viewModel.calculationMode == mode,
                                onClick = {
                                    viewModel.calculationMode = mode
                                    viewModel.clearResults()
                                },
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                mode.title,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimary
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Input fields based on calculation mode
                when (viewModel.calculationMode) {
                    CalculationMode.CONSUMPTION_AND_COST -> {
                        FuelInputSection(
                            label = "Израсходовано топлива",
                            value = viewModel.fuelUsed,
                            onValueChange = { newValue ->
                                viewModel.fuelUsed = newValue
                                fuelUsedError = if (newValue.text.toDoubleOrNull() == 0.0 || newValue.text.isEmpty()) emptyOrZeroErrorMessage else null
                            },
                            isError = fuelUsedError != null,
                            errorMessage = fuelUsedError,
                            selectedUnit = viewModel.selectedFuelUnit,
                            onUnitChange = { viewModel.selectedFuelUnit = it },
                            icon = R.drawable.ic_fuel
                        )
                        DistanceInputSection(
                            label = "Пройденное расстояние",
                            value = viewModel.distance,
                            onValueChange = { newValue ->
                                viewModel.distance = newValue
                                distanceError = if (newValue.text.toDoubleOrNull() == 0.0 || newValue.text.isEmpty()) emptyOrZeroErrorMessage else null
                            },
                            isError = distanceError != null,
                            errorMessage = distanceError,
                            selectedUnit = viewModel.selectedDistanceUnit,
                            onUnitChange = { viewModel.selectedDistanceUnit = it },
                            icon = R.drawable.ic_route
                        )
                    }
                    CalculationMode.AVERAGE_CONSUMPTION_GIVEN -> {
                        AverageConsumptionInputSection(
                            viewModel = viewModel,
                            averageConsumptionError = averageConsumptionError,
                            onAverageConsumptionErrorChange = { averageConsumptionError = it },
                            emptyOrZeroErrorMessage = emptyOrZeroErrorMessage
                        )
                        DistanceInputSection(
                            label = "Пройденное расстояние",
                            value = viewModel.distance,
                            onValueChange = { newValue ->
                                viewModel.distance = newValue
                                distanceError = if (newValue.text.toDoubleOrNull() == 0.0 || newValue.text.isEmpty()) emptyOrZeroErrorMessage else null
                            },
                            isError = distanceError != null,
                            errorMessage = distanceError,
                            selectedUnit = viewModel.selectedDistanceUnit,
                            onUnitChange = { viewModel.selectedDistanceUnit = it },
                            icon = R.drawable.ic_route
                        )
                    }
                    CalculationMode.ODOMETER_TRIP -> {
                        OdometerTripInputSection(
                            viewModel = viewModel,
                            odometerStartError = odometerStartError,
                            onOdometerStartErrorChange = { odometerStartError = it },
                            odometerEndError = odometerEndError,
                            onOdometerEndErrorChange = { odometerEndError = it },
                            fuelInTankStartError = fuelInTankStartError,
                            onFuelInTankStartErrorChange = { fuelInTankStartError = it },
                            fuelInTankEndError = fuelInTankEndError,
                            onFuelInTankEndErrorChange = { fuelInTankEndError = it },
                            emptyOrZeroErrorMessage = emptyOrZeroErrorMessage,
                            endLessThanStartError = endLessThanStartError
                        )
                    }
                }

                // Cost and Currency
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Стоимость(${viewModel.selectedCurrency.symbol}/${viewModel.selectedFuelUnit.perUnitName})",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        OutlinedTextField(
                            value = viewModel.pricePerUnit,
                            onValueChange = { viewModel.pricePerUnit = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            shape = MaterialTheme.shapes.medium,
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Orange400,
                                unfocusedIndicatorColor = BorderLight,
                                unfocusedContainerColor = InputBackgroundLight,
                                focusedContainerColor = InputBackgroundLight,
                                errorContainerColor = InputBackgroundLight,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Валюта",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        CurrencySelector(viewModel = viewModel)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Buttons
                Button(
                    onClick = {
                        var hasError = false
                        fuelUsedError = null
                        averageConsumptionError = null
                        distanceError = null
                        odometerStartError = null
                        odometerEndError = null
                        fuelInTankStartError = null
                        fuelInTankEndError = null

                        when (viewModel.calculationMode) {
                            CalculationMode.CONSUMPTION_AND_COST -> {
                                val currentFuelUsed = viewModel.fuelUsed.text
                                val currentDistance = viewModel.distance.text
                                if (currentFuelUsed.toDoubleOrNull() == 0.0 || currentFuelUsed.isEmpty()) {
                                    fuelUsedError = emptyOrZeroErrorMessage; hasError = true
                                }
                                if (currentDistance.toDoubleOrNull() == 0.0 || currentDistance.isEmpty()) {
                                    distanceError = emptyOrZeroErrorMessage; hasError = true
                                }
                            }
                            CalculationMode.AVERAGE_CONSUMPTION_GIVEN -> {
                                val currentAvgConsumption = viewModel.averageConsumptionInput.text
                                val currentDistance = viewModel.distance.text
                                if (currentAvgConsumption.toDoubleOrNull() == 0.0 || currentAvgConsumption.isEmpty()) {
                                    averageConsumptionError = emptyOrZeroErrorMessage; hasError = true
                                }
                                if (currentDistance.toDoubleOrNull() == 0.0 || currentDistance.isEmpty()) {
                                    distanceError = emptyOrZeroErrorMessage; hasError = true
                                }
                            }
                            CalculationMode.ODOMETER_TRIP -> {
                                val odoStartText = viewModel.odometerStart.text
                                val odoEndText = viewModel.odometerEnd.text
                                val fuelStartText = viewModel.fuelInTankStart.text
                                val fuelEndText = viewModel.fuelInTankEnd.text

                                val odoStart = odoStartText.toDoubleOrNull()
                                val odoEnd = odoEndText.toDoubleOrNull()
                                val fuelStart = fuelStartText.toDoubleOrNull()
                                val fuelEnd = fuelEndText.toDoubleOrNull()

                                if (odoStartText.isEmpty()) { odometerStartError = emptyOrZeroErrorMessage; hasError = true }
                                if (odoEndText.isEmpty()) { odometerEndError = emptyOrZeroErrorMessage; hasError = true }
                                else if (odoStart != null && odoEnd != null && odoEnd <= odoStart) { odometerEndError = endLessThanStartError; hasError = true}

                                if (fuelStartText.isEmpty()) { fuelInTankStartError = emptyOrZeroErrorMessage; hasError = true }
                                if (fuelEndText.isEmpty()) { fuelInTankEndError = emptyOrZeroErrorMessage; hasError = true }
                                else if (fuelStart != null && fuelEnd != null && fuelEnd >= fuelStart) { fuelInTankEndError = "Конечное значение должно быть меньше начального"; hasError = true}

                            }
                        }

                        if (!hasError) {
                            viewModel.calculate()
                            navController.navigate(FuelCalculatorScreenRoute.Result.route)
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
                                    colors = listOf(Orange400, Rose500, Pink500)
                                ),
                                shape = MaterialTheme.shapes.medium
                            )
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(painterResource(id = R.drawable.ic_calculator), contentDescription = "Calculate", tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("РАССЧИТАТЬ", color = Color.White, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                Button(
                    onClick = { mainAppNavController.popBackStack() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = TextPrimary
                    ),
                    border = BorderStroke(
                        width = 2.dp,
                        brush = Brush.horizontalGradient(
                            colors = listOf(BorderLight, BorderLight)
                        )
                    ),
                    shape = MaterialTheme.shapes.medium,
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painterResource(id = R.drawable.ic_home), contentDescription = "Main Menu", tint = TextPrimary)
                        Spacer(Modifier.width(8.dp))
                        Text("Главное меню", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySelector(viewModel: FuelCalculatorViewModel) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = viewModel.selectedCurrency.symbol,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Emerald400,
                unfocusedIndicatorColor = BorderLight,
                unfocusedContainerColor = InputBackgroundLight,
                focusedContainerColor = InputBackgroundLight,
                errorContainerColor = InputBackgroundLight,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(CardLight)
        ) {
            Currency.entries.forEach { currency ->
                DropdownMenuItem(
                    text = { Text(currency.displayName, color = TextPrimary) },
                    onClick = {
                        viewModel.selectedCurrency = currency
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelInputSection(
    label: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    isError: Boolean,
    errorMessage: String?,
    selectedUnit: FuelUnit,
    onUnitChange: (FuelUnit) -> Unit,
    icon: Int
) {
    var unitMenuExpanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(painterResource(id = icon), contentDescription = "$label Icon", tint = Orange400, modifier = Modifier.size(20.dp))
            Text(
                text = "$label (${selectedUnit.displayName.lowercase()})",
                style = MaterialTheme.typography.labelMedium,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Box {
                IconButton(onClick = { unitMenuExpanded = true }) {
                    Icon(Icons.Filled.Settings, contentDescription = "Единицы ${label.lowercase()}", tint = TextSecondary)
                }
                DropdownMenu(
                    expanded = unitMenuExpanded,
                    onDismissRequest = { unitMenuExpanded = false },
                    modifier = Modifier.background(CardLight)
                ) {
                    FuelUnit.entries.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit.displayName, color = TextPrimary) },
                            onClick = {
                                onUnitChange(unit)
                                unitMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            isError = isError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Orange400,
                unfocusedIndicatorColor = BorderLight,
                unfocusedContainerColor = InputBackgroundLight,
                focusedContainerColor = InputBackgroundLight,
                errorContainerColor = InputBackgroundLight,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )
        if (isError) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DistanceInputSection(
    label: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    isError: Boolean,
    errorMessage: String?,
    selectedUnit: DistanceUnit,
    onUnitChange: (DistanceUnit) -> Unit,
    icon: Int
) {
    var unitMenuExpanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(painterResource(id = icon), contentDescription = "$label Icon", tint = Rose500, modifier = Modifier.size(20.dp))
            Text(
                text = "$label (${selectedUnit.displayName.lowercase()})",
                style = MaterialTheme.typography.labelMedium,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Box {
                IconButton(onClick = { unitMenuExpanded = true }) {
                    Icon(Icons.Filled.Settings, contentDescription = "Единицы расстояния", tint = TextSecondary)
                }
                DropdownMenu(
                    expanded = unitMenuExpanded,
                    onDismissRequest = { unitMenuExpanded = false },
                    modifier = Modifier.background(CardLight)
                ) {
                    DistanceUnit.entries.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit.displayName, color = TextPrimary) },
                            onClick = {
                                onUnitChange(unit)
                                unitMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            isError = isError,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Rose500,
                unfocusedIndicatorColor = BorderLight,
                unfocusedContainerColor = InputBackgroundLight,
                focusedContainerColor = InputBackgroundLight,
                errorContainerColor = InputBackgroundLight,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )
        if (isError) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AverageConsumptionInputSection(
    viewModel: FuelCalculatorViewModel,
    averageConsumptionError: String?,
    onAverageConsumptionErrorChange: (String?) -> Unit,
    emptyOrZeroErrorMessage: String
) {
    var avgFuelUnitMenuExpanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(painterResource(id = R.drawable.ic_fuel), contentDescription = "Fuel Icon", tint = Orange400, modifier = Modifier.size(20.dp))
            Text(
                text = "Средний расход (${viewModel.selectedFuelUnit.perUnitName}/100 ${viewModel.selectedDistanceUnit.shortName})",
                style = MaterialTheme.typography.labelMedium,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Box {
                IconButton(onClick = { avgFuelUnitMenuExpanded = true }) {
                    Icon(Icons.Filled.Settings, contentDescription = "Единицы для среднего расхода", tint = TextSecondary)
                }
                DropdownMenu(
                    expanded = avgFuelUnitMenuExpanded,
                    onDismissRequest = { avgFuelUnitMenuExpanded = false },
                    modifier = Modifier.background(CardLight)
                ) {
                    Text("Единица топлива:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal=12.dp, vertical = 8.dp), color = TextPrimary)
                    FuelUnit.entries.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit.displayName, color = TextPrimary) },
                            onClick = {
                                viewModel.selectedFuelUnit = unit
                                avgFuelUnitMenuExpanded = false
                            }
                        )
                    }
                    Text("Единица расстояния (для /100):", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal=12.dp, vertical = 8.dp), color = TextPrimary)
                    DistanceUnit.entries.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit.displayName, color = TextPrimary) },
                            onClick = {
                                viewModel.selectedDistanceUnit = unit
                                avgFuelUnitMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }
        OutlinedTextField(
            value = viewModel.averageConsumptionInput,
            onValueChange = { newValue ->
                viewModel.averageConsumptionInput = newValue
                onAverageConsumptionErrorChange(if (newValue.text.toDoubleOrNull() == 0.0 || newValue.text.isEmpty()) emptyOrZeroErrorMessage else null)
            },
            isError = averageConsumptionError != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Orange400,
                unfocusedIndicatorColor = BorderLight,
                unfocusedContainerColor = InputBackgroundLight,
                focusedContainerColor = InputBackgroundLight,
                errorContainerColor = InputBackgroundLight,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )
        if (averageConsumptionError != null) {
            Text(
                text = averageConsumptionError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OdometerTripInputSection(
    viewModel: FuelCalculatorViewModel,
    odometerStartError: String?,
    onOdometerStartErrorChange: (String?) -> Unit,
    odometerEndError: String?,
    onOdometerEndErrorChange: (String?) -> Unit,
    fuelInTankStartError: String?,
    onFuelInTankStartErrorChange: (String?) -> Unit,
    fuelInTankEndError: String?,
    onFuelInTankEndErrorChange: (String?) -> Unit,
    emptyOrZeroErrorMessage: String,
    endLessThanStartError: String
) {
    var odoTripUnitsMenuExpanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(painterResource(id = R.drawable.ic_settings), contentDescription = "Units Icon", tint = Orange400, modifier = Modifier.size(20.dp))
            Text(
                text = "Единицы одометра и бака",
                style = MaterialTheme.typography.labelMedium,
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Box {
                IconButton(onClick = { odoTripUnitsMenuExpanded = true }) {
                    Icon(Icons.Filled.Settings, contentDescription = "Единицы для одометра и бака", tint = TextSecondary)
                }
                DropdownMenu(
                    expanded = odoTripUnitsMenuExpanded,
                    onDismissRequest = { odoTripUnitsMenuExpanded = false },
                    modifier = Modifier.background(CardLight)
                ) {
                    Text("Единица расстояния (одометр):", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal=12.dp, vertical = 8.dp), color = TextPrimary)
                    DistanceUnit.entries.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit.displayName, color = TextPrimary) },
                            onClick = {
                                viewModel.selectedDistanceUnit = unit
                            }
                        )
                    }
                    Text("Единица топлива (бак):", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal=12.dp, vertical = 8.dp), color = TextPrimary)
                    FuelUnit.entries.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit.displayName, color = TextPrimary) },
                            onClick = {
                                viewModel.selectedFuelUnit = unit
                            }
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))

        Text("Одометр (Начало поездки, ${viewModel.selectedDistanceUnit.shortName}):", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
        OutlinedTextField(
            value = viewModel.odometerStart,
            onValueChange = { newValue ->
                viewModel.odometerStart = newValue
                onOdometerStartErrorChange(if (newValue.text.isEmpty()) emptyOrZeroErrorMessage else null)
            },
            isError = odometerStartError != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Orange400,
                unfocusedIndicatorColor = BorderLight,
                unfocusedContainerColor = InputBackgroundLight,
                focusedContainerColor = InputBackgroundLight,
                errorContainerColor = InputBackgroundLight,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )
        if (odometerStartError != null) {
            Text(odometerStartError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 4.dp))
        }
        Spacer(Modifier.height(8.dp))

        Text("Одометр (Конец поездки, ${viewModel.selectedDistanceUnit.shortName}):", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
        OutlinedTextField(
            value = viewModel.odometerEnd,
            onValueChange = { newValue ->
                viewModel.odometerEnd = newValue
                val start = viewModel.odometerStart.text.toDoubleOrNull()
                val end = newValue.text.toDoubleOrNull()
                onOdometerEndErrorChange(if (newValue.text.isEmpty()) emptyOrZeroErrorMessage
                else if (start != null && end != null && end <= start) endLessThanStartError
                else null)
            },
            isError = odometerEndError != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Rose500,
                unfocusedIndicatorColor = BorderLight,
                unfocusedContainerColor = InputBackgroundLight,
                focusedContainerColor = InputBackgroundLight,
                errorContainerColor = InputBackgroundLight,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )
        if (odometerEndError != null) {
            Text(odometerEndError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 4.dp))
        }
        Spacer(Modifier.height(8.dp))

        Text("Топливо в баке (Начало, ${viewModel.selectedFuelUnit.displayName.lowercase()}):", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
        OutlinedTextField(
            value = viewModel.fuelInTankStart,
            onValueChange = { newValue ->
                viewModel.fuelInTankStart = newValue
                onFuelInTankStartErrorChange(if (newValue.text.isEmpty()) emptyOrZeroErrorMessage else null)
            },
            isError = fuelInTankStartError != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Orange400,
                unfocusedIndicatorColor = BorderLight,
                unfocusedContainerColor = InputBackgroundLight,
                focusedContainerColor = InputBackgroundLight,
                errorContainerColor = InputBackgroundLight,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )
        if (fuelInTankStartError != null) {
            Text(fuelInTankStartError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 4.dp))
        }
        Spacer(Modifier.height(8.dp))

        Text("Топливо в баке (Конец, ${viewModel.selectedFuelUnit.displayName.lowercase()}):", style = MaterialTheme.typography.labelMedium, color = TextPrimary)
        OutlinedTextField(
            value = viewModel.fuelInTankEnd,
            onValueChange = { newValue ->
                viewModel.fuelInTankEnd = newValue
                val start = viewModel.fuelInTankStart.text.toDoubleOrNull()
                val end = newValue.text.toDoubleOrNull()
                onFuelInTankEndErrorChange(if (newValue.text.isEmpty()) emptyOrZeroErrorMessage
                else if (start != null && end != null && end >= start) "Конечное значение должно быть меньше начального"
                else null)
            },
            isError = fuelInTankEndError != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Rose500,
                unfocusedIndicatorColor = BorderLight,
                unfocusedContainerColor = InputBackgroundLight,
                focusedContainerColor = InputBackgroundLight,
                errorContainerColor = InputBackgroundLight,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )
        if (fuelInTankEndError != null) {
            Text(fuelInTankEndError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 4.dp))
        }
        Spacer(Modifier.height(8.dp))
    }
}


@Composable
fun ResultRow(label: String, value: String, isError: Boolean = false, errorText: String = "(нет данных)") {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .background(Color.White, shape = MaterialTheme.shapes.medium)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            modifier = Modifier.weight(0.5f)
        )
        Text(
            text = if (isError) errorText else value,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isError) MaterialTheme.colorScheme.error else TextPrimary,
            modifier = Modifier.weight(0.5f),
            textAlign = TextAlign.End
        )
    }
}


@Composable
fun FuelCalculatorResultScreen(
    navController: NavController,
    viewModel: FuelCalculatorViewModel,
    mainAppNavController: NavController,
    scaffoldPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    Box(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(LightOrange, LightRose, LightPink)
                )
            )
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
                colors = CardDefaults.cardColors(containerColor = CardLight.copy(alpha = 0.9f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp) // Changed to spacedBy
                ) {
                    Text(
                        "Результаты расчета",
                        style = MaterialTheme.typography.titleLarge.copy(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Orange400, Rose500)
                            )
                        ),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    val priceText = viewModel.pricePerUnit.text
                    val priceIsEntered = priceText.isNotBlank()
                    val priceIsZero = priceText.toDoubleOrNull() == 0.0

                    val formattedAvgConsumption =
                        viewModel.calculatedAverageConsumptionResult?.format(2)
                    val formattedTotalCost = viewModel.calculatedTotalCostResult?.format(2)
                    val formattedFuelNeeded = viewModel.calculatedFuelNeededResult?.format(2)
                    val formattedDistance = viewModel.calculatedDistanceResult?.format(2)
                    val formattedFuelSpent = viewModel.calculatedFuelSpentResult?.format(2)


                    val generalErrorText = "(некорректный ввод)"
                    val costErrorText = when {
                        formattedTotalCost != null -> ""
                        priceIsZero -> ""
                        !priceIsEntered -> "(цена не указана)"
                        else -> "(неверные данные для расчета стоимости)"
                    }

                    when (viewModel.calculationMode) {
                        CalculationMode.CONSUMPTION_AND_COST -> {
                            ResultRow(
                                label = "Средний расход:",
                                value = formattedAvgConsumption?.let { "$it ${viewModel.selectedFuelUnit.perUnitName}/100 ${viewModel.selectedDistanceUnit.shortName}" }
                                    ?: "",
                                isError = formattedAvgConsumption == null,
                                errorText = generalErrorText
                            )
                            ResultRow(
                                label = "Общая стоимость:",
                                value = formattedTotalCost?.let { "$it ${viewModel.selectedCurrency.symbol}" }
                                    ?: (if (priceIsZero) "0.00 ${viewModel.selectedCurrency.symbol}" else ""),
                                isError = formattedTotalCost == null && !priceIsZero,
                                errorText = costErrorText
                            )
                            ResultRow(
                                label = "Израсходовано топлива (ввод):",
                                value = "${viewModel.fuelUsed.text} ${viewModel.selectedFuelUnit.displayName.lowercase()}"
                            )
                            ResultRow(
                                label = "Пройденное расстояние (ввод):",
                                value = "${viewModel.distance.text} ${viewModel.selectedDistanceUnit.shortName}"
                            )
                        }

                        CalculationMode.AVERAGE_CONSUMPTION_GIVEN -> {
                            ResultRow(
                                label = "Необходимо топлива:",
                                value = formattedFuelNeeded?.let { "$it ${viewModel.selectedFuelUnit.displayName.lowercase()}" }
                                    ?: "",
                                isError = formattedFuelNeeded == null,
                                errorText = generalErrorText
                            )
                            ResultRow(
                                label = "Общая стоимость поездки:",
                                value = formattedTotalCost?.let { "$it ${viewModel.selectedCurrency.symbol}" }
                                    ?: (if (priceIsZero) "0.00 ${viewModel.selectedCurrency.symbol}" else ""),
                                isError = formattedTotalCost == null && !priceIsEntered,
                                errorText = costErrorText
                            )
                            ResultRow(
                                label = "Средний расход (ввод):",
                                value = viewModel.averageConsumptionInput.text.takeIf { it.isNotBlank() }
                                    ?.let {
                                        "$it ${viewModel.selectedFuelUnit.perUnitName}/100 ${viewModel.selectedDistanceUnit.shortName}"
                                    } ?: "",
                                isError = viewModel.averageConsumptionInput.text.isBlank(),
                                errorText = "(не указан)"
                            )
                            ResultRow(
                                label = "Пройденное расстояние (ввод):",
                                value = "${viewModel.distance.text} ${viewModel.selectedDistanceUnit.shortName}"
                            )
                        }

                        CalculationMode.ODOMETER_TRIP -> {
                            ResultRow(
                                label = "Пройденное расстояние:",
                                value = formattedDistance?.let { "$it ${viewModel.selectedDistanceUnit.shortName}" }
                                    ?: "",
                                isError = formattedDistance == null,
                                errorText = generalErrorText
                            )
                            ResultRow(
                                label = "Потрачено топлива:",
                                value = formattedFuelSpent?.let { "$it ${viewModel.selectedFuelUnit.displayName.lowercase()}" }
                                    ?: "",
                                isError = formattedFuelSpent == null,
                                errorText = generalErrorText
                            )
                            ResultRow(
                                label = "Средний расход:",
                                value = formattedAvgConsumption?.let { "$it ${viewModel.selectedFuelUnit.perUnitName}/100 ${viewModel.selectedDistanceUnit.shortName}" }
                                    ?: "",
                                isError = formattedAvgConsumption == null,
                                errorText = generalErrorText
                            )
                            ResultRow(
                                label = "Общая стоимость:",
                                value = formattedTotalCost?.let { "$it ${viewModel.selectedCurrency.symbol}" }
                                    ?: (if (priceIsZero) "0.00 ${viewModel.selectedCurrency.symbol}" else ""),
                                isError = formattedTotalCost == null && !priceIsEntered,
                                errorText = costErrorText
                            )
                            Text(
                                "Исходные данные:",
                                style = MaterialTheme.typography.headlineSmall,
                                color = TextPrimary,
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                            )
                            ResultRow(
                                label = "Одометр (начало):",
                                value = "${viewModel.odometerStart.text} ${viewModel.selectedDistanceUnit.shortName}"
                            )
                            ResultRow(
                                label = "Одометр (конец):",
                                value = "${viewModel.odometerEnd.text} ${viewModel.selectedDistanceUnit.shortName}"
                            )
                            ResultRow(
                                label = "Топливо (начало):",
                                value = "${viewModel.fuelInTankStart.text} ${viewModel.selectedFuelUnit.displayName.lowercase()}"
                            )
                            ResultRow(
                                label = "Топливо (конец):",
                                value = "${viewModel.fuelInTankEnd.text} ${viewModel.selectedFuelUnit.displayName.lowercase()}"
                            )
                        }
                    }

                    if (viewModel.calculationMode != CalculationMode.ODOMETER_TRIP) {
                        Text(
                            "Исходные данные:",
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                    ResultRow(
                        label = "Цена за ${viewModel.selectedFuelUnit.perUnitName}:",
                        value = "${(priceText.ifEmpty { "0" })} ${viewModel.selectedCurrency.symbol}"
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND)
                            shareIntent.type = "text/plain"
                            shareIntent.putExtra(
                                Intent.EXTRA_TEXT,
                                viewModel.getShareableResultText()
                            )
                            context.startActivity(
                                Intent.createChooser(
                                    shareIntent,
                                    "Поделиться результатом"
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = TextPrimary
                        ),
                        border = BorderStroke(
                            width = 2.dp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(BorderLight, BorderLight)
                            )
                        ),
                        shape = MaterialTheme.shapes.medium,
                        contentPadding = PaddingValues(vertical = 16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Share,
                                contentDescription = "Поделиться",
                                tint = TextPrimary
                            )
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
                                colors = listOf(Orange400, Rose500)
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
                    containerColor = Color.Transparent,
                    contentColor = TextPrimary
                ),
                border = BorderStroke(
                    width = 2.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(BorderLight, BorderLight)
                    )
                ),
                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painterResource(id = R.drawable.ic_home),
                        contentDescription = "Main Menu",
                        tint = TextPrimary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Главное меню", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
