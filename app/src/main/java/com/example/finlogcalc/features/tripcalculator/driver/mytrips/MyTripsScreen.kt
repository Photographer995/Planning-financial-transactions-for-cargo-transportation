package com.example.finlogcalc.features.tripcalculator.driver.mytrips

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.finlogcalc.features.tripcalculator.driver.DeepNeonTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController

enum class TripStatus {
    COMPLETED, CANCELLED, PLANNED
}

data class TripItem(
    val id: String,
    val name: String,
    val date: String,
    val time: String,
    val distance: String? = null,
    val duration: String? = null,
    val status: TripStatus,
    val iconType: IconType = IconType.CAR
)

enum class IconType {
    CAR, FLAG
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTripsScreen(navController: NavController? = null) {
    val systemUiController = rememberSystemUiController()
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    
    val allTrips = remember {
        listOf(
            TripItem("1", "Рейс в Краснодар", "24.12.2024", "14:00 - 17:30", status = TripStatus.COMPLETED),
            TripItem("2", "Рейс в", "24.12.2024", "14:00 - 17:30", status = TripStatus.CANCELLED),
            TripItem("3", "Доставка ИКЕА", "25.04.2024", "14:00 - 10 мин", distance = "254 км", duration = "3 ч:00 - 30 мин", status = TripStatus.CANCELLED, iconType = IconType.FLAG),
            TripItem("4", "Дополнительный план", "25.04.2024", "14:00 - 10 мин", status = TripStatus.CANCELLED, iconType = IconType.FLAG),
            TripItem("5", "Рейс Рейс", "26.04.2024", "12:00 - 15:00", status = TripStatus.PLANNED),
        )
    }
    
    val filteredTrips = when (selectedTab) {
        0 -> allTrips
        1 -> allTrips.filter { it.status == TripStatus.PLANNED }
        2 -> allTrips.filter { it.status == TripStatus.COMPLETED }
        else -> allTrips
    }.filter { 
        searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true)
    }
    
    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = false
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNeonTheme.createVerticalGradient(DeepNeonTheme.BackgroundGradient))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Заголовок
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                navController?.let {
                    IconButton(onClick = { it.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = DeepNeonTheme.TextNeonWhite
                        )
                    }
                }
                Text(
                    text = "Мои Рейсы",
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
            }
            
            // Поиск
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Поиск по рейсам...",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            // Вкладки
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                TabButton(
                    text = "Все",
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    text = "Запланированные",
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    text = "Завершенные",
                    isSelected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Список рейсов
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredTrips) { trip ->
                    TripCard(trip = trip)
                }
            }
        }
        
        // Плавающая кнопка
        FloatingActionButton(
            onClick = { /* TODO: Navigate to new trip */ },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(64.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(50),
                    spotColor = DeepNeonTheme.NeonCyan.copy(alpha = 0.5f)
                ),
            containerColor = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = DeepNeonTheme.createVerticalGradient(listOf(DeepNeonTheme.NeonCyanDark, DeepNeonTheme.NeonCyan)),
                        shape = RoundedCornerShape(50)
                    )
                    .border(
                        2.dp,
                        DeepNeonTheme.NeonCyan.copy(alpha = 0.6f),
                        RoundedCornerShape(50)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Новый Рейс",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        
        // Текст под кнопкой
        Text(
            text = "Новый Рейс",
            color = DeepNeonTheme.TextNeonWhite,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 80.dp)
        )
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .background(
                DeepNeonTheme.BackgroundDeepBlue.copy(alpha = 0.5f),
                RoundedCornerShape(12.dp)
            )
            .border(1.dp, DeepNeonTheme.BorderNeon, RoundedCornerShape(12.dp))
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = DeepNeonTheme.TextNeonGray
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = DeepNeonTheme.NeonCyan.copy(alpha = 0.7f)
                )
            },
            colors = TextFieldDefaults.colors(
                focusedTextColor = DeepNeonTheme.TextNeonWhite,
                unfocusedTextColor = DeepNeonTheme.TextNeonWhite,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = if (isSelected) {
        DeepNeonTheme.NeonCyan
    } else {
        DeepNeonTheme.TextNeonGray
    }
    
    val glowIntensity by rememberInfiniteTransition(label = "tab glow").animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glowIntensity"
    )
    
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Column {
            Text(
                text = text,
                color = textColor,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            if (isSelected) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(
                            DeepNeonTheme.NeonCyan.copy(alpha = glowIntensity),
                            RoundedCornerShape(1.dp)
                        )
                )
            }
        }
    }
}

@Composable
fun TripCard(trip: TripItem) {
    val statusColor = when (trip.status) {
        TripStatus.COMPLETED -> DeepNeonTheme.NeonGreen
        TripStatus.CANCELLED -> DeepNeonTheme.NeonOrange
        TripStatus.PLANNED -> DeepNeonTheme.NeonCyan
    }
    
    val statusText = when (trip.status) {
        TripStatus.COMPLETED -> "Завершен"
        TripStatus.CANCELLED -> "Отменен"
        TripStatus.PLANNED -> "Запланирован"
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: Open trip details */ }
            .background(
                DeepNeonTheme.BackgroundDeepBlue.copy(alpha = 0.5f),
                RoundedCornerShape(16.dp)
            )
            .border(1.dp, DeepNeonTheme.BorderNeon, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Иконка
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        DeepNeonTheme.NeonCyanDark.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    )
                    .border(1.dp, DeepNeonTheme.NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (trip.iconType) {
                        IconType.CAR -> Icons.Default.DirectionsCar
                        IconType.FLAG -> Icons.Default.Flag
                    },
                    contentDescription = null,
                    tint = DeepNeonTheme.NeonCyan,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = trip.name,
                    color = DeepNeonTheme.TextNeonWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${trip.date} | ${trip.time}",
                    color = DeepNeonTheme.TextNeonGray,
                    fontSize = 14.sp
                )
                trip.distance?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$it | ${trip.duration ?: ""}",
                        color = DeepNeonTheme.TextNeonGray,
                        fontSize = 12.sp
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                StatusBadge(
                    text = statusText,
                    color = statusColor
                )
            }
        }
    }
}

@Composable
fun StatusBadge(
    text: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .background(
                color.copy(alpha = 0.2f),
                RoundedCornerShape(8.dp)
            )
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
