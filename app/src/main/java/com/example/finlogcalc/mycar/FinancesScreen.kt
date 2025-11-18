package com.example.finlogcalc.mycar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.finlogcalc.R // Added explicit import for R class
import com.example.finlogcalc.ui.theme.FinLogCalcTheme
// import java.text.SimpleDateFormat // Not strictly needed for the current composable logic if R.string is used
// import java.util.Date // Not strictly needed for the current composable logic if R.string is used
import java.util.Locale
import kotlin.math.abs

// --- Data classes for Finances Screen (Placeholder) ---
data class FinanceSummary(
    val title: String,
    val amount: String,
    val icon: ImageVector,
    val color: Color
)

data class Transaction(
    val id: String,
    val name: String,
    val date: String,
    val amount: Double,
    val isIncome: Boolean,
    val category: String
)

// --- Composable for Finances Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancesScreen(
    navController: NavController, // Keep NavController for potential future navigation from this screen
    mainScaffoldPadding: PaddingValues // To respect padding from parent Scaffold (e.g. in MainActivity)
) {
    // Placeholder data - In a real app, this would come from a ViewModel
    val incomeSummary = FinanceSummary(
        title = stringResource(R.string.finances_income),
        amount = "+1,250.75 ₽",
        icon = Icons.Filled.TrendingUp,
        color = MaterialTheme.colorScheme.tertiary // Example: Greenish for income
    )
    val expenseSummary = FinanceSummary(
        title = stringResource(R.string.finances_expenses),
        amount = "-750.20 ₽",
        icon = Icons.Filled.TrendingDown,
        color = MaterialTheme.colorScheme.error // Example: Reddish for expenses
    )

    val transactions = listOf(
        Transaction("1", "Зарплата", "15.03.2024", 1200.0, true, "Работа"),
        Transaction("2", "Покупка продуктов", "16.03.2024", -50.5, false, "Еда"),
        Transaction("3", "Кофе", "16.03.2024", -5.0, false, "Досуг"),
        Transaction("4", "Подарок от Анны", "17.03.2024", 100.0, true, "Подарки"),
        Transaction("5", "Счет за интернет", "18.03.2024", -25.0, false, "Счета")
    )

    Scaffold(
        modifier = Modifier.padding(mainScaffoldPadding), // Apply padding passed from MainAppScreen
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: Navigate to add finance entry screen */ }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_finance_entry_desc))
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding -> // This innerPadding is from *this* Scaffold (FinancesScreen's Scaffold)
        Column(
            modifier = Modifier
                .padding(innerPadding) // Apply padding from this screen's Scaffold
                .padding(horizontal = 16.dp) // Horizontal padding for content inside the Column
                .fillMaxSize()
        ) {
            // Summary Cards
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), // Padding around the Row
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SummaryCard(summary = incomeSummary, modifier = Modifier.weight(1f))
                SummaryCard(summary = expenseSummary, modifier = Modifier.weight(1f))
            }

            // Transaction List Title
            Text(
                stringResource(R.string.finances_transactions_list_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp, top = 8.dp) // Padding for the title
            )

            // Transaction List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp) // Padding at the bottom of the list
            ) {
                items(transactions, key = { it.id }) { transaction ->
                    TransactionItemCard(transaction = transaction)
                }
            }
        }
    }
}

@Composable
fun SummaryCard(summary: FinanceSummary, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = summary.icon,
                contentDescription = summary.title,
                tint = summary.color,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = summary.title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = summary.amount,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = summary.color
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // For Badge
@Composable
fun TransactionItemCard(transaction: Transaction) {
    val indicatorColor = if (transaction.isIncome) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
    val amountPrefix = if (transaction.isIncome) "+" else "-"
    val amountColor = indicatorColor

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(indicatorColor)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = transaction.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    // Ensure locale-safe formatting for currency if needed, and correct currency symbol
                    text = "$amountPrefix${"%.2f".format(Locale.US, abs(transaction.amount))} ₽",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = amountColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Badge(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Text(
                        text = transaction.category,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

// --- Preview ---
@Preview(showBackground = true, name = "Finances Screen Light")
@Composable
fun FinancesScreenPreviewLight() {
    FinLogCalcTheme(darkTheme = false) {
        val context = LocalContext.current
        val navController = NavController(context)
        Scaffold { paddingValues -> // Add a basic scaffold for preview context
            FinancesScreen(navController = navController, mainScaffoldPadding = paddingValues)
        }
    }
}

@Preview(showBackground = true, name = "Finances Screen Dark")
@Composable
fun FinancesScreenPreviewDark() {
    FinLogCalcTheme(darkTheme = true) {
        val context = LocalContext.current
        val navController = NavController(context)
         Scaffold { paddingValues -> // Add a basic scaffold for preview context
            FinancesScreen(navController = navController, mainScaffoldPadding = paddingValues)
        }
    }
}
