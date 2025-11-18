package com.example.finlogcalc.calculator.alcoholcalculator.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.TextFieldValue
import java.util.*
import kotlin.math.max
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Man
import androidx.compose.material.icons.filled.Woman
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Restaurant

// --- Enums & Data Classes ---
enum class Gender(val displayName: String, val icon: ImageVector) {
    MALE("Мужчина", Icons.Filled.Man),
    FEMALE("Женщина", Icons.Filled.Woman)
}

enum class StomachFullness(val displayName: String, val icon: ImageVector) {
    EMPTY("На голодную", Icons.Outlined.Restaurant),
    MEDIUM("Средняя сытость", Icons.Filled.RestaurantMenu),
    FULL("Плотно поел", Icons.Filled.Restaurant)
}

data class DrinkType(val name: String, val alcoholPercentage: Double) {
    companion object {
        val list = listOf(
            DrinkType("Безалкогольное пиво (0.5%)", 0.005),
            DrinkType("Кефир старше 3х дней (0.7%)", 0.007),
            DrinkType("Хлебный квас (0.9%)", 0.009),
            DrinkType("Кумыс (1.2%)", 0.012),
            DrinkType("Пиво легкое (4%)", 0.04),
            DrinkType("Пиво обычное/Сидр (5%)", 0.05),
            DrinkType("Пиво портер/темное (6%)", 0.06),
            DrinkType("Слабоалкоголки (7%)", 0.07),
            DrinkType("Пиво крепкое (8%)", 0.08),
            DrinkType("Шампанское (10%)", 0.10),
            DrinkType("Вино (12%)", 0.12),
            DrinkType("Вермут (Martini и пр.) (15%)", 0.15),
            DrinkType("Мягкие ликёры (Baileys и пр.) (17%)", 0.17),
            DrinkType("Портвейн (20%)", 0.20),
            DrinkType("Средние ликёры (Malibu и пр.) (20%)", 0.20),
            DrinkType("Рижский бальзам и т.п. (30%)", 0.30),
            DrinkType("Крепкие ликёры (Jagermeister и т.п.) (35%)", 0.35),
            DrinkType("Текила/Бренди/Бехеровка и т.п. (38%)", 0.38),
            DrinkType("Ром, Джин (39%)", 0.39),
            DrinkType("Водка (40%)", 0.40)
        )
        val default = list.first()
    }
}

data class ConsumedDrink(
    val id: Int = UUID.randomUUID().hashCode(),
    var selectedDrink: DrinkType = DrinkType.default,
    var volumeMl: TextFieldValue = TextFieldValue("500")
)

// --- ViewModel ---
class AlcoholCalculatorViewModel : ViewModel() {
    var gender by mutableStateOf(Gender.MALE)
    var heightCm by mutableStateOf(TextFieldValue("170"))
    var weightKg by mutableStateOf(TextFieldValue("70"))
    var consumedDrinksList = mutableStateListOf(ConsumedDrink())
    val availableDrinks: List<DrinkType> = DrinkType.list
    var stomachFullness by mutableStateOf(StomachFullness.MEDIUM)
    private val calendar: Calendar = Calendar.getInstance()
    var startDay by mutableStateOf(TextFieldValue(calendar.get(Calendar.DAY_OF_MONTH).toString()))
    var startMonth by mutableStateOf(TextFieldValue((calendar.get(Calendar.MONTH) + 1).toString()))
    var startYear by mutableStateOf(TextFieldValue(calendar.get(Calendar.YEAR).toString()))
    var startHour by mutableStateOf(TextFieldValue(calendar.get(Calendar.HOUR_OF_DAY).toString()))
    var startMinute by mutableStateOf(TextFieldValue(calendar.get(Calendar.MINUTE).toString()))
    var endDay by mutableStateOf(TextFieldValue(calendar.get(Calendar.DAY_OF_MONTH).toString()))
    var endMonth by mutableStateOf(TextFieldValue((calendar.get(Calendar.MONTH) + 1).toString()))
    var endYear by mutableStateOf(TextFieldValue(calendar.get(Calendar.YEAR).toString()))
    var endHour by mutableStateOf(TextFieldValue(calendar.get(Calendar.HOUR_OF_DAY).toString()))
    var endMinute by mutableStateOf(TextFieldValue(calendar.get(Calendar.MINUTE).toString()))
    var maxBacResult by mutableStateOf<String?>(null)
    var fullEliminationTimeResult by mutableStateOf<String?>(null)
    var currentBacResult by mutableStateOf<String?>(null)
    var recommendationsResult by mutableStateOf<String?>(null)

