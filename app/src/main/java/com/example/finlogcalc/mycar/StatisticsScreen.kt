package com.example.finlogcalc.mycar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.finlogcalc.R // Added explicit import for R class
import com.example.finlogcalc.ui.theme.FinLogCalcTheme

// --- Data classes for Statistics Screen (Placeholder) ---
data class StatSummaryItem(
    val title: String,
    val value: String
)

// --- Composable for Statistics Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    navController: NavController,
    mainScaffoldPadding: PaddingValues
) {
    // Placeholder data
    val summaryItems = listOf(
        StatSummaryItem(stringResource(R.string.statistics_total_spent), "5,850.25 ₽"),
        StatSummaryItem(stringResource(R.string.statistics_avg_expense), "120.50 ₽"),
        StatSummaryItem(stringResource(R.string.statistics_spent_this_month), "1,230.00 ₽")
    )

    Scaffold(
        modifier = Modifier.padding(mainScaffoldPadding)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding) // Apply padding from this screen's Scaffold
                .padding(16.dp) // Additional content padding
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // Make content scrollable
        ) {
            // Screen Title (Optional, as TopAppBar usually handles this in MainAppScreen)
            // Text("Статистика", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp))

            // Summary Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                summaryItems.forEach {
                    StatSummaryCard(item = it, modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Pie Chart Section
            Text(
                stringResource(R.string.statistics_expenses_by_category),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            ChartPlaceholder(chartType = "PieChart - Расходы по категориям")

            Spacer(modifier = Modifier.height(24.dp))

            // Bar Chart Section
            Text(
                stringResource(R.string.statistics_income_expense_by_month),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            ChartPlaceholder(chartType = "BarChart - Доходы и расходы/мес.")
        }
    }
}

@Composable
fun StatSummaryCard(item: StatSummaryItem, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(IntrinsicSize.Min), // Ensure cards in a row have similar height potential
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(), // Fill width inside the weighted modifier
            horizontalAlignment = Alignment.CenterHorizontally, // Center content in card
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = item.value,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ChartPlaceholder(chartType: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(
                    text = chartType,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "(Placeholder: Integrate charting library here)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}


// --- Preview ---
@Preview(showBackground = true, name = "Statistics Screen Light")
@Composable
fun StatisticsScreenPreview() {
    FinLogCalcTheme(darkTheme = false) {
        val navController = rememberNavController()
        Scaffold {
            StatisticsScreen(navController = navController, mainScaffoldPadding = it)
        }
    }
}

@Preview(showBackground = true, name = "Statistics Screen Dark")
@Composable
fun StatisticsScreenDarkPreview() {
    FinLogCalcTheme(darkTheme = true) {
        val navController = rememberNavController()
        Scaffold {
            StatisticsScreen(navController = navController, mainScaffoldPadding = it)
        }
    }
}
