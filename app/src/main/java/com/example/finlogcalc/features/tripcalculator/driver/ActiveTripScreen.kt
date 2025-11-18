package com.example.finlogcalc.features.tripcalculator.driver

import androidx.compose.animation.core.*
import androidx.compose.animation.core.spring
import com.example.finlogcalc.features.tripcalculator.driver.AnimatedVisibilityWithGlow
import com.example.finlogcalc.features.tripcalculator.driver.HapticFeedback
import com.example.finlogcalc.features.tripcalculator.driver.HapticType
import com.example.finlogcalc.features.tripcalculator.driver.rememberHapticFeedback
import com.example.finlogcalc.features.tripcalculator.driver.DeepNeonTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.util.concurrent.TimeUnit
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveTripScreen(
    navController: NavController?,
    viewModel: DriverTripViewModel,
    uiState: DriverTripUiState
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val hapticFeedback = rememberHapticFeedback()
    val speedMonitor = remember { SpeedLimitMonitor(context) }
    
    // Мониторинг скорости
    LaunchedEffect(uiState.currentSpeed) {
        if (uiState.tripStatus == TripStatus.IN_PROGRESS) {
            speedMonitor.checkSpeed(uiState.currentSpeed, hapticFeedback)
        }
    }
    
    SideEffect {
        // systemUiController.setStatusBarColor is deprecated. Removed.
    }
    
    fun formatTime(totalSeconds: Long): String {
        val hours = TimeUnit.SECONDS.toHours(totalSeconds)
        val minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) % 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }
    
    val distanceUnit = if (uiState.useKilometers) "км" else "миль"
    val speedUnit = if (uiState.useKilometers) "км/ч" else "миль/ч"
    
    // Вычисляем оставшееся расстояние и время прибытия (заглушка)
    val remainingDistance = 25.3
    val arrivalTime = "15:45"
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNeonTheme.createVerticalGradient(DeepNeonTheme.BackgroundGradient))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Заголовок
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
                    text = "Активный Рейс",
                    fontSize = 24.sp,
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
            
            Text(
                text = "Основная информация",
                fontSize = 14.sp,
                color = DeepNeonTheme.TextNeonGray,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            // Плашка "В ПУТИ"
            InTransitBanner(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Карта (заглушка)
            MapPlaceholder(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Метрики с анимацией
            AnimatedVisibilityWithGlow(
                visible = uiState.tripStatus == TripStatus.IN_PROGRESS || uiState.tripStatus == TripStatus.PAUSED
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AnimatedMetricCard(
                        label = "Скорость",
                        value = uiState.currentSpeed,
                        unit = speedUnit,
                        subtitle = "${"%.1f".format(uiState.distance)} $distanceUnit",
                        modifier = Modifier.weight(1f),
                        color = DeepNeonTheme.NeonCyan
                    )
                    AnimatedMetricCard(
                        label = "Осталось",
                        value = remainingDistance,
                        unit = distanceUnit,
                        modifier = Modifier.weight(1f),
                        color = DeepNeonTheme.NeonOrange
                    )
                    MetricCard(
                        label = "Прибытие",
                        value = arrivalTime,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Кнопки управления
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ControlButton(
                    icon = Icons.Default.Pause,
                    text = "ПАУЗА",
                    onClick = {
                        hapticFeedback.performHaptic(HapticType.MEDIUM_CLICK)
                        viewModel.pauseTrip()
                    },
                    color = DeepNeonTheme.NeonOrange,
                    modifier = Modifier.weight(1f)
                )
                ControlButton(
                    icon = Icons.Default.Stop,
                    text = "ЗАВЕРШИТЬ",
                    onClick = {
                        hapticFeedback.performHaptic(HapticType.HEAVY_CLICK)
                        viewModel.endTrip()
                    },
                    color = DeepNeonTheme.NeonRed,
                    modifier = Modifier.weight(1f)
                )
                ControlButton(
                    icon = Icons.Default.Flag,
                    text = "ЛОГ ОСТАНОВКИ",
                    onClick = {
                        hapticFeedback.performHaptic(HapticType.LIGHT_CLICK)
                        viewModel.logManualStop()
                    },
                    color = DeepNeonTheme.TextNeonGray,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Панель сведений о рейсе
            TripDetailsPanel(
                tripName = "Рейс в Краснодар",
                startTime = "14:00",
                timeInTransit = formatTime(uiState.totalTripTimeSeconds),
                distanceTraveled = "${"%.1f".format(uiState.distance)} $distanceUnit",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Кнопка добавления голосовой заметки
            VoiceNoteButton(
                onClick = { /* TODO: Add voice note */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun InTransitBanner(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave animation")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "waveOffset"
    )
    
    Box(
        modifier = modifier
            .height(48.dp)
            .background(
                brush = DeepNeonTheme.createVerticalGradient(listOf(DeepNeonTheme.NeonCyanDark, DeepNeonTheme.NeonCyan)),
                shape = RoundedCornerShape(12.dp)
            )
            .border(1.dp, DeepNeonTheme.NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = DeepNeonTheme.NeonCyan.copy(alpha = 0.5f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "В ПУТИ",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            // Анимация звуковых волн
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                repeat(5) { index ->
                    val height = (0.3f + (waveOffset + index * 0.2f) % 1f * 0.7f) * 20f
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(height.dp)
                            .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(2.dp))
                    )
                }
            }
        }
    }
}

@Composable
fun MapPlaceholder(modifier: Modifier = Modifier) {
    // Анимация пульсации для карты
    val pulseAlpha by rememberInfiniteTransition(label = "map pulse").animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulseAlpha"
    )
    
    Box(
        modifier = modifier
            .background(DeepNeonTheme.BackgroundDeepBlue, RoundedCornerShape(16.dp))
            .border(1.dp, DeepNeonTheme.BorderNeon.copy(alpha = pulseAlpha), RoundedCornerShape(16.dp))
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = DeepNeonTheme.NeonCyan.copy(alpha = pulseAlpha * 0.3f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Map,
                contentDescription = null,
                tint = DeepNeonTheme.NeonCyan.copy(alpha = pulseAlpha),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Карта с GPS-треком",
                color = DeepNeonTheme.TextNeonGray,
                fontSize = 14.sp
            )
            Text(
                text = "Отслеживание в реальном времени",
                color = DeepNeonTheme.TextNeonGray.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun AnimatedMetricCard(
    label: String,
    value: Double,
    unit: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    color: Color = DeepNeonTheme.NeonCyan
) {
    // Анимация изменения значения
    val animatedValue by animateFloatAsState(
        targetValue = value.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "animatedValue"
    )
    
    // Пульсация свечения
    val glowIntensity by rememberInfiniteTransition(label = "glow pulse").animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glowIntensity"
    )
    
    Column(
        modifier = modifier
            .background(
                DeepNeonTheme.BackgroundDeepBlue.copy(alpha = 0.5f),
                RoundedCornerShape(12.dp)
            )
            .border(1.dp, color.copy(alpha = glowIntensity * 0.5f), RoundedCornerShape(12.dp))
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = color.copy(alpha = glowIntensity * 0.3f)
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = DeepNeonTheme.TextNeonGray,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${animatedValue.toInt()} $unit",
            color = color.copy(alpha = glowIntensity),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            style = androidx.compose.ui.text.TextStyle(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = color.copy(alpha = glowIntensity * 0.5f),
                    blurRadius = 8f
                )
            )
        )
        subtitle?.let {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = it,
                color = DeepNeonTheme.TextNeonGray,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun MetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    Column(
        modifier = modifier
            .background(
                DeepNeonTheme.BackgroundDeepBlue.copy(alpha = 0.5f),
                RoundedCornerShape(12.dp)
            )
            .border(1.dp, DeepNeonTheme.BorderNeon, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            color = DeepNeonTheme.TextNeonGray,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = DeepNeonTheme.TextNeonWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        subtitle?.let {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = it,
                color = DeepNeonTheme.TextNeonGray,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun ControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TripDetailsPanel(
    tripName: String,
    startTime: String,
    timeInTransit: String,
    distanceTraveled: String,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .background(
                DeepNeonTheme.BackgroundDeepBlue.copy(alpha = 0.5f),
                RoundedCornerShape(16.dp)
            )
            .border(1.dp, DeepNeonTheme.BorderNeon, RoundedCornerShape(16.dp))
            .clickable { isExpanded = !isExpanded }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Сведения о рейсе",
                color = DeepNeonTheme.TextNeonWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = DeepNeonTheme.TextNeonGray
            )
        }
        
        if (isExpanded) {
            Spacer(modifier = Modifier.height(12.dp))
            TripDetailRow(
                icon = Icons.Default.Description,
                label = "Название",
                value = tripName
            )
            Spacer(modifier = Modifier.height(8.dp))
            TripDetailRow(
                icon = Icons.Default.Schedule,
                label = "Начало",
                value = startTime
            )
            Spacer(modifier = Modifier.height(8.dp))
            TripDetailRow(
                icon = Icons.Default.Timer,
                label = "В пути",
                value = timeInTransit
            )
            Spacer(modifier = Modifier.height(8.dp))
            TripDetailRow(
                icon = Icons.Default.Route,
                label = "Пройдено",
                value = distanceTraveled
            )
        }
    }
}

@Composable
fun TripDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DeepNeonTheme.NeonCyan.copy(alpha = 0.7f),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "$label:",
                color = DeepNeonTheme.TextNeonGray,
                fontSize = 14.sp
            )
        }
        Text(
            text = value,
            color = DeepNeonTheme.TextNeonWhite,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun VoiceNoteButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glowIntensity by rememberInfiniteTransition(label = "voice button glow").animateFloat(
        initialValue = 0.6f,
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
                spotColor = DeepNeonTheme.NeonCyan.copy(alpha = glowIntensity * 0.5f)
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
                    brush = DeepNeonTheme.createVerticalGradient(listOf(DeepNeonTheme.NeonCyanDark, DeepNeonTheme.NeonGreen)),
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    1.dp,
                    DeepNeonTheme.NeonCyan.copy(alpha = glowIntensity * 0.6f),
                    RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ДОБАВИТЬ ГОЛОСОВУЮ ЗАМЕТКУ",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
