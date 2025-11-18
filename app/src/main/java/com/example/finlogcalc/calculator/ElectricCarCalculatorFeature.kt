package com.example.finlogcalc.calculator

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import com.example.finlogcalc.calculator.ui.ElectricCarCalculatorTheme
import com.example.finlogcalc.calculator.ui.ElectricCarInputScreen
import com.example.finlogcalc.calculator.ui.ElectricCarResultScreen
import java.util.Locale

enum class ElectricChargeMode(val title: String) {
    CHARGE_TO_LEVEL("Зарядка до уровня"),
    CHARGE_BY_TIME("Зарядка по времени")
}

// --- ViewModel ---
class ElectricCarCalculatorViewModel : ViewModel() {
    var batteryCapacity by mutableStateOf(TextFieldValue("")) // kWh
    var chargingPower by mutableStateOf(TextFieldValue(""))   // kW

    var currentChargeMode by mutableStateOf(ElectricChargeMode.CHARGE_TO_LEVEL)

    // Mode: CHARGE_TO_LEVEL
    var startChargePercent by mutableStateOf(TextFieldValue("0")) // %
    var endChargePercent by mutableStateOf(TextFieldValue("100")) // %

    // Mode: CHARGE_BY_TIME
    var chargeTimeHours by mutableStateOf(TextFieldValue("")) // hours

    // Optional fields
    var energyConsumption by mutableStateOf(TextFieldValue("")) // kWh/100km
    var pricePerKwh by mutableStateOf(TextFieldValue(""))
    var selectedCurrency by mutableStateOf(Currency.RUB) // Assuming Currency.RUB is accessible

    // Validation states
    var batteryCapacityError by mutableStateOf(false)
    var chargingPowerError by mutableStateOf(false)
    var startChargePercentError by mutableStateOf(false)
    var endChargePercentError by mutableStateOf(false)
    var chargeTimeHoursError by mutableStateOf(false)
    var energyConsumptionError by mutableStateOf(false)
    var pricePerKwhError by mutableStateOf(false)

    // Results
    var calculatedChargingTimeHours by mutableStateOf<Double?>(null)
    var calculatedFinalChargeLevelPercent by mutableStateOf<Double?>(null)
    var calculatedTotalCost by mutableStateOf<Double?>(null)
    var calculatedRangeKm by mutableStateOf<Double?>(null)

    fun calculate(): Boolean {
        // Reset errors
        batteryCapacityError = false
        chargingPowerError = false
        startChargePercentError = false
        endChargePercentError = false
        chargeTimeHoursError = false
        energyConsumptionError = false
        pricePerKwhError = false

        calculatedChargingTimeHours = null
        calculatedFinalChargeLevelPercent = null
        calculatedTotalCost = null
        calculatedRangeKm = null

        val capacity = batteryCapacity.text.toDoubleOrNull()
        val power = chargingPower.text.toDoubleOrNull()
        val price = pricePerKwh.text.toDoubleOrNull()
        val consumption = energyConsumption.text.toDoubleOrNull()

        var hasError = false

        if (capacity == null || capacity <= 0) {
            batteryCapacityError = true
            hasError = true
        }
        if (power == null || power <= 0) {
            chargingPowerError = true
            hasError = true
        }

        if (currentChargeMode == ElectricChargeMode.CHARGE_TO_LEVEL) {
            val startPercent = startChargePercent.text.toDoubleOrNull()
            val endPercent = endChargePercent.text.toDoubleOrNull()

            if (startPercent == null || startPercent < 0 || startPercent > 100) {
                startChargePercentError = true
                hasError = true
            }
            if (endPercent == null || endPercent < 0 || endPercent > 100) {
                endChargePercentError = true
                hasError = true
            }
            if (startPercent != null && endPercent != null && startPercent >= endPercent) {
                startChargePercentError = true
                endChargePercentError = true
                hasError = true
            }

        } else { // CHARGE_BY_TIME
            val time = chargeTimeHours.text.toDoubleOrNull()
            if (time == null || time <= 0) {
                chargeTimeHoursError = true
                hasError = true
            }
        }

        // Optional fields validation
        if (energyConsumption.text.isNotBlank() && (consumption == null || consumption <= 0)) {
            energyConsumptionError = true
            hasError = true
        }
        if (pricePerKwh.text.isNotBlank() && price == null) {
            pricePerKwhError = true
            hasError = true
        }

        if (hasError) {
            return false
        }

        // Perform calculations only if no errors
        if (capacity != null && power != null) {
            if (currentChargeMode == ElectricChargeMode.CHARGE_TO_LEVEL) {
                val startPercent = startChargePercent.text.toDouble()
                val endPercent = endChargePercent.text.toDouble()

                val energyToCharge = capacity * ((endPercent - startPercent) / 100.0)
                calculatedChargingTimeHours = energyToCharge / power

                if (price != null) {
                    calculatedTotalCost = energyToCharge * price
                }
                if (consumption != null && consumption > 0) {
                    calculatedRangeKm = (capacity * (endPercent / 100.0)) / (consumption / 100.0)
                }

            } else { // CHARGE_BY_TIME
                val time = chargeTimeHours.text.toDouble()
                val energyCharged = power * time
                var finalChargePercentCalculated = (energyCharged / capacity) * 100.0
                finalChargePercentCalculated = finalChargePercentCalculated.coerceAtMost(100.0)
                calculatedFinalChargeLevelPercent = finalChargePercentCalculated

                if (price != null) {
                    calculatedTotalCost = energyCharged * price
                }
                if (consumption != null && consumption > 0) {
                    calculatedRangeKm = (capacity * (finalChargePercentCalculated / 100.0)) / (consumption / 100.0)
                }
            }
        }
        return true
    }

