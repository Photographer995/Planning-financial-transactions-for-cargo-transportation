package com.example.finlogcalc.calculator.alcoholcalculator.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AlcoholAdditionalParamsScreen(
    navController: NavController,
    viewModel: AlcoholCalculatorViewModel,
    scaffoldPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNeonBackground)
            .padding(scaffoldPadding)
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Дополнительные параметры",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            StomachFullnessSelector(
                selectedFullness = viewModel.stomachFullness,
                onFullnessSelect = { viewModel.stomachFullness = it }
            )

            ConsumptionTimeSelector(viewModel)

            Spacer(modifier = Modifier.weight(1f))


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { navController.popBackStack() },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DeepNeonCardBackground,
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, DeepNeonPink.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Назад")
                }
                Button(
                    onClick = {
                        viewModel.calculateResults()
                        navController.navigate(AlcoholCalculatorScreenRoute.Results.route)
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(DeepNeonPink, DeepNeonPurple)
                                )
                            )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("Рассчитать", color = Color.White, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StomachFullnessSelector(
    selectedFullness: StomachFullness,
    onFullnessSelect: (StomachFullness) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Наполненность желудка:", color = Color.White.copy(alpha = 0.8f))
        StomachFullness.values().forEach { fullness ->
            FullnessButton(
                fullness = fullness,
                isSelected = selectedFullness == fullness,
                onClick = { onFullnessSelect(fullness) }
            )
        }
    }
}

@Composable
private fun FullnessButton(
    fullness: StomachFullness,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) DeepNeonPink else DeepNeonBorder
    val backgroundColor = if (isSelected) DeepNeonPink.copy(alpha = 0.1f) else DeepNeonCardBackground

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(DeepNeonPink, shape = RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
        }
        Icon(
            imageVector = fullness.icon,
            contentDescription = fullness.displayName,
            tint = if (isSelected) DeepNeonPink else Color.White.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = fullness.displayName,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun ConsumptionTimeSelector(viewModel: AlcoholCalculatorViewModel) {
    val sdf = SimpleDateFormat("EEEE, d MMMM yyyy г. в HH:mm", Locale("ru"))
    val currentTime = sdf.format(Date())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DeepNeonCardBackground)
            .border(1.dp, DeepNeonBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Schedule, contentDescription = "Время употребления", tint = DeepNeonPurple)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Время употребления",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
        Text(
            text = "Сейчас $currentTime, верно?",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp
        )

        TimeInputRow("Во сколько вы начали пить?", viewModel, isStart = true)
        TimeInputRow("Во сколько вы закончили пить?", viewModel, isStart = false)

        QuickTimeButtons(viewModel)
    }
}

@Composable
private fun TimeInputRow(label: String, viewModel: AlcoholCalculatorViewModel, isStart: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, color = Color.White.copy(alpha = 0.7f))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TimeTextField(
                modifier = Modifier.weight(1f),
                value = if (isStart) viewModel.startDay else viewModel.endDay,
                onValueChange = { if (isStart) viewModel.startDay = it else viewModel.endDay = it },
                placeholder = "ДД"
            )
            TimeTextField(
                modifier = Modifier.weight(1f),
                value = if (isStart) viewModel.startMonth else viewModel.endMonth,
                onValueChange = { if (isStart) viewModel.startMonth = it else viewModel.endMonth = it },
                placeholder = "ММ"
            )
            TimeTextField(
                modifier = Modifier.weight(1.5f),
                value = if (isStart) viewModel.startYear else viewModel.endYear,
                onValueChange = { if (isStart) viewModel.startYear = it else viewModel.endYear = it },
                placeholder = "ГГГГ"
            )
            TimeTextField(
                modifier = Modifier.weight(1f),
                value = if (isStart) viewModel.startHour else viewModel.endHour,
                onValueChange = { if (isStart) viewModel.startHour = it else viewModel.endHour = it },
                placeholder = "ЧЧ"
            )
            TimeTextField(
                modifier = Modifier.weight(1f),
                value = if (isStart) viewModel.startMinute else viewModel.endMinute,
                onValueChange = { if (isStart) viewModel.startMinute = it else viewModel.endMinute = it },
                placeholder = "ММ"
            )
        }
    }
}

@Composable
fun TimeTextField(
    modifier: Modifier = Modifier,
    value: androidx.compose.ui.text.input.TextFieldValue,
    onValueChange: (androidx.compose.ui.text.input.TextFieldValue) -> Unit,
    placeholder: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = { Text(placeholder, color = Color.White.copy(alpha = 0.5f), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = DeepNeonBackground.copy(alpha = 0.5f),
            unfocusedContainerColor = DeepNeonBackground.copy(alpha = 0.5f),
            focusedBorderColor = DeepNeonPurple,
            unfocusedBorderColor = DeepNeonBorder,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        textStyle = TextStyle(textAlign = TextAlign.Center, fontSize = 16.sp)
    )
}

@Composable
private fun QuickTimeButtons(viewModel: AlcoholCalculatorViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Быстрое время окончания:", color = Color.White.copy(alpha = 0.7f))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickTimeButton(
                text = "Топ",
                hoursAgo = 0,
                viewModel = viewModel,
                modifier = Modifier.weight(1f),
                color1 = DeepNeonPurple,
                color2 = DeepNeonPurple.copy(alpha = 0.7f)
            )
            QuickTimeButton(
                text = "Час",
                hoursAgo = 1,
                viewModel = viewModel,
                modifier = Modifier.weight(1f),
                color1 = DeepNeonPink,
                color2 = DeepNeonPink.copy(alpha = 0.7f)
            )
            QuickTimeButton(
                text = "2ч",
                hoursAgo = 2,
                viewModel = viewModel,
                modifier = Modifier.weight(1f),
                color1 = DeepNeonPurple,
                color2 = DeepNeonPink
            )
        }
    }
}

@Composable
private fun QuickTimeButton(
    text: String,
    hoursAgo: Int,
    viewModel: AlcoholCalculatorViewModel,
    modifier: Modifier = Modifier,
    color1: Color,
    color2: Color
) {
    Button(
        onClick = { viewModel.setConsumptionEndTimeRelativeToNow(hoursAgo) },
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = Brush.horizontalGradient(colors = listOf(color1, color2)))
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Update, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Text(text, color = Color.White, fontSize = 12.sp)
            }
        }
    }
}
