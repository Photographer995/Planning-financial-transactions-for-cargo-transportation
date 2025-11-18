@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.finlogcalc

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
// import android.os.Build // Not used directly, can be removed if no other usage
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.util.Locale
// Ensure Screen is imported if MainActivity.Screen is intended, though same package might not require it explicitly
// import com.example.finlogcalc.Screen // If needed after MainActivity changes

// --- SharedPreferences Keys (kept private to this file if not shared) ---
private const val PREFS_NAME = "FinLogCalcPrefs"
private const val KEY_THEME = "app_theme"
private const val KEY_LANGUAGE = "app_language"

// --- Data Enums (remain public as they are used in function signatures) ---
enum class AppThemeOption(@StringRes val titleResId: Int) {
    SYSTEM(R.string.theme_system),
    LIGHT(R.string.theme_light),
    DARK(R.string.theme_dark)
}

enum class AppLanguageOption(@StringRes val titleResId: Int, val code: String) {
    RUSSIAN(R.string.language_russian, "ru"),
    ENGLISH(R.string.language_english, "en")
}

// --- Helper Functions ---
private fun saveThemePreference(context: Context, theme: AppThemeOption) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putString(KEY_THEME, theme.name).apply()
}

// loadSelectedTheme needs to be public for MainActivity
fun loadSelectedTheme(context: Context): AppThemeOption {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val themeName = prefs.getString(KEY_THEME, AppThemeOption.SYSTEM.name)
    return AppThemeOption.valueOf(themeName ?: AppThemeOption.SYSTEM.name)
}

private fun saveLanguagePreference(context: Context, language: AppLanguageOption) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putString(KEY_LANGUAGE, language.code).apply()
}

// Renamed to avoid conflict with MainActivity.loadSelectedLanguage
private fun loadCurrentAppLanguageOption(context: Context): AppLanguageOption {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val languageCode = prefs.getString(KEY_LANGUAGE, AppLanguageOption.RUSSIAN.code) // Default to Russian as per original
    return AppLanguageOption.entries.find { it.code == languageCode } ?: AppLanguageOption.RUSSIAN
}

// This context extension function for locale update is specific to SettingsScreen usage for now
private fun Context.updateLocaleSettings(languageCode: String): Context {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)
    val config = Configuration(resources.configuration)
    config.setLocale(locale)
    return createConfigurationContext(config)
}

