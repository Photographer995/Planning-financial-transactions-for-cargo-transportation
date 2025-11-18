package com.example.finlogcalc.features.tripcalculator.driver.menu

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.AddRoad
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.* 
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.finlogcalc.features.tripcalculator.driver.DeepNeonTheme
import com.example.finlogcalc.features.tripcalculator.driver.DriverTripUiState
import com.example.finlogcalc.features.tripcalculator.driver.DriverTripViewModel
import com.example.finlogcalc.features.tripcalculator.driver.TripStatus
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit

@Composable
fun TripMainMenuScreen(
    navController: NavController,
    viewModel: DriverTripViewModel,
    requestPermissions: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDriving = uiState.tripStatus == TripStatus.IN_PROGRESS

    fun formatTime(totalSeconds: Long): String {
        val hours = TimeUnit.SECONDS.toHours(totalSeconds)
        val minutes = TimeUnit.SECONDS.toMinutes(totalSeconds) % 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    // Анимация фона с медленным движением
    val infiniteTransition = rememberInfiniteTransition(label = "background animation")
    val animatedProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "animatedProgress"
    )

    val systemUiController = rememberSystemUiController()
    val useDarkIcons = false

    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = useDarkIcons
        )
        systemUiController.setNavigationBarColor(
            color = DeepNeonTheme.BackgroundDark,
            darkIcons = useDarkIcons
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNeonTheme.createVerticalGradient(DeepNeonTheme.BackgroundGradient))
            .systemBarsPadding()
    ) {
        // Эффект цифрового шума на фоне (опционально)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            DeepNeonTheme.BackgroundDark.copy(alpha = 0.3f),
                            DeepNeonTheme.BackgroundDark
                        ),
                        radius = 1000f
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp)) 
            StatusIndicator(
                isDriving = isDriving,
                totalTripTimeSeconds = uiState.totalTripTimeSeconds,
                formatTime = ::formatTime
            )

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MenuItemButton(
                        text = "Новый Рейс",
                        icon = Icons.Default.AddRoad,
                        gradientColors = DeepNeonTheme.GradientNewTrip
                    ) { navController.navigate(TripCalculatorDestinations.NEW_TRIP_ROUTE) }
                    MenuItemButton(
                        text = "Мои Рейсы",
                        icon = Icons.AutoMirrored.Filled.List,
                        gradientColors = DeepNeonTheme.GradientMyTrips
                    ) { navController.navigate(TripCalculatorDestinations.MY_TRIPS_ROUTE) }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MenuItemButton(
                        text = "Маршруты",
                        icon = Icons.Default.Map,
                        gradientColors = DeepNeonTheme.GradientRoutes
                    ) { navController.navigate(TripCalculatorDestinations.ROUTES_ROUTE) }
                    MenuItemButton(
                        text = "Отчеты",
                        icon = Icons.AutoMirrored.Filled.ShowChart,
                        gradientColors = DeepNeonTheme.GradientReports
                    ) { navController.navigate(TripCalculatorDestinations.REPORTS_ROUTE) }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}


@Composable
fun StatusIndicator(isDriving: Boolean, totalTripTimeSeconds: Long, formatTime: (Long) -> String) {
    val statusText = if (isDriving) "В рейсе" else "Вне рейса"
    val statusColor = if (isDriving) DeepNeonTheme.NeonGreen else DeepNeonTheme.TextNeonGray
    
    // Анимация пульсации для статуса
    val infiniteTransition = rememberInfiniteTransition(label = "status pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulseAlpha"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = statusColor.copy(alpha = pulseAlpha),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = "Текущий статус:",
                color = DeepNeonTheme.TextNeonGray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )
            Text(
                text = statusText,
                color = statusColor.copy(alpha = pulseAlpha),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            if (isDriving) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Время в пути: ${formatTime(totalTripTimeSeconds)}",
                    color = DeepNeonTheme.TextNeonGray,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun MenuItemButton(
    text: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    // Анимация свечения при наведении/нажатии
    var isPressed by remember { mutableStateOf(false) }
    val glowIntensity by animateFloatAsState(
        targetValue = if (isPressed) 1f else 0.7f,
        animationSpec = tween(durationMillis = 300),
        label = "glowIntensity"
    )
    
    val borderColor = gradientColors.last().copy(alpha = glowIntensity * 0.5f)
    
    Box(
        modifier = Modifier
            .size(160.dp)
            .clickable(onClick = onClick)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = gradientColors.last().copy(alpha = glowIntensity * 0.3f)
            )
            .background(
                brush = DeepNeonTheme.createVerticalGradient(gradientColors),
                shape = RoundedCornerShape(24.dp)
            )
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(24.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = Color.White,
                modifier = Modifier
                    .size(56.dp)
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(50),
                        spotColor = gradientColors.last().copy(alpha = 0.5f)
                    )
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = text,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TripMainMenuScreenPreview_Driving() {
    val context = LocalContext.current
    val previewViewModel = object : DriverTripViewModel(context) {
        private val _mockUiState = MutableStateFlow(DriverTripUiState(tripStatus = TripStatus.IN_PROGRESS, totalTripTimeSeconds = 3661))
        override val uiState: StateFlow<DriverTripUiState> = _mockUiState.asStateFlow()
        override fun startTrip() {}
        override fun endTrip() {}
        override fun requestPermissionsIfNeeded(onPermissionsGranted: () -> Unit, onPermissionsDenied: () -> Unit) {
            onPermissionsGranted()
        }
    }

    TripMainMenuScreen(
        navController = rememberNavController(),
        viewModel = previewViewModel,
        requestPermissions = {}
    )
}

@Preview(showBackground = true)
@Composable
fun TripMainMenuScreenPreview_NotDriving() {
    val context = LocalContext.current
    val previewViewModel = object : DriverTripViewModel(context) {
        private val _mockUiState = MutableStateFlow(DriverTripUiState(tripStatus = TripStatus.NOT_STARTED))
        override val uiState: StateFlow<DriverTripUiState> = _mockUiState.asStateFlow()
        override fun startTrip() {}
        override fun endTrip() {}
        override fun requestPermissionsIfNeeded(onPermissionsGranted: () -> Unit, onPermissionsDenied: () -> Unit) {
            onPermissionsGranted()
        }
    }

    TripMainMenuScreen(
        navController = rememberNavController(),
        viewModel = previewViewModel,
        requestPermissions = {}
    )
}