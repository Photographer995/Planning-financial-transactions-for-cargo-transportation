package com.example.finlogcalc.features.tripcalculator.driver.routes

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

data class RouteItem(
    val id: String,
    val name: String,
    val from: String,
    val distance: String,
    val duration: String,
    val isDaily: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutesScreen(navController: NavController? = null) {
    val systemUiController = rememberSystemUiController()
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    
    val allRoutes = remember {
        listOf(
            RouteItem("1", "Доставка ИКЕА (Ежедневный)", "От: Склад А", "45 км", "≈ 1 час", true),
            RouteItem("2", "Доставка ИКЕА (Ежедневный)", "От: Склад А", "35 км", "≈ 1 час", true),
            RouteItem("3", "Доставка ИКЕА (Ежедневный)", "От: Склад А", "45 км", "≈ 1 час", true),
        )
    }
    
    val filteredRoutes = if (searchQuery.isEmpty()) {
        allRoutes
    } else {
        allRoutes.filter { it.name.contains(searchQuery, ignoreCase = true) }
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
                    text = "Маршруты",
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
                placeholder = "Поиск маршрутов...",
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
                    text = "Сохраненные",
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Список маршрутов
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredRoutes) { route ->
                    RouteCard(route = route)
                }
            }
        }
        
        // Плавающая кнопка
        FloatingActionButton(
            onClick = { /* TODO: Create route */ },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(72.dp)
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
                        brush = DeepNeonTheme.createVerticalGradient(DeepNeonTheme.GradientCreateRoute),
                        shape = RoundedCornerShape(50)
                    )
                    .border(
                        2.dp,
                        DeepNeonTheme.NeonCyan.copy(alpha = 0.6f),
                        RoundedCornerShape(50)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Создать\nМаршрут",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 12.sp
                    )
                }
            }
        }
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
    val borderColor = if (isSelected) {
        DeepNeonTheme.NeonCyan
    } else {
        Color.Transparent
    }
    
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
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
            if (isSelected) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(
                            borderColor.copy(alpha = glowIntensity),
                            RoundedCornerShape(1.dp)
                        )
                )
            }
        }
    }
}

@Composable
fun RouteCard(route: RouteItem) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: Open route details */ }
            .background(
                DeepNeonTheme.BackgroundDeepBlue.copy(alpha = 0.5f),
                RoundedCornerShape(16.dp)
            )
            .border(1.dp, DeepNeonTheme.BorderNeon, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Мини-карта (заглушка)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        DeepNeonTheme.BackgroundDark,
                        RoundedCornerShape(8.dp)
                    )
                    .border(1.dp, DeepNeonTheme.BorderNeon, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Здесь будет мини-карта с маршрутом
            }
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = route.name,
                    color = DeepNeonTheme.TextNeonWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = route.from,
                    color = DeepNeonTheme.TextNeonGray,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${route.distance} | ${route.duration}",
                    color = DeepNeonTheme.NeonGreen.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
            
            Icon(
                imageVector = if (route.isDaily) Icons.Default.CalendarToday else Icons.Default.LocationOn,
                contentDescription = null,
                tint = DeepNeonTheme.NeonCyan.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
