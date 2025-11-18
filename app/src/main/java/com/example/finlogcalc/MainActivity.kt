package com.example.finlogcalc

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
// Removed: import androidx.compose.material.icons.automirrored.filled.*  - This was causing conflict for ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.finlogcalc.auth.ui.AuthNavigation
import com.example.finlogcalc.calculator.ElectricCarCalculatorFeatureNavHost
import com.example.finlogcalc.calculator.FuelCalculatorFeatureNavHost
import com.example.finlogcalc.calculator.alcoholcalculator.ui.AlcoholCalculatorFeatureNavHost
import com.example.finlogcalc.calculator.brakingpath.BrakingPathCalculatorScreen
import com.example.finlogcalc.features.tripcalculator.driver.DriverTripActivity
import com.example.finlogcalc.mycar.FinancesScreen
import com.example.finlogcalc.mycar.RemindersScreen
import com.example.finlogcalc.mycar.StatisticsScreen
import com.example.finlogcalc.mycar.menu.MyCarFeatureNavHost
import com.example.finlogcalc.ui.theme.FinLogCalcTheme
import com.example.finlogcalc.utils.blur
import com.example.finlogcalc.utils.chat.ChatScreenNeon // Changed from ChatScreen
import com.example.finlogcalc.radio.ui.RadioHome.RadioHomeScreen
import kotlinx.coroutines.launch
import java.util.Locale

data class Language(val code: String, val displayName: String)

private const val APP_PREFS_NAME = "FinLogCalcPrefs"
private const val KEY_SELECTED_LANGUAGE = "selected_language"

fun loadSelectedLanguage(context: Context): Language {
    val sharedPreferences = context.getSharedPreferences(APP_PREFS_NAME, Context.MODE_PRIVATE)
    val defaultLanguageCode = Locale.getDefault().language
    val languageCode = sharedPreferences.getString(KEY_SELECTED_LANGUAGE, defaultLanguageCode) ?: defaultLanguageCode
    return Language(languageCode, languageCode.uppercase(Locale.getDefault()))
}

fun Context.updateLocale(languageCode: String): Context {
    val locale = Locale.forLanguageTag(languageCode)
    Locale.setDefault(locale)
    val resources = this.resources
    val configuration = resources.configuration
    configuration.setLocale(locale)
    configuration.setLayoutDirection(locale)
    return createConfigurationContext(configuration)
}

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Auth : Screen("auth")
    object MainMenu : Screen("main_menu")
    object MainApp : Screen("main_app")
    object Settings : Screen("settings")
    object MyCar : Screen("my_car_feature_screen")
    object FuelCalculator : Screen("fuel_calculator_feature")
    object ElectricCarCalculator : Screen("electric_car_calculator_feature")
    object AlcoholCalculator : Screen("alcohol_calculator_feature")
    object BrakingPathCalculator : Screen("braking_path_calculator_screen")
    object TripCalculatorChoice : Screen("trip_calculator_choice")
    object Chat : Screen("chat_screen") // Added Chat Screen
    object Radio : Screen("radio_screen")
    object Feature : Screen("feature_screen/{featureName}") {
        fun createRoute(featureName: String) = "feature_screen/$featureName"
    }
}