    fun addDrink() { consumedDrinksList.add(ConsumedDrink()) }
    fun removeDrink(index: Int) { if (index >= 0 && index < consumedDrinksList.size) { consumedDrinksList.removeAt(index) } }
    fun updateDrinkType(index: Int, newType: DrinkType) { if (index >= 0 && index < consumedDrinksList.size) { consumedDrinksList[index] = consumedDrinksList[index].copy(selectedDrink = newType) } }
    fun updateDrinkVolume(index: Int, newVolume: TextFieldValue) { if (index >= 0 && index < consumedDrinksList.size) { consumedDrinksList[index] = consumedDrinksList[index].copy(volumeMl = newVolume) } }

    fun setConsumptionTimeToNow(isEnd: Boolean) {
        val cal = Calendar.getInstance()
        if (isEnd) {
            endDay = TextFieldValue(cal.get(Calendar.DAY_OF_MONTH).toString())
            endMonth = TextFieldValue((cal.get(Calendar.MONTH) + 1).toString())
            endYear = TextFieldValue(cal.get(Calendar.YEAR).toString())
            endHour = TextFieldValue(cal.get(Calendar.HOUR_OF_DAY).toString())
            endMinute = TextFieldValue(cal.get(Calendar.MINUTE).toString())
        } else {
            startDay = TextFieldValue(cal.get(Calendar.DAY_OF_MONTH).toString())
            startMonth = TextFieldValue((cal.get(Calendar.MONTH) + 1).toString())
            startYear = TextFieldValue(cal.get(Calendar.YEAR).toString())
            startHour = TextFieldValue(cal.get(Calendar.HOUR_OF_DAY).toString())
            startMinute = TextFieldValue(cal.get(Calendar.MINUTE).toString())
        }
    }

