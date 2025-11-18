package com.example.finlogcalc.calculator.alcoholcalculator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Man
import androidx.compose.material.icons.filled.Woman
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

val DeepNeonPink = Color(0xFFEC4899)
val DeepNeonPurple = Color(0xFF8B5CF6)
val DeepNeonBackground = Color(0xFF0F172A)
val DeepNeonCardBackground = Color(0xFF1E293B)
val DeepNeonBorder = DeepNeonPink.copy(alpha = 0.3f)

@Composable
fun AlcoholBasicParamsScreen(
    navController: NavController,
    viewModel: AlcoholCalculatorViewModel,
    mainNavController: NavController,
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
                text = "Введите основные параметры",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            GenderSelector(
                selectedGender = viewModel.gender,
                onGenderSelect = { viewModel.gender = it }
            )

            ParametersInput(viewModel)

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { navController.navigate(AlcoholCalculatorScreenRoute.Drinks.route) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
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
                        Text("Далее к выбору напитков", color = Color.White, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = "Далее", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun GenderSelector(selectedGender: Gender, onGenderSelect: (Gender) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Выберите ваш пол:", color = Color.White.copy(alpha = 0.8f))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            GenderButton(
                gender = Gender.MALE,
                isSelected = selectedGender == Gender.MALE,
                onClick = { onGenderSelect(Gender.MALE) },
                modifier = Modifier.weight(1f)
            )
            GenderButton(
                gender = Gender.FEMALE,
                isSelected = selectedGender == Gender.FEMALE,
                onClick = { onGenderSelect(Gender.FEMALE) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}


@Composable
private fun GenderButton(
    gender: Gender,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) DeepNeonPink else DeepNeonBorder
    val backgroundColor = if (isSelected) DeepNeonPink.copy(alpha = 0.1f) else DeepNeonCardBackground

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(DeepNeonPink, shape = RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Icon(
            imageVector = if (gender == Gender.MALE) Icons.Default.Man else Icons.Default.Woman,
            contentDescription = gender.displayName,
            tint = if (isSelected) DeepNeonPink else Color.White.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = gender.displayName,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun ParametersInput(viewModel: AlcoholCalculatorViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DeepNeonCardBackground)
            .border(1.dp, DeepNeonBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Введите ваши параметры:",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        ParameterTextField(
            value = viewModel.heightCm,
            onValueChange = { viewModel.heightCm = it },
            label = "Рост, см",
            icon = Icons.Default.Man,
            iconColor = DeepNeonPink
        )
        ParameterTextField(
            value = viewModel.weightKg,
            onValueChange = { viewModel.weightKg = it },
            label = "Вес, кг",
            icon = Icons.Default.Man,
            iconColor = DeepNeonPurple
        )
    }
}

@Composable
fun ParameterTextField(
    value: androidx.compose.ui.text.input.TextFieldValue,
    onValueChange: (androidx.compose.ui.text.input.TextFieldValue) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color
) {
    Column {
        Text(label, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconColor.copy(alpha = 0.2f))
                        .padding(8.dp)
                ) {
                    Icon(icon, contentDescription = label, tint = iconColor)
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DeepNeonBackground.copy(alpha = 0.5f),
                unfocusedContainerColor = DeepNeonBackground.copy(alpha = 0.5f),
                focusedBorderColor = iconColor,
                unfocusedBorderColor = DeepNeonBorder,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
             textStyle = TextStyle(fontSize = 16.sp)
        )
    }
}
