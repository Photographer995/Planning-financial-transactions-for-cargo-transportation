package com.example.finlogcalc.calculator.alcoholcalculator.ui

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun AlcoholResultsScreen(
    navController: NavController,
    viewModel: AlcoholCalculatorViewModel,
    mainNavController: NavController,
    scaffoldPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNeonBackground)
            .padding(scaffoldPadding)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Результаты расчета",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            ResultCard(viewModel)

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
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, viewModel.getShareableResultText())
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
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
                            Icon(Icons.Default.Share, contentDescription = "Поделиться", tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Поделиться", color = Color.White, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun ResultCard(viewModel: AlcoholCalculatorViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DeepNeonCardBackground)
            .border(1.dp, DeepNeonBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ResultRow("Макс. концентрация:", viewModel.maxBacResult, Icons.Default.Info, DeepNeonPink)
        Divider(color = DeepNeonBorder)
        ResultRow("Полное выведение:", viewModel.fullEliminationTimeResult, Icons.Default.Info, DeepNeonPurple)
        Divider(color = DeepNeonBorder)
        ResultRow("Текущая концентрация:", viewModel.currentBacResult, Icons.Default.Info, DeepNeonPink)
        Divider(color = DeepNeonBorder)
        ResultRow("Рекомендации:", viewModel.recommendationsResult, Icons.Default.Info, DeepNeonPurple)
    }
}

@Composable
private fun ResultRow(label: String, value: String?, icon: ImageVector, iconColor: Color) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.padding(top = 4.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
            Text(
                text = value ?: "Нет данных",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