    fun setConsumptionEndTimeRelativeToNow(hoursAgo: Int) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.HOUR_OF_DAY, -hoursAgo)
        endDay = TextFieldValue(cal.get(Calendar.DAY_OF_MONTH).toString())
        endMonth = TextFieldValue((cal.get(Calendar.MONTH) + 1).toString())
        endYear = TextFieldValue(cal.get(Calendar.YEAR).toString())
        endHour = TextFieldValue(cal.get(Calendar.HOUR_OF_DAY).toString())
        endMinute = TextFieldValue(cal.get(Calendar.MINUTE).toString())
    }

    fun calculateResults() {
        val weight = weightKg.text.toDoubleOrNull()
        val height = heightCm.text.toDoubleOrNull()
        if (weight == null || weight <= 0 || height == null || height <=0 ) {
            maxBacResult = "Ошибка: неверный вес или рост"
            fullEliminationTimeResult = ""
            currentBacResult = ""
            recommendationsResult = "Пожалуйста, введите корректные данные роста и веса."
            return
        }
        val r = if (gender == Gender.MALE) 0.68 else 0.55
        val beta = 0.015
        var totalAlcoholGrams = 0.0
        consumedDrinksList.forEach { consumedDrink ->
            val volume = consumedDrink.volumeMl.text.toDoubleOrNull() ?: 0.0
            totalAlcoholGrams += volume * consumedDrink.selectedDrink.alcoholPercentage * 0.789
        }
        if (totalAlcoholGrams == 0.0) {
            maxBacResult = "0.00 ‰"
            fullEliminationTimeResult = "Алкоголь не употреблен."
            currentBacResult = "0.00 ‰"
            recommendationsResult = "Можно управлять автомобилем."
            return
        }
        val maxBac = (totalAlcoholGrams / (r * weight)) * 1.0
        val absorptionFactor = when (stomachFullness) {
            StomachFullness.EMPTY -> 1.0
            StomachFullness.MEDIUM -> 0.85
            StomachFullness.FULL -> 0.7
        }
        val effectiveMaxBac = maxBac * absorptionFactor
        maxBacResult = "%.2f ‰".format(Locale.US, effectiveMaxBac)
        val eliminationHours = if (effectiveMaxBac > 0) effectiveMaxBac / beta else 0.0
        fullEliminationTimeResult = "Примерно через %.1f часов".format(Locale.US, eliminationHours)
        val endCal = Calendar.getInstance().apply {
            set(endYear.text.toIntOrNull() ?: calendar.get(Calendar.YEAR),
                (endMonth.text.toIntOrNull() ?: (calendar.get(Calendar.MONTH)+1)) -1,
                endDay.text.toIntOrNull() ?: calendar.get(Calendar.DAY_OF_MONTH),
                endHour.text.toIntOrNull() ?: calendar.get(Calendar.HOUR_OF_DAY),
                endMinute.text.toIntOrNull() ?: calendar.get(Calendar.MINUTE))
        }
        val hoursSinceConsumptionEnd = max(0.0, (System.currentTimeMillis() - endCal.timeInMillis) / (1000.0 * 60 * 60))
        val currentBac = max(0.0, effectiveMaxBac - (beta * hoursSinceConsumptionEnd))
        currentBacResult = "%.2f ‰ (текущий)".format(Locale.US, currentBac)
        recommendationsResult = when {
            currentBac == 0.0 && (hoursSinceConsumptionEnd >= eliminationHours || eliminationHours == 0.0) -> "Можно управлять автомобилем. Алкоголь полностью выведен."
            currentBac <= 0.16 -> "Концентрация близка к допустимой норме (0.16 мг/л выдыхаемого воздуха ~ 0.3‰ крови). Рекомендуется воздержаться от вождения."
            currentBac <= 0.3 -> "Нельзя управлять автомобилем. Допустимая норма превышена."
            else -> "Категорически нельзя управлять автомобилем! Высокая концентрация алкоголя."
        }
    }

    fun getShareableResultText(): String {
        val sb = StringBuilder()
        sb.append("Результаты алкогольного калькулятора:\n")
        maxBacResult?.let { sb.append("Макс. концентрация: $it\n") }
        fullEliminationTimeResult?.let { sb.append("Полное выведение: $it\n") }
        currentBacResult?.let { sb.append("Текущая концентрация: $it\n") }
        recommendationsResult?.let { sb.append("Рекомендации: $it\n\n") }
        sb.append("Введенные данные:\n")
        sb.append("Пол: ${gender.displayName}\n")
        sb.append("Рост: ${heightCm.text} см\n")
        sb.append("Вес: ${weightKg.text} кг\n")
        sb.append("Наполненность желудка: ${stomachFullness.displayName}\n")
        sb.append("Начало употребления: ${startDay.text}.${startMonth.text}.${startYear.text} в ${startHour.text}:${startMinute.text}\n")
        sb.append("Конец употребления: ${endDay.text}.${endMonth.text}.${endYear.text} в ${endHour.text}:${endMinute.text}\n")
        sb.append("Напитки:\n")
        consumedDrinksList.forEach { sb.append("  - ${it.selectedDrink.name}: ${it.volumeMl.text} мл\n") }
        return sb.toString()
    }
}

sealed class AlcoholCalculatorScreenRoute(val route: String) {
    object BasicParams : AlcoholCalculatorScreenRoute("alc_basic_params")
    object Drinks : AlcoholCalculatorScreenRoute("alc_drinks")
    object AdditionalParams : AlcoholCalculatorScreenRoute("alc_additional_params")
    object Results : AlcoholCalculatorScreenRoute("alc_results")
}

@Composable
fun AlcoholCalculatorFeatureNavHost(
    mainNavController: NavController, scaffoldPadding: PaddingValues, modifier: Modifier = Modifier
) {
    val internalNavController = rememberNavController()
    val viewModel: AlcoholCalculatorViewModel = viewModel()
    NavHost(navController = internalNavController, startDestination = AlcoholCalculatorScreenRoute.BasicParams.route, modifier = modifier) {
        composable(AlcoholCalculatorScreenRoute.BasicParams.route) { AlcoholBasicParamsScreen(internalNavController, viewModel, mainNavController, scaffoldPadding, Modifier.fillMaxWidth()) }
        composable(AlcoholCalculatorScreenRoute.Drinks.route) { AlcoholDrinksInputScreen(internalNavController, viewModel, scaffoldPadding, Modifier.fillMaxWidth()) }
        composable(AlcoholCalculatorScreenRoute.AdditionalParams.route) { AlcoholAdditionalParamsScreen(internalNavController, viewModel, scaffoldPadding, Modifier.fillMaxWidth()) }
        composable(AlcoholCalculatorScreenRoute.Results.route) { AlcoholResultsScreen(internalNavController, viewModel, mainNavController, scaffoldPadding, Modifier.fillMaxWidth()) }
    }
}
