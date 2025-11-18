package com.example.finlogcalc.calculator.alcoholcalculator.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

@Composable
fun AlcoholDrinksInputScreen(
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
                text = "Выпитые напитки:",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(viewModel.consumedDrinksList) { index, drink ->
                    DrinkInputCard(
                        drink = drink,
                        viewModel = viewModel,
                        index = index,
                        onRemove = { viewModel.removeDrink(index) }
                    )
                }
            }

            Button(
                onClick = { viewModel.addDrink() },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepNeonCardBackground,
                    contentColor = Color.White
                ),
                border = BorderStroke(1.dp, DeepNeonPurple.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить напиток")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Добавить еще напиток")
            }

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
                    onClick = { navController.navigate(AlcoholCalculatorScreenRoute.AdditionalParams.route) },
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
                            Text("Далее", color = Color.White, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = "Далее", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrinkInputCard(
    drink: ConsumedDrink,
    viewModel: AlcoholCalculatorViewModel,
    index: Int,
    onRemove: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DeepNeonCardBackground)
            .border(1.dp, DeepNeonBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Название напитка
        Column {
            Text("Название:", color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(bottom = 8.dp))
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = drink.selectedDrink.name,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DeepNeonBackground.copy(alpha = 0.5f),
                        unfocusedContainerColor = DeepNeonBackground.copy(alpha = 0.5f),
                        focusedBorderColor = DeepNeonPink,
                        unfocusedBorderColor = DeepNeonBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                    ),
                    textStyle = TextStyle(fontSize = 16.sp)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(DeepNeonCardBackground)
                ) {
                    viewModel.availableDrinks.forEach { drinkType ->
                        DropdownMenuItem(
                            text = { Text(drinkType.name, color = Color.White) },
                            onClick = {
                                viewModel.updateDrinkType(index, drinkType)
                                expanded = false
                            },
                            modifier = Modifier.background(DeepNeonCardBackground)
                        )
                    }
                }
            }
        }

        // Объем
        Column {
            Text("Объем (мл):", color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(bottom = 8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = drink.volumeMl,
                    onValueChange = { viewModel.updateDrinkVolume(index, it) },
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DeepNeonPurple.copy(alpha = 0.2f))
                                .padding(8.dp)
                        ) {
                            Icon(Icons.Default.LocalBar, contentDescription = "Объем", tint = DeepNeonPurple)
                        }
                    },
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
                    textStyle = TextStyle(fontSize = 16.sp)
                )

                if (viewModel.consumedDrinksList.size > 1) {
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DeepNeonCardBackground)
                            .border(1.dp, Color.Red.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = Color.Red.copy(alpha = 0.8f))
                    }
                }
            }
        }
    }
}
