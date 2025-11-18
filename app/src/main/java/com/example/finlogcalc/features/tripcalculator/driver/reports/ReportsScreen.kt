package com.example.finlogcalc.features.tripcalculator.driver.reports

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.finlogcalc.features.tripcalculator.driver.DeepNeonTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(navController: NavController? = null) {
    val systemUiController = rememberSystemUiController()
    var selectedTab by remember { mutableStateOf(0) }
    
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
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Заголовок
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                    text = "Отчёты",
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Вкладки
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TabButton(
                    text = "Обзор",
                    isSelected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    text = "Статистика",
                    isSelected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    text = "Документы",
                    isSelected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            when (selectedTab) {
                0 -> OverviewTab()
                1 -> StatisticsTab()
                2 -> DocumentsTab()
            }
        }
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
fun OverviewTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // График пробега
        MileageChart()
        
        // Сводные карточки
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                icon = Icons.Default.DirectionsCar,
                label = "Общий пробег",
                value = "1,245 км",
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                icon = Icons.Default.Schedule,
                label = "Время в пути",
                value = "35 ч 17 мин",
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                icon = Icons.Default.Speed,
                label = "Средняя скорость",
                value = "38 км/ч",
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Последние отчёты
        Text(
            text = "Последние отчёты",
            color = DeepNeonTheme.TextNeonWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        
        ReportItem(
            title = "Отчёт за Декабрь 2024",
            onClick = { /* TODO: Open report */ }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Кнопка создания отчёта
        GenerateReportButton(
            onClick = { /* TODO: Generate report */ },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun MileageChart() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                DeepNeonTheme.BackgroundDeepBlue.copy(alpha = 0.5f),
                RoundedCornerShape(16.dp)
            )
            .border(1.dp, DeepNeonTheme.BorderNeon, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "Пробег (км)",
                color = DeepNeonTheme.TextNeonWhite,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "за последние 30 дней",
                color = DeepNeonTheme.TextNeonGray,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Здесь будет график
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "График пробега",
                    color = DeepNeonTheme.TextNeonGray,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun SummaryCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
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
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = DeepNeonTheme.NeonCyan,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
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
    }
}

@Composable
fun ReportItem(
    title: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
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
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = DeepNeonTheme.NeonCyan,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = title,
                    color = DeepNeonTheme.TextNeonWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "Скачать",
                tint = DeepNeonTheme.NeonCyan.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun GenerateReportButton(
    onClick: () -> Unit,
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
                    brush = DeepNeonTheme.createVerticalGradient(DeepNeonTheme.GradientGenerateReport),
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    1.dp,
                    DeepNeonTheme.NeonCyan.copy(alpha = glowIntensity * 0.6f),
                    RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Сформировать новый отчёт",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StatisticsTab() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Статистика",
            color = DeepNeonTheme.TextNeonGray,
            fontSize = 16.sp
        )
    }
}

@Composable
fun DocumentsTab() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Документы",
            color = DeepNeonTheme.TextNeonGray,
            fontSize = 16.sp
        )
    }
}
