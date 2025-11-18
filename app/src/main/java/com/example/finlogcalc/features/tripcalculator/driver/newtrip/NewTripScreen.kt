package com.example.finlogcalc.features.tripcalculator.driver.newtrip

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.finlogcalc.features.tripcalculator.driver.DeepNeonTheme
import com.example.finlogcalc.features.tripcalculator.driver.DriverTripViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTripScreen(
    navController: NavController?,
    viewModel: DriverTripViewModel,
    requestPermissions: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val systemUiController = rememberSystemUiController()
    
    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = false
        )
    }
    
    var tripName by remember { mutableStateOf("") }
    var selectedVehicle by remember { mutableStateOf("") }
    var tripPurpose by remember { mutableStateOf("Перевозка пассажиров") }
    var departurePoint by remember { mutableStateOf("") }
    var destinationPoint by remember { mutableStateOf("") }
    var routeType by remember { mutableStateOf("Оптимальный") }
    var addIntermediatePoints by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNeonTheme.createVerticalGradient(DeepNeonTheme.BackgroundGradient))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Заголовок
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { navController?.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = DeepNeonTheme.TextNeonWhite
                    )
                }
                Text(
                    text = "Новый Рейс",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepNeonTheme.TextNeonWhite,
                    modifier = Modifier.weight(1f),
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = DeepNeonTheme.NeonCyan.copy(alpha = 0.5f),
                            blurRadius = 8f
                        )
                    )
                )
                IconButton(onClick = { /* Menu */ }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Меню",
                        tint = DeepNeonTheme.TextNeonWhite
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Основная информация
            SectionTitle("Основная информация")
            Spacer(modifier = Modifier.height(12.dp))
            
            NeonTextField(
                value = tripName,
                onValueChange = { tripName = it },
                placeholder = "Название рейса",
                leadingIcon = Icons.Default.Description,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Пример подсказки
            NeonSuggestionCard(
                text = "Например: \"Рейс в Краснодар\" или \"Доставка ИКЕА\"",
                onClick = { tripName = "Рейс в Краснодар" },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Автомобиль
            SectionTitle("Автомобиль")
            Spacer(modifier = Modifier.height(12.dp))
            
            NeonSelectionCard(
                text = selectedVehicle.ifEmpty { "Выберите автомобиль" },
                icon = Icons.Default.DirectionsCar,
                onClick = { /* TODO: Open vehicle selection */ },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Цель рейса
            SectionTitle("Цель рейса")
            Spacer(modifier = Modifier.height(12.dp))
            
            NeonSelectionCard(
                text = tripPurpose,
                subtitle = "Перевозка пассажиров",
                icon = Icons.Default.DirectionsBus,
                onClick = { /* TODO: Open purpose selection */ },
                trailingIcon = Icons.Default.Info,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Маршрут
            SectionTitle("Маршрут")
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    NeonTextField(
                        value = departurePoint,
                        onValueChange = { departurePoint = it },
                        placeholder = "Начните вводить адрес",
                        leadingIcon = Icons.Default.LocationOn,
                        trailingIcon = Icons.Default.DirectionsWalk,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                // Мини-карта (заглушка)
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            DeepNeonTheme.BackgroundDeepBlue,
                            RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, DeepNeonTheme.BorderNeon, RoundedCornerShape(12.dp))
                ) {
                    // Здесь будет мини-карта
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            NeonTextField(
                value = destinationPoint,
                onValueChange = { destinationPoint = it },
                placeholder = "Начните вводить адрес",
                leadingIcon = Icons.Default.Flag,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Переключатель типа маршрута
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Кратчайший",
                        color = if (routeType == "Кратчайший") DeepNeonTheme.NeonCyan else DeepNeonTheme.TextNeonGray,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Оптимальный",
                        color = if (routeType == "Оптимальный") DeepNeonTheme.NeonCyan else DeepNeonTheme.TextNeonGray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Switch(
                    checked = routeType == "Оптимальный",
                    onCheckedChange = { routeType = if (it) "Оптимальный" else "Кратчайший" },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = DeepNeonTheme.NeonCyan,
                        checkedTrackColor = DeepNeonTheme.NeonCyanDark
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Добавить промежуточные точки
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "+ Добавить промежуточные точки",
                    color = DeepNeonTheme.TextNeonGray,
                    fontSize = 14.sp
                )
                Switch(
                    checked = addIntermediatePoints,
                    onCheckedChange = { addIntermediatePoints = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = DeepNeonTheme.NeonCyan,
                        checkedTrackColor = DeepNeonTheme.NeonCyanDark
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Кнопка "Начать Рейс"
            NeonButton(
                text = "Начать Рейс",
                onClick = {
            Log.d("NewTripScreen", "Start Trip Clicked")
            if (uiState.locationPermissionsGranted && uiState.notificationPermissionGranted) {
                viewModel.startTrip()
                        navController?.popBackStack()
            } else {
                Log.d("NewTripScreen", "Permissions not granted, requesting...")
                requestPermissions()
            }
                },
                gradient = DeepNeonTheme.GradientStartTrip,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.SemiBold,
        color = DeepNeonTheme.TextNeonWhite
    )
}

@Composable
fun NeonTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    var isFocused by remember { mutableStateOf(false) }
    
    val borderColor = if (isFocused) {
        DeepNeonTheme.NeonCyan.copy(alpha = 0.8f)
    } else {
        DeepNeonTheme.BorderNeon
    }
    
    Box(
        modifier = modifier
            .background(
                DeepNeonTheme.BackgroundDeepBlue.copy(alpha = 0.5f),
                RoundedCornerShape(12.dp)
            )
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = DeepNeonTheme.TextNeonGray
                )
            },
            leadingIcon = leadingIcon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = DeepNeonTheme.NeonCyan.copy(alpha = 0.7f)
                    )
                }
            },
            trailingIcon = trailingIcon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = DeepNeonTheme.TextNeonGray
                    )
                }
            },
            colors = TextFieldDefaults.colors(
                focusedTextColor = DeepNeonTheme.TextNeonWhite,
                unfocusedTextColor = DeepNeonTheme.TextNeonWhite,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused },
            singleLine = true
        )
    }
}

@Composable
fun NeonSuggestionCard(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .background(
                DeepNeonTheme.NeonCyanDark.copy(alpha = 0.2f),
                RoundedCornerShape(12.dp)
            )
            .border(1.dp, DeepNeonTheme.NeonCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                color = DeepNeonTheme.NeonGreen.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = DeepNeonTheme.NeonGreen,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun NeonSelectionCard(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    subtitle: String? = null,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .background(
                DeepNeonTheme.BackgroundDeepBlue.copy(alpha = 0.5f),
                RoundedCornerShape(12.dp)
            )
            .border(1.dp, DeepNeonTheme.BorderNeon, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = DeepNeonTheme.NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = text,
                        color = DeepNeonTheme.TextNeonWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    subtitle?.let {
                        Text(
                            text = it,
                            color = DeepNeonTheme.TextNeonGray,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            trailingIcon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = DeepNeonTheme.TextNeonGray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun NeonButton(
    text: String,
    onClick: () -> Unit,
    gradient: List<Color>,
    modifier: Modifier = Modifier
) {
    val glowIntensity by rememberInfiniteTransition(label = "button glow").animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glowIntensity"
    )
    
    Button(
        onClick = onClick,
        modifier = modifier
            .height(56.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = gradient.last().copy(alpha = glowIntensity * 0.5f)
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = DeepNeonTheme.createVerticalGradient(gradient),
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    1.dp,
                    gradient.last().copy(alpha = glowIntensity * 0.6f),
                    RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