sealed class BottomNavItem(
    val route: String,
    val titleResId: Int,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) {
    object Car : BottomNavItem("bottom_car", R.string.bottom_nav_car, Icons.Outlined.DirectionsCar, Icons.Filled.DirectionsCar)
    object Finances : BottomNavItem("bottom_finances", R.string.bottom_nav_finances, Icons.Outlined.AccountBalanceWallet, Icons.Filled.AccountBalanceWallet)
    object Statistics : BottomNavItem("bottom_statistics", R.string.bottom_nav_statistics, Icons.Outlined.BarChart, Icons.Filled.BarChart)
    object Reminders : BottomNavItem("bottom_reminders", R.string.bottom_nav_reminders, Icons.Outlined.Notifications, Icons.Filled.Notifications)
}

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val languageCode = loadSelectedLanguage(newBase).code
        super.attachBaseContext(newBase.updateLocale(languageCode))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val currentThemeSetting = loadSelectedTheme(this)
            val useDarkTheme = when (currentThemeSetting) {
                AppThemeOption.LIGHT -> false
                AppThemeOption.DARK -> true
                AppThemeOption.SYSTEM -> isSystemInDarkTheme()
            }
            FinLogCalcTheme(darkTheme = useDarkTheme) {
                val rootNavController = rememberNavController()
                NavHost(
                    navController = rootNavController,
                    startDestination = Screen.Auth.route,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(Screen.Welcome.route) { WelcomeScreen(rootNavController = rootNavController, modifier = Modifier.fillMaxSize()) }
                    composable(Screen.Auth.route) { AuthNavigation(rootNavController = rootNavController) }
                    composable(Screen.MainMenu.route) { MainMenuScreen(navController = rootNavController, modifier = Modifier.fillMaxSize()) }
                    composable(Screen.MainApp.route) { MainAppScreen(rootNavController = rootNavController, modifier = Modifier.fillMaxSize()) }
                    composable(Screen.Settings.route) { SettingsScreen(navController = rootNavController, modifier = Modifier.fillMaxSize()) }
                    composable(Screen.MyCar.route) { MyCarFeatureNavHost(mainNavController = rootNavController, scaffoldPadding = PaddingValues(), modifier = Modifier.fillMaxSize()) }
                    composable(Screen.FuelCalculator.route) {
                        FuelCalculatorFeatureNavHost(
                            mainNavController = rootNavController,
                            scaffoldPadding = PaddingValues(),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    composable(Screen.ElectricCarCalculator.route) {
                        ElectricCarCalculatorFeatureNavHost(
                            mainNavController = rootNavController,
                            scaffoldPadding = PaddingValues(),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    composable(Screen.AlcoholCalculator.route) {
                        AlcoholCalculatorFeatureNavHost(
                            mainNavController = rootNavController,
                            scaffoldPadding = PaddingValues(),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    composable(Screen.BrakingPathCalculator.route) { BrakingPathCalculatorScreen(/* navController = rootNavController */) }
                    composable(Screen.TripCalculatorChoice.route) { TripCalculatorChoiceScreen(modifier = Modifier.fillMaxSize())}
                    composable(Screen.Chat.route) { ChatScreenNeon(onNavigateBack = { rootNavController.popBackStack() }) } // Changed to ChatScreenNeon
                    composable(Screen.Radio.route) { RadioHomeScreen(navController = rootNavController) }
                    composable(Screen.Feature.route, arguments = listOf(navArgument("featureName") { type = NavType.StringType })) { backStackEntry ->
                        val featureName = backStackEntry.arguments?.getString("featureName") ?: "Feature"
                        ActualFeatureScreen(navController = rootNavController, featureName = featureName, modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerHeaderProfile(name: String, email: String, avatar: ImageVector, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = avatar,
                contentDescription = stringResource(R.string.drawer_header_avatar_description),
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawerMenuItem(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDrawerContent(navController: NavHostController, drawerState: DrawerState, scope: kotlinx.coroutines.CoroutineScope) {
    ModalDrawerSheet(
        modifier = Modifier.fillMaxHeight(),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Column {
            DrawerHeaderProfile(name = "Svyatoslav", email = "svyat@example.com", avatar = Icons.Filled.AccountCircle)
            AppDrawerMenuItem(
                text = stringResource(R.string.main_menu_button_my_car),
                icon = Icons.Outlined.DirectionsCar,
                onClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate(Screen.MainApp.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )

            AppDrawerMenuItem(
                text = stringResource(R.string.main_menu_button_journey_calculator),
                icon = Icons.Outlined.Map,
                onClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate(Screen.TripCalculatorChoice.route)
                }
            )

            AppDrawerMenuItem(
                text = stringResource(R.string.main_menu_button_settings),
                icon = Icons.Outlined.Settings,
                onClick = {
                    scope.launch { drawerState.close() }
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(rootNavController: NavHostController, modifier: Modifier = Modifier) {
    val bottomNavController = rememberNavController()
    val items = listOf(BottomNavItem.Car, BottomNavItem.Finances, BottomNavItem.Finances, BottomNavItem.Statistics, BottomNavItem.Reminders)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(drawerState = drawerState, drawerContent = { AppDrawerContent(navController = rootNavController, drawerState = drawerState, scope = scope) }, modifier = modifier) {
        Scaffold(
            topBar = {
                TopAppBarNeon(
                    title = stringResource(id = R.string.app_name),
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onUserClick = { /* TODO: Navigate to user profile or settings */ }
                )
            },
            bottomBar = { AppBottomNavigationBar(navController = bottomNavController, items = items) }
        ) { innerPadding ->
            NavHost(navController = bottomNavController, startDestination = BottomNavItem.Car.route, modifier = Modifier.fillMaxSize()) {
                composable(BottomNavItem.Car.route) { MyCarFeatureNavHost(mainNavController = rootNavController, scaffoldPadding = innerPadding, modifier = Modifier.fillMaxSize()) }
                composable(BottomNavItem.Finances.route) {
                    FinancesScreen(
                        navController = bottomNavController,
                        mainScaffoldPadding = innerPadding
                    )
                }
                composable(BottomNavItem.Statistics.route) {
                    StatisticsScreen(
                        navController = bottomNavController,
                        mainScaffoldPadding = innerPadding
                    )
                }
                composable(BottomNavItem.Reminders.route) {
                    RemindersScreen(
                        navController = bottomNavController,
                        mainScaffoldPadding = innerPadding
                    )
                }
            }
        }
    }
}

@Composable
fun AppBottomNavigationBar(navController: NavController, items: List<BottomNavItem>) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(if (currentDestination?.hierarchy?.any { it.route == screen.route } == true) screen.selectedIcon else screen.icon, contentDescription = stringResource(screen.titleResId)) },
                label = { Text(stringResource(screen.titleResId)) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun WelcomeScreen(rootNavController: NavController, modifier: Modifier = Modifier) {
    Box(modifier = modifier.clickable { rootNavController.navigate(Screen.MainMenu.route) { popUpTo(Screen.Welcome.route) { inclusive = true } } }) {
        Image(painter = painterResource(id = R.drawable.helloimage2025), contentDescription = stringResource(R.string.welcome_greeting), modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
    }
}

//--- NEW Main Menu Design Composables ---
object NeonColors {
    val BackgroundGradientStart = Color(0xFF020617) // slate-950
    val BackgroundGradientMid = Color(0xFF0F172A) // slate-900
    val BackgroundGradientEnd = Color(0xFF020617) // slate-950

    val Cyan600 = Color(0xFF0891B2)
    val Cyan500 = Color(0xFF06B6D4)
    val Cyan400 = Color(0xFF22D3EE)
    val Purple600 = Color(0xFF9333EA)
    val Purple500 = Color(0xFFA855F7)
    val Purple400 = Color(0xFFC084FC)
    val Pink600 = Color(0xFFDB2777)
    val Pink500 = Color(0xFFEC4899)
    val Orange400 = Color(0xFFFB923C)
    val Orange500 = Color(0xFFF97316)
    val Amber600 = Color(0xFFD97706)
    val Lime500 = Color(0xFF84CC16)

    val Slate900 = Color(0xFF0F172A)

    val Cyan500_10 = Cyan500.copy(alpha = 0.1f)
    val Cyan500_20 = Cyan500.copy(alpha = 0.2f)
    val Cyan500_30 = Cyan500.copy(alpha = 0.3f)
    val Cyan500_40 = Cyan500.copy(alpha = 0.4f)
    val Cyan500_50 = Cyan500.copy(alpha = 0.5f)

    val Purple500_10 = Purple500.copy(alpha = 0.1f)
    val Purple500_20 = Purple500.copy(alpha = 0.2f)
    val Purple500_50 = Purple500.copy(alpha = 0.5f)
    val Purple500_60 = Purple500.copy(alpha = 0.6f)

    val Pink500_05 = Pink500.copy(alpha = 0.05f)

    val Gray200 = Color(0xFFE5E7EB)
    val Gray300 = Color(0xFFD1D5DB)
    val Gray400 = Color(0xFF9CA3AF)
    val Gray600 = Color(0xFF4B5563)

    // Gradients for icons/elements
    val PurpleToPinkGradient = listOf(Purple600, Pink600)
    val CyanToPurpleGradient = listOf(Cyan400, Purple400)
    val EMERALD_LIGHT = Color(0xFF6EE7B7) // emerald-300
    val EMERALD_DARK = Color(0xFF047857)  // emerald-700
    val ElectricGradient = listOf(EMERALD_LIGHT, EMERALD_DARK) // Example, adjust as needed
    val FuelGradient = listOf(Cyan500, Cyan600) // Cyan
    val AlcoholGradient = listOf(Pink500, Purple600) // Pink-Purple
    val BrakingPathGradient = listOf(Orange500, Amber600) // Orange-Amber
    val RadioGradient = listOf(NeonColors.Cyan400, NeonColors.Cyan600)
}

@Composable
fun NeonBackground(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        NeonColors.BackgroundGradientStart,
                        NeonColors.BackgroundGradientMid,
                        NeonColors.BackgroundGradientEnd
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .size(384.dp)
                .align(Alignment.TopStart)
                .offset(x = 96.dp, y = 0.dp)
                .background(NeonColors.Cyan500_10, CircleShape)
                .blur(128.dp)
        )
        Box(
            modifier = Modifier
                .size(384.dp)
                .align(Alignment.BottomEnd)
                .offset(x = (-96).dp, y = 0.dp)
                .background(NeonColors.Purple500_10, CircleShape)
                .blur(128.dp)
        )
        Box(
            modifier = Modifier
                .size(600.dp)
                .align(Alignment.Center)
                .background(NeonColors.Pink500_05, CircleShape)
                .blur(128.dp)
        )
    }
}

@Composable
fun TopAppBarNeon(
    title: String,
    onMenuClick: () -> Unit,
    onUserClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(NeonColors.Slate900.copy(alpha = 0.8f))
            .border(1.dp, NeonColors.Cyan500.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = NeonColors.Cyan500.copy(alpha = 0.1f),
                spotColor = NeonColors.Cyan500.copy(alpha = 0.1f)
            )
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
        ) {
            Icon(Icons.Filled.Menu, contentDescription = stringResource(R.string.main_menu_drawer_open_description), tint = NeonColors.Cyan400)
        }

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(brush = Brush.horizontalGradient(NeonColors.CyanToPurpleGradient))) {
                    append(title)
                }
            },
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp),
            fontWeight = FontWeight.Bold
        )

        IconButton(
            onClick = onUserClick,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
        ) {
            Icon(Icons.Filled.AccountCircle, contentDescription = "User profile", tint = NeonColors.Gray400)
        }
    }
}

@Composable
fun MainMenuNeonButton(
    text: String,
    icon: ImageVector,
    iconTint: Color,
    borderColor: Color,
    shadowColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            // Removed .background(NeonColors.Slate900_50)
            .border(2.dp, borderColor, RoundedCornerShape(24.dp))
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = shadowColor,
                spotColor = shadowColor
            ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent) // Make card transparent to show background
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = text, color = NeonColors.Gray200, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun NyaKoSection(
    isPlaying: Boolean,
    onCatClick: () -> Unit,
    catResponse: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.size(128.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.sweepGradient(
                            listOf(
                                NeonColors.Purple600,
                                NeonColors.Pink600,
                                NeonColors.Cyan600,
                                NeonColors.Purple600
                            )
                        ),
                        shape = CircleShape
                    )
                    .clip(CircleShape)
                    .padding(4.dp)
                    .background(NeonColors.Slate900, CircleShape)
                    .shadow(
                        elevation = 30.dp,
                        shape = CircleShape,
                        ambientColor = NeonColors.Purple500_60,
                        spotColor = NeonColors.Purple500_60
                    )
            ) {
                Image(
                    painter = painterResource(id = R.drawable.iconnyako),
                    contentDescription = "Няко",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp) // Changed from 20.dp to 4.dp
                        .clickable(onClick = onCatClick), // Make the image clickable
                    contentScale = ContentScale.Fit
                )
            }
        }
        Text(
            text = "Няко",
            style = MaterialTheme.typography.headlineMedium,
            color = NeonColors.Gray300,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (catResponse.isNotBlank()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NeonColors.Slate900),
                elevation = CardDefaults.cardElevation(2.dp),
                border = BorderStroke(1.dp, NeonColors.Cyan500_20)
            ) {
                Text(
                    text = catResponse,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = NeonColors.Gray300
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun NeonCalculatorGridItemCard(
    text: String,
    icon: ImageVector,
    iconGradientColors: List<Color>,
    borderColor: Color,
    shadowColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(20.dp))
            // Removed .background(NeonColors.Slate900_50)
            .border(2.dp, borderColor, RoundedCornerShape(20.dp))
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = shadowColor,
                spotColor = shadowColor
            ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(brush = Brush.verticalGradient(colors = iconGradientColors))
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(12.dp),
                        ambientColor = iconGradientColors
                            .first()
                            .copy(alpha = 0.6f),
                        spotColor = iconGradientColors
                            .last()
                            .copy(alpha = 0.6f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = NeonColors.Gray300,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun NeonHistoryItemCard(
    text: String,
    subText: String,
    icon: ImageVector,
    borderColor: Color,
    shadowColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            // Removed .background(NeonColors.Slate900_50)
            .border(1.dp, borderColor, RoundedCornerShape(24.dp))
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = shadowColor,
                spotColor = shadowColor
            ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(icon, contentDescription = null, tint = NeonColors.Cyan400, modifier = Modifier.size(20.dp));
                Text(text, color = NeonColors.Cyan400, style = MaterialTheme.typography.bodyLarge)
            }
            Text(subText, color = NeonColors.Gray400, style = MaterialTheme.typography.bodyMedium)
        }
    }
}


@Composable
fun MainMenuScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    // Removed textToSpeech related code and createSsml function
    var catResponse by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        onDispose {
            // textToSpeech.shutdown() is no longer needed if TextToSpeech is not initialized here
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(navController = navController, drawerState = drawerState, scope = scope)
        },
        modifier = modifier
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            NeonBackground()
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBarNeon(
                        title = stringResource(id = R.string.app_name),
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onUserClick = { /* TODO: Navigate to user profile or settings */ }
                    )
                }
            ) { mainMenuScaffoldInnerPadding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(mainMenuScaffoldInnerPadding)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 32.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            MainMenuNeonButton(
                                text = stringResource(id = R.string.main_menu_button_my_car),
                                icon = Icons.Filled.DirectionsCar,
                                iconTint = NeonColors.Orange400,
                                borderColor = NeonColors.Cyan500_50,
                                shadowColor = NeonColors.Cyan500_20,
                                onClick = { navController.navigate(Screen.MainApp.route) },
                                modifier = Modifier.weight(1f)
                            )
                            MainMenuNeonButton(
                                text = stringResource(id = R.string.main_menu_card_driver_title),
                                icon = Icons.Filled.AccountCircle,
                                iconTint = NeonColors.Purple400,
                                borderColor = NeonColors.Purple500_50,
                                shadowColor = NeonColors.Purple500_20,
                                onClick = {
                                    val intent = Intent(context, DriverTripActivity::class.java)
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        NyaKoSection(
                            isPlaying = isPlaying,
                            onCatClick = { navController.navigate(Screen.Chat.route) }, // Directly navigate to chat screen
                            catResponse = catResponse,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Radio button added here
                    item {
                        MainMenuNeonButton(
                            text = stringResource(id = R.string.main_menu_button_radio),
                            icon = Icons.Filled.Radio,
                            iconTint = NeonColors.Cyan400,
                            borderColor = NeonColors.RadioGradient.first().copy(alpha = 0.5f),
                            shadowColor = NeonColors.RadioGradient.last().copy(alpha = 0.4f),
                            onClick = { navController.navigate(Screen.Radio.route) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        )
                    }

                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Calculate,
                                contentDescription = null,
                                tint = NeonColors.Cyan400,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(id = R.string.main_menu_section_calculators),
                                style = MaterialTheme.typography.titleMedium,
                                color = NeonColors.Gray300
                            )
                        }
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 32.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                NeonCalculatorGridItemCard(
                                    text = stringResource(R.string.main_menu_button_fuel_calculator),
                                    icon = Icons.Filled.LocalGasStation,
                                    iconGradientColors = NeonColors.FuelGradient,
                                    borderColor = NeonColors.Cyan500_50,
                                    shadowColor = NeonColors.Cyan500_40,
                                    onClick = { navController.navigate(Screen.FuelCalculator.route) },
                                    modifier = Modifier.weight(1f)
                                )
                                NeonCalculatorGridItemCard(
                                    text = stringResource(R.string.main_menu_button_electric_car_calculator),
                                    icon = Icons.Filled.EvStation,
                                    iconGradientColors = NeonColors.ElectricGradient,
                                    borderColor = NeonColors.Lime500.copy(alpha = 0.5f),
                                    shadowColor = NeonColors.Lime500.copy(alpha = 0.4f),
                                    onClick = { navController.navigate(Screen.ElectricCarCalculator.route) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                NeonCalculatorGridItemCard(
                                    text = stringResource(R.string.main_menu_button_alcohol_calculator),
                                    icon = Icons.Filled.WineBar,
                                    iconGradientColors = NeonColors.AlcoholGradient,
                                    borderColor = NeonColors.Pink500.copy(alpha = 0.5f),
                                    shadowColor = NeonColors.Pink500.copy(alpha = 0.4f),
                                    onClick = { navController.navigate(Screen.AlcoholCalculator.route) },
                                    modifier = Modifier.weight(1f)
                                )
                                NeonCalculatorGridItemCard(
                                    text = stringResource(R.string.main_menu_button_braking_path_calculator),
                                    icon = Icons.Filled.Speed,
                                    iconGradientColors = NeonColors.BrakingPathGradient,
                                    borderColor = NeonColors.Orange500.copy(alpha = 0.5f),
                                    shadowColor = NeonColors.Orange500.copy(alpha = 0.4f),
                                    onClick = { navController.navigate(Screen.BrakingPathCalculator.route) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = null,
                                tint = NeonColors.Purple400,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(id = R.string.main_menu_button_history),
                                style = MaterialTheme.typography.titleMedium,
                                color = NeonColors.Gray300
                            )
                        }
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            NeonHistoryItemCard(
                                text = stringResource(id = R.string.main_menu_card_calculators_history_title),
                                subText = "",
                                icon = Icons.Filled.History,
                                borderColor = NeonColors.Cyan500_30,
                                shadowColor = NeonColors.Cyan500_10,
                                onClick = { navController.navigate(Screen.Feature.createRoute(navController.context.getString(R.string.main_menu_card_calculators_history_title))) }
                            )
                            NeonHistoryItemCard(
                                text = stringResource(id = R.string.main_menu_button_trip_history),
                                subText = "",
                                icon = Icons.Filled.Route,
                                borderColor = NeonColors.Cyan500_30,
                                shadowColor = NeonColors.Cyan500_10,
                                onClick = { navController.navigate(Screen.Feature.createRoute(navController.context.getString(R.string.main_menu_button_trip_history))) }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun TripCalculatorChoiceScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBarNeon(
                title = stringResource(R.string.main_menu_button_journey_calculator),
                onMenuClick = { /* No menu in this screen */ },
                onUserClick = { /* No user profile in this screen */ },
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            NeonBackground()
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MainMenuNeonButton(
                    text = stringResource(R.string.trip_choice_driver),
                    icon = Icons.Filled.DirectionsCar,
                    iconTint = NeonColors.Orange400,
                    borderColor = NeonColors.Cyan500_50,
                    shadowColor = NeonColors.Cyan500_20,
                    onClick = {
                        val intent = Intent(context, DriverTripActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActualFeatureScreen(navController: NavController, featureName: String, modifier: Modifier = Modifier) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(featureName) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back)) } }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = stringResource(R.string.feature_screen_title, featureName))
        }
    }
}

@Preview(showBackground = true, name = "Welcome Screen Preview")
@Composable
fun WelcomeScreenPreview() {
    FinLogCalcTheme { WelcomeScreen(rootNavController = rememberNavController(), modifier = Modifier.fillMaxSize()) }
}

@Preview(showBackground = true, name = "Main Menu Screen Preview")
@Composable
fun MainMenuScreenPreview() {
    FinLogCalcTheme { MainMenuScreen(navController = rememberNavController(), modifier = Modifier.fillMaxSize()) }
}

@Preview(showBackground = true, name = "Trip Calculator Choice Screen Preview")
@Composable
fun TripCalculatorChoiceScreenPreview() {
    FinLogCalcTheme { TripCalculatorChoiceScreen(modifier = Modifier.fillMaxSize()) }
}

@Preview(showBackground = true, name = "Main App Screen Preview")
@Composable
fun MainAppScreenPreview() {
    FinLogCalcTheme { MainAppScreen(rootNavController = rememberNavController(), modifier = Modifier.fillMaxSize()) }
}

@Preview(showBackground = true, name = "App Bottom Nav Bar Preview")
@Composable
fun AppBottomNavigationBarPreview() {
    FinLogCalcTheme { AppBottomNavigationBar(navController = rememberNavController(), items = listOf(BottomNavItem.Car, BottomNavItem.Finances, BottomNavItem.Statistics, BottomNavItem.Reminders)) }
}

@Preview(showBackground = true, name = "Feature Screen Preview")
@Composable
fun FeatureScreenPreview() {
    FinLogCalcTheme { ActualFeatureScreen(navController = rememberNavController(), featureName = "Example Feature", modifier = Modifier.fillMaxSize()) }
}