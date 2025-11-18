package com.example.finlogcalc.mycar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.finlogcalc.R
import com.example.finlogcalc.ui.theme.FinLogCalcTheme

// --- Data classes for Reminders Screen (Placeholder) ---
enum class ReminderPriority(val displayName: String, val color: Color, val icon: ImageVector) {
    HIGH("Высокий", Color(0xFFE53935), Icons.Outlined.WarningAmber), // Red
    MEDIUM("Средний", Color(0xFFFFB300), Icons.Outlined.CalendarToday), // Amber
    LOW("Низкий", Color(0xFF4CAF50), Icons.Outlined.CalendarToday) // Green
}

data class Reminder(
    val id: String,
    val title: String,
    val description: String? = null,
    val date: String,
    val category: String,
    val priority: ReminderPriority,
    var isCompleted: Boolean = false
)

// --- Composable for Reminders Screen ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    navController: NavController,
    mainScaffoldPadding: PaddingValues
) {
    // Placeholder data
    val activeReminders = listOf(
        Reminder("1", "Оплатить счета", "Интернет и коммунальные услуги", "20.03.2024", "Счета", ReminderPriority.HIGH),
        Reminder("2", "Запись к врачу", "Стоматолог, осмотр", "22.03.2024", "Здоровье", ReminderPriority.MEDIUM),
        Reminder("3", "Купить продукты", null, "Завтра", "Покупки", ReminderPriority.LOW)
    )
    val completedReminders = listOf(
        Reminder("4", "Позвонить маме", null, "15.03.2024", "Семья", ReminderPriority.MEDIUM, true),
        Reminder("5", "Забрать посылку", "Книги из онлайн-магазина", "14.03.2024", "Покупки", ReminderPriority.LOW, true)
    )

    Scaffold(
        modifier = Modifier.padding(mainScaffoldPadding),
        floatingActionButton = {
            FloatingActionButton(onClick = { /* TODO: Navigate to add reminder screen */ }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_reminder_desc))
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding) // Apply padding from this screen's Scaffold
                .padding(horizontal = 16.dp) // Horizontal padding for content
                .fillMaxSize(),
            contentPadding = PaddingValues(vertical = 16.dp), // Vertical padding for LazyColumn content
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Active Reminders Section
            item {
                Text(
                    stringResource(R.string.reminders_active_section), // "Активные"
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(activeReminders, key = { it.id }) { reminder ->
                ReminderItemCard(reminder = reminder)
            }

            // Completed Reminders Section
            if (completedReminders.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.reminders_completed_section), // "Выполненные"
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                items(completedReminders, key = { it.id }) { reminder ->
                    ReminderItemCard(reminder = reminder)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderItemCard(reminder: Reminder) {
    val cardAlpha = if (reminder.isCompleted) 0.6f else 1f
    val titleTextStyle = if (reminder.isCompleted) {
        MaterialTheme.typography.titleMedium.copy(textDecoration = TextDecoration.LineThrough, fontWeight = FontWeight.Normal)
    } else {
        MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
    }
    val contentColor = if (reminder.isCompleted) {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val iconToShow = if (reminder.isCompleted) Icons.Filled.CheckCircle else reminder.priority.icon
    val iconColor = if (reminder.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else reminder.priority.color

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(alpha = cardAlpha), // Apply transparency for completed items
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = iconToShow,
                contentDescription = reminder.priority.displayName,
                tint = iconColor,
                modifier = Modifier.size(24.dp).padding(top = 2.dp) // Align icon slightly with title
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.title,
                    style = titleTextStyle,
                    color = contentColor
                )
                if (reminder.description != null) {
                    Text(
                        text = reminder.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor.copy(alpha = if (reminder.isCompleted) 0.7f else 0.8f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Text(
                    text = reminder.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = if (reminder.isCompleted) 0.8f else 0.7f),
                    modifier = Modifier.padding(top = 6.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = if (reminder.isCompleted) 0.7f else 1f)
                    ) {
                        Text(
                            text = reminder.category,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp
                        )
                    }
                    Badge(
                        containerColor = reminder.priority.color.copy(alpha = if (reminder.isCompleted) 0.3f else 0.2f),
                        contentColor = reminder.priority.color.copy(alpha = if (reminder.isCompleted) 0.7f else 1f).let { if (MaterialTheme.colorScheme.isLight) it.darken(0.1f) else it.lighten(0.1f) } // Ensure contrast
                    ) {
                        Text(
                            text = reminder.priority.displayName,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// Helper extension for Color to darken/lighten (simplified)
fun Color.darken(factor: Float = 0.2f): Color {
    return Color(red * (1 - factor), green * (1 - factor), blue * (1 - factor), alpha)
}
fun Color.lighten(factor: Float = 0.2f): Color {
    return Color( (red * (1-factor)) + factor, (green * (1-factor)) + factor, (blue * (1-factor)) + factor, alpha).coerceIn(0f,1f)
}
fun Color.coerceIn(min:Float, max:Float):Color = Color(red.coerceIn(min,max), green.coerceIn(min,max), blue.coerceIn(min,max), alpha)
val ColorScheme.isLight: Boolean @Composable get() = !this.surface.luminanceIsLow()
fun Color.luminanceIsLow(threshold: Float = 0.5f): Boolean = (0.2126f * red + 0.7152f * green + 0.0722f * blue) < threshold


// --- Preview ---
@Preview(showBackground = true, name = "Reminders Screen Light")
@Composable
fun RemindersScreenPreview() {
    FinLogCalcTheme(darkTheme = false) {
        val navController = rememberNavController()
        Scaffold {
            RemindersScreen(navController = navController, mainScaffoldPadding = it)
        }
    }
}

@Preview(showBackground = true, name = "Reminders Screen Dark")
@Composable
fun RemindersScreenDarkPreview() {
    FinLogCalcTheme(darkTheme = true) {
        val navController = rememberNavController()
         Scaffold {
            RemindersScreen(navController = navController, mainScaffoldPadding = it)
        }
    }
}