fun restartActivity(context: Context) { // This can remain public if called from elsewhere or for previews
    val intent = Intent(context, MainActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
    if (context is Activity) {
        context.finish()
    }
}

// --- Composable Functions ---
@Composable
fun SettingsScreen(
    navController: NavController? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var selectedTheme by remember { mutableStateOf(loadSelectedTheme(context)) } 
    // Updated to call the renamed function
    var selectedLanguage by remember { mutableStateOf<AppLanguageOption>(loadCurrentAppLanguageOption(context)) } 

    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    val playStoreUrl = "https://play.google.com/store/games?hl=ru&pli=1" // Example URL

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = selectedTheme,
            onDismissRequest = { showThemeDialog = false },
            onThemeSelected = {
                selectedTheme = it
                saveThemePreference(context, it) 
                showThemeDialog = false
                restartActivity(context)
            }
        )
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = selectedLanguage,
            onDismissRequest = { showLanguageDialog = false },
            onLanguageSelected = {
                selectedLanguage = it
                saveLanguagePreference(context, it) 
                showLanguageDialog = false
                context.updateLocaleSettings(it.code) 
                restartActivity(context)
            }
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    if (navController?.previousBackStackEntry != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_button_desc))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {

            item {
                SettingsSectionTitle(stringResource(R.string.settings_section_application))
            }
            item {
                SettingsChooserItem(
                    icon = Icons.Outlined.NightsStay,
                    title = stringResource(R.string.settings_item_theme),
                    currentValue = stringResource(selectedTheme.titleResId),
                    onClick = { showThemeDialog = true }
                )
            }
            item {
                SettingsChooserItem(
                    icon = Icons.Outlined.Translate,
                    title = stringResource(R.string.settings_item_language),
                    currentValue = stringResource(selectedLanguage.titleResId),
                    onClick = { showLanguageDialog = true }
                )
            }
            item { FormattedDivider() }

            item {
                SettingsSectionTitle(stringResource(R.string.settings_section_support))
            }
            item {
                SettingsClickableItem(
                    icon = Icons.Outlined.Share,
                    title = stringResource(R.string.settings_item_share),
                    subtitle = stringResource(R.string.settings_item_share_subtitle),
                    onClick = { shareApp(context, playStoreUrl) }
                )
            }
            item {
                SettingsClickableItem(
                    icon = Icons.Outlined.Email,
                    title = stringResource(R.string.settings_item_contact),
                    subtitle = stringResource(R.string.settings_item_contact_subtitle),
                    onClick = { contactUs(context, "svyathelp@gmail.com", "FinLogCalc") }
                )
            }
            item { FormattedDivider() }

            item {
                SettingsSectionTitle(stringResource(R.string.settings_section_information))
            }
            item {
                SettingsClickableItem(
                    icon = Icons.Outlined.PrivacyTip,
                    title = stringResource(R.string.settings_item_privacy_policy),
                    subtitle = stringResource(R.string.settings_item_privacy_policy_subtitle),
                    onClick = { openUrl(context, playStoreUrl) }
                )
            }
            item {
                SettingsClickableItem(
                    icon = Icons.Outlined.Article,
                    title = stringResource(R.string.settings_item_user_agreement),
                    subtitle = stringResource(R.string.settings_item_user_agreement_subtitle),
                    onClick = { openUrl(context, playStoreUrl) }
                )
            }
            item {
                SettingsClickableItem(
                    icon = Icons.Outlined.Storefront,
                    title = stringResource(R.string.settings_item_google_play),
                    subtitle = stringResource(R.string.settings_item_google_play_subtitle),
                    onClick = { openUrl(context, playStoreUrl) }
                )
            }
            item {
                SettingsInfoItem(
                    icon = Icons.Outlined.Info,
                    title = stringResource(R.string.settings_item_app_version),
                    info = "1.0" // Example version
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: AppThemeOption,
    onDismissRequest: () -> Unit,
    onThemeSelected: (AppThemeOption) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.dialog_select_theme_title)) },
        text = {
            Column {
                AppThemeOption.entries.forEach { theme ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (theme == currentTheme),
                                onClick = { onThemeSelected(theme) }
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (theme == currentTheme),
                            onClick = null
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(stringResource(theme.titleResId))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.dialog_button_cancel))
            }
        }
    )
}

@Composable
fun LanguageSelectionDialog(
    currentLanguage: AppLanguageOption,
    onDismissRequest: () -> Unit,
    onLanguageSelected: (AppLanguageOption) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.dialog_select_language_title)) },
        text = {
            Column {
                AppLanguageOption.entries.forEach { language ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (language == currentLanguage),
                                onClick = { onLanguageSelected(language) }
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (language == currentLanguage),
                            onClick = null
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(stringResource(language.titleResId))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.dialog_button_cancel))
            }
        }
    )
}

// These UI helper composables can remain public if they are generic enough or used in previews.
@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsChooserItem(
    icon: ImageVector,
    title: String,
    currentValue: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.padding(end = 16.dp).size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                currentValue,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun SettingsClickableItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.padding(end = 16.dp).size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun SettingsInfoItem(
    icon: ImageVector,
    title: String,
    info: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.padding(end = 16.dp).size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                info,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FormattedDivider() {
    Divider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}

// These intent launchers can remain public if generic
fun shareApp(context: Context, appUrl: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, "Ознакомьтесь с этим приложением: $appUrl")
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, null)
    context.startActivity(shareIntent)
}

fun contactUs(context: Context, email: String, subject: String) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        putExtra(Intent.EXTRA_SUBJECT, subject)
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        // Handle error, e.g., show a Toast
    }
}

fun openUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        // Handle error, e.g., show a Toast
    }
}