    fun getShareableResultText(): String {
        val sb = StringBuilder()
        sb.append("Результаты расчета зарядки электромобиля:\n\n")
        sb.append("Режим расчета: ${currentChargeMode.title}\n")

        if (currentChargeMode == ElectricChargeMode.CHARGE_TO_LEVEL) {
            calculatedChargingTimeHours?.let {
                sb.append("Время зарядки: ${String.format(Locale.US, "%.2f", it)} ч\n")
            } ?: sb.append("Время зарядки: ${if (batteryCapacityError || chargingPowerError || startChargePercentError || endChargePercentError) "(ошибка ввода)" else "(не рассчитано)"}\n")
        } else { // CHARGE_BY_TIME
            calculatedFinalChargeLevelPercent?.let {
                sb.append("Конечный уровень заряда: ${String.format(Locale.US, "%.1f", it)}%\n")
            } ?: sb.append("Конечный уровень заряда: ${if (batteryCapacityError || chargingPowerError || chargeTimeHoursError) "(ошибка ввода)" else "(не рассчитано)"}\n")
        }

        calculatedTotalCost?.let {
            sb.append("Стоимость зарядки: ${String.format(Locale.US, "%.2f", it)} ${selectedCurrency.symbol}\n")
        } ?: if (pricePerKwh.text.toDoubleOrNull() == 0.0 && pricePerKwh.text.isNotBlank() && !pricePerKwhError) {
            sb.append("Стоимость зарядки: 0.00 ${selectedCurrency.symbol}\n")
        } else if (pricePerKwhError) {
            sb.append("Стоимость зарядки: (ошибка ввода цены)\n")
        } else if (pricePerKwh.text.isNotBlank()){
            sb.append("Стоимость зарядки: (неверные данные для расчета стоимости)\n")
        } else {
            sb.append("Стоимость зарядки: (цена не указана)\n")
        }
        
        calculatedRangeKm?.let {
            sb.append("Запас хода: ${String.format(Locale.US, "%.0f", it)} км\n")
        } ?: if (energyConsumptionError) {
             sb.append("Запас хода: (ошибка ввода расхода)\n")
        } else if (energyConsumption.text.isNotBlank()){
             sb.append("Запас хода: (не рассчитано)\n")
        } else {
             sb.append("Запас хода: (расход не указан)\n")
        }

        sb.append("\nИсходные данные:\n")
        sb.append("Емкость батареи: ${batteryCapacity.text.ifEmpty {"(не указана)"}} кВт·ч ${if (batteryCapacityError) "(ошибка)" else ""}\n")
        sb.append("Мощность зарядки: ${chargingPower.text.ifEmpty {"(не указана)"}} кВт ${if (chargingPowerError) "(ошибка)" else ""}\n")

        if (currentChargeMode == ElectricChargeMode.CHARGE_TO_LEVEL) {
            sb.append("Начальный уровень заряда (ввод): ${startChargePercent.text}% ${if (startChargePercentError) "(ошибка)" else ""}\n")
            sb.append("Конечный уровень заряда (ввод): ${endChargePercent.text}% ${if (endChargePercentError) "(ошибка)" else ""}\n")
        } else { // CHARGE_BY_TIME
            sb.append("Время зарядки (ввод): ${chargeTimeHours.text.ifEmpty {"(не указано)"}} ч ${if (chargeTimeHoursError) "(ошибка)" else ""}\n")
        }

        if (energyConsumption.text.isNotBlank()) {
            sb.append("Расход энергии (ввод): ${energyConsumption.text} кВт·ч/100км ${if (energyConsumptionError) "(ошибка)" else ""}\n")
        } else {
            sb.append("Расход энергии (ввод): (не указан)\n")
        }
        sb.append("Цена кВт·ч (ввод): ${pricePerKwh.text.ifEmpty {"(не указана)"}} ${selectedCurrency.symbol} ${if (pricePerKwhError) "(ошибка)" else ""}\n")
        return sb.toString()
    }
}

// --- Navigation for this feature ---
sealed class ElectricCarCalculatorScreenRoute(val route: String) {
    object Input : ElectricCarCalculatorScreenRoute("electric_car_input")
    object Result : ElectricCarCalculatorScreenRoute("electric_car_result")
}

// --- Main NavHost for the Electric Car Calculator Feature ---
@Composable
fun ElectricCarCalculatorFeatureNavHost(
    mainNavController: NavController,
    scaffoldPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val calculatorViewModel: ElectricCarCalculatorViewModel = viewModel()
    val internalNavController = rememberNavController()

    ElectricCarCalculatorTheme { // Apply the new theme here
        NavHost(
            navController = internalNavController,
            startDestination = ElectricCarCalculatorScreenRoute.Input.route,
            modifier = modifier
        ) {
            composable(ElectricCarCalculatorScreenRoute.Input.route) {
                ElectricCarInputScreen(
                    navController = internalNavController,
                    viewModel = calculatorViewModel,
                    mainAppNavController = mainNavController,
                    scaffoldPadding = scaffoldPadding,
                    modifier = Modifier.fillMaxSize()
                )
            }
            composable(ElectricCarCalculatorScreenRoute.Result.route) {
                ElectricCarResultScreen(
                    navController = internalNavController,
                    viewModel = calculatorViewModel,
                    mainAppNavController = mainNavController,
                    scaffoldPadding = scaffoldPadding,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
