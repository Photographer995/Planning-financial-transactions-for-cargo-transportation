package com.example.finlogcalc.radio.ui.RadioHome

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.MenuDefaults
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.finlogcalc.radio.data.sampleRadioStations
import com.example.finlogcalc.radio.ui.RadioMusic.RadioMusicScreen
import com.example.finlogcalc.radio.ui.viewmodel.RadioPlayerViewModel

@Composable
fun rememberFavorites(context: Context): MutableList<String> {
    val sharedPreferences = remember {
        context.getSharedPreferences("radio_favorites", Context.MODE_PRIVATE)
    }

    val favorites = remember {
        val savedFavorites = sharedPreferences.getStringSet("favorite_stations", emptySet())
        mutableStateListOf<String>().apply {
            addAll(savedFavorites.orEmpty())
        }
    }

    DisposableEffect(favorites) {
        onDispose {
            with(sharedPreferences.edit()) {
                putStringSet("favorite_stations", favorites.toSet())
                apply()
            }
        }
    }

    return favorites
}

@Composable
fun RadioHomeScreen(navController: NavController) {
    val bottomNavController = rememberNavController()

    val context = LocalContext.current
    val viewModel: RadioPlayerViewModel = viewModel(factory = RadioPlayerViewModel.Factory(context))

    val currentPlayingStation by viewModel.currentPlayingStation.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val sleepRemainingMs by viewModel.sleepRemainingMs.collectAsState()
    val currentMetadata by viewModel.currentMetadata.collectAsState()
    val favorites = rememberFavorites(context = context)
    val favoriteStations by remember { derivedStateOf { sampleRadioStations.filter { favorites.contains(it.id) } } }
    var isFullPlayerVisible by remember { mutableStateOf(false) }
    var currentFilterOption by remember { mutableStateOf<StationFilter>(StationFilter.All) }
    
    // Формируем список для свайпа в зависимости от текущего фильтра
    val carouselStations by remember(currentFilterOption, favoriteStations, currentPlayingStation) {
        derivedStateOf {
            val filter = currentFilterOption
            val base = when (filter) {
                StationFilter.Favorites -> favoriteStations
                StationFilter.All -> sampleRadioStations
                is StationFilter.Genre -> sampleRadioStations.filter { it.genre == filter.genre }
            }
            currentPlayingStation?.let { station ->
                if (base.any { it.id == station.id }) base else listOf(station) + base
            } ?: base
        }
    }

    fun toggleFavorite(stationId: String) {
        if (favorites.contains(stationId)) {
            favorites.remove(stationId)
        } else {
            favorites.add(stationId)
        }
        // Save to SharedPreferences
        val sharedPreferences = context.getSharedPreferences("radio_favorites", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putStringSet("favorite_stations", favorites.toSet())
            apply()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopRadio()
        }
    }

    NeonBackgroundRadio {
        Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                Column {
                    currentPlayingStation?.let { station ->
                        PersistentPlayerCard(
                            station = station,
                            isPlaying = isPlaying,
                                isFavorite = favorites.contains(station.id),
                            onPlayPauseClick = {
                                if (isPlaying) viewModel.pauseRadio() else viewModel.playRadio(station)
                            },
                                onToggleFavorite = { toggleFavorite(station.id) },
                                onExpand = { isFullPlayerVisible = true }
                        )
                    }
                    AppBottomNavigationBar(navController = bottomNavController)
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = bottomNavController,
                startDestination = "radio",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                composable("radio") {
                    RadioContent(
                        viewModel = viewModel,
                        currentPlayingStation = currentPlayingStation,
                        isPlaying = isPlaying,
                        favorites = favorites,
                        toggleFavorite = ::toggleFavorite,
                        currentFilterOption = currentFilterOption,
                        onFilterOptionChanged = { currentFilterOption = it }
                    )
                }
                composable("music") { RadioMusicScreen() }
                composable("video") { VideoScreen() }
                composable("progress") { ProgressScreen() }
                composable("utilities") { UtilitiesScreen() }
                }
            }

            currentPlayingStation?.let { station ->
                AnimatedVisibility(
                    modifier = Modifier.fillMaxSize(),
                    visible = isFullPlayerVisible,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    BackHandler(enabled = true) { isFullPlayerVisible = false }
                    FullScreenRadioPlayer(
                        station = station,
                        availableStations = carouselStations,
                        isPlaying = isPlaying,
                        isFavorite = favorites.contains(station.id),
                        sleepRemainingMs = sleepRemainingMs,
                        trackMetadata = currentMetadata,
                        onPlayPause = {
                            if (isPlaying) viewModel.pauseRadio() else viewModel.playRadio(station)
                        },
                        onToggleFavorite = { toggleFavorite(station.id) },
                        onDismiss = { isFullPlayerVisible = false },
                        onStationSelected = { selected ->
                            if (selected.id != station.id) {
                                viewModel.playRadio(selected)
                            }
                        },
                        onSleepTimer = { minutes ->
                            if (minutes == null) {
                                viewModel.cancelSleepTimer()
                            } else {
                                viewModel.startSleepTimer(minutes)
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioContent(
    viewModel: RadioPlayerViewModel,
    currentPlayingStation: com.example.finlogcalc.radio.data.RadioStation?,
    isPlaying: Boolean,
    favorites: MutableList<String>,
    toggleFavorite: (String) -> Unit,
    currentFilterOption: StationFilter,
    onFilterOptionChanged: (StationFilter) -> Unit
) {
    val allStations = sampleRadioStations
    val distinctGenres = remember {
        listOf(
            "Pop",
            "Rock",
            "Hip-Hop / Rap",
            "Electronic / Dance",
            "House",
            "Techno",
            "R&B / Soul",
            "Classical",
            "Jazz",
            "Country",
            "Metal",
            "Folk / Acoustic",
            "Reggae",
            "Blues",
            "Latin",
            "Indie",
            "Alternative",
            "Chill-Out / Lounge",
            "Disco",
            "Funk",
            "Punk",
            "Ambient",
            "Drum & Bass",
            "Trance",
            "Europop",
            "Schlager",
            "Classical Crossover",
            "World Music"
        )
    }

    var sortOption by remember { mutableStateOf(SortOption.Default) }
    val filterOption = currentFilterOption // Используем переданный фильтр
    var isFilterMenuExpanded by remember { mutableStateOf(false) }
    var favoritesOverlayVisible by remember { mutableStateOf(false) }

    val favoriteStations by remember { derivedStateOf { allStations.filter { favorites.contains(it.id) } } }
    val recommendedStations by remember { derivedStateOf { allStations.filterNot { station -> favoriteStations.any { it.id == station.id } } } }

    val filteredStations by remember {
        derivedStateOf {
            when (val filter = filterOption) {
                StationFilter.All -> allStations
                StationFilter.Favorites -> allStations.filter { favorites.contains(it.id) }
                is StationFilter.Genre -> allStations.filter { it.genre == filter.genre }
            }
        }
    }

    val stationsToDisplay = remember(filteredStations, sortOption) {
        when (sortOption) {
            SortOption.Default -> filteredStations
            SortOption.Alphabetical -> filteredStations.sortedBy { it.name }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
            Column(
            modifier = Modifier
                .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
                    Column {
            Text(
                text = "Radio Stations",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
                        val currentFilter = filterOption // Local immutable copy
                        Text(
                            text = when (currentFilter) {
                                StationFilter.All -> "Лучшие станции для любого настроения"
                                StationFilter.Favorites -> "Только ваши любимые станции"
                                is StationFilter.Genre -> "Жанр: ${currentFilter.genre}"
                            },
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 14.sp
                        )
                    }

                    IconButton(
                        onClick = {
                            if (favoriteStations.isNotEmpty()) {
                                favoritesOverlayVisible = true
                            }
                        }
                    ) {
                        BadgedBox(
                            badge = {
            if (favoriteStations.isNotEmpty()) {
                                    Badge(containerColor = NeonColorsRadio.Pink500) {
                                        Text(
                                            text = favoriteStations.size.toString(),
                                            color = Color.White,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (favoriteStations.isNotEmpty()) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Избранное",
                                tint = if (favoriteStations.isNotEmpty()) NeonColorsRadio.Pink500 else Color.White.copy(alpha = 0.4f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NeonFilterChip(
                    label = "По алфавиту",
                    icon = Icons.Filled.Sort,
                    selected = sortOption == SortOption.Alphabetical,
                    onClick = {
                        sortOption = if (sortOption == SortOption.Alphabetical) SortOption.Default else SortOption.Alphabetical
                    }
                )

                Box {
                    val currentFilter = filterOption
                    NeonFilterChip(
                        label = when (currentFilter) {
                            StationFilter.All -> "Фильтр"
                            StationFilter.Favorites -> "Избранные"
                            is StationFilter.Genre -> currentFilter.genre
                        },
                        icon = Icons.Filled.FilterList,
                        selected = filterOption != StationFilter.All,
                        onClick = { isFilterMenuExpanded = true }
                    )

                    val dropdownShape = RoundedCornerShape(20.dp)
                    DropdownMenu(
                        expanded = isFilterMenuExpanded,
                        onDismissRequest = { isFilterMenuExpanded = false },
                        modifier = Modifier
                            .background(
                                brush = Brush.verticalGradient(
                                    listOf(
                                        NeonColorsRadio.BackgroundDark.copy(alpha = 0.96f),
                                        NeonColorsRadio.BackgroundDark.copy(alpha = 0.88f)
                                    )
                                ),
                                shape = dropdownShape
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.08f), dropdownShape)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Все станции",
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                if (filterOption == StationFilter.All) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = NeonColorsRadio.Cyan400
                                    )
                                }
                            },
                            onClick = {
                                onFilterOptionChanged(StationFilter.All)
                                isFilterMenuExpanded = false
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = Color.White,
                                leadingIconColor = NeonColorsRadio.Cyan400
                            )
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Избранные",
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                if (filterOption == StationFilter.Favorites) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = NeonColorsRadio.Cyan400
                                    )
                                }
                            },
                            onClick = {
                                onFilterOptionChanged(StationFilter.Favorites)
                                isFilterMenuExpanded = false
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = Color.White,
                                leadingIconColor = NeonColorsRadio.Cyan400
                            )
                        )
                        if (distinctGenres.isNotEmpty()) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Жанры",
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                enabled = false,
                                onClick = {}
                            )
                            distinctGenres.forEach { genre ->
                                val localFilter = filterOption
                                val isGenreSelected = localFilter is StationFilter.Genre && localFilter.genre == genre
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = genre,
                                            color = Color.White,
                                            fontWeight = if (isGenreSelected) FontWeight.Medium else FontWeight.Normal
                                        )
                                    },
                                    leadingIcon = {
                                        if (isGenreSelected) {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = null,
                                                tint = NeonColorsRadio.Cyan400
                                            )
                                        }
                                    },
                                    onClick = {
                                        onFilterOptionChanged(StationFilter.Genre(genre))
                                        isFilterMenuExpanded = false
                                    },
                                    colors = MenuDefaults.itemColors(
                                        textColor = Color.White,
                                        leadingIconColor = NeonColorsRadio.Cyan400
                                    )
                                )
                            }
                        }
                    }
                }
            }
            }

            if (filterOption == StationFilter.Favorites) {
            FavoritesSection(
                stations = favoriteStations,
                onToggleFavorite = toggleFavorite,
                onPlay = { station ->
                    if (currentPlayingStation?.id == station.id && isPlaying) {
                        viewModel.pauseRadio()
                    } else {
                        viewModel.playRadio(station)
                    }
                },
                currentPlaying = currentPlayingStation
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                    items(stationsToDisplay, key = { it.id }) { station ->
                    val isCurrentlyPlaying = currentPlayingStation?.id == station.id && isPlaying

                    RadioStationRow(
                        station = station,
                        isFavorite = favorites.contains(station.id),
                        isPlaying = isCurrentlyPlaying,
                        onPlayClick = {
                            if (isCurrentlyPlaying) {
                                viewModel.pauseRadio()
                            } else {
                                viewModel.playRadio(station)
                            }
                        },
                        onFavoriteClick = { toggleFavorite(station.id) }
                    )
                }
            }
        }
    }

        AnimatedVisibility(
            visible = favoritesOverlayVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            BackHandler(enabled = favoritesOverlayVisible) { favoritesOverlayVisible = false }
            FavoritesOverlay(
                stations = favoriteStations,
                recommended = recommendedStations,
                currentPlaying = currentPlayingStation,
                isPlaying = isPlaying,
                onPlayStation = { station ->
                    if (currentPlayingStation?.id == station.id && isPlaying) {
                        viewModel.pauseRadio()
                    } else {
                        viewModel.playRadio(station)
                    }
                },
                onToggleFavorite = { stationId -> toggleFavorite(stationId) },
                onClose = { favoritesOverlayVisible = false }
            )
        }
    }
}

enum class SortOption { Default, Alphabetical }

sealed class StationFilter {
    object All : StationFilter()
    object Favorites : StationFilter()
    data class Genre(val genre: String) : StationFilter()
}

@Composable
fun MusicScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NeonColorsRadio.BackgroundGradientStart),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Music Screen",
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
fun VideoScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NeonColorsRadio.BackgroundGradientStart),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Video Screen",
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
fun ProgressScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NeonColorsRadio.BackgroundGradientStart),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Progress Screen",
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
fun UtilitiesScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NeonColorsRadio.BackgroundGradientStart),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Utilities Screen",
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
fun AppBottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

                val items = listOf(
                    BottomNavigationItemData(
                        route = "radio",
                        selectedIcon = Icons.Filled.Radio,
                        unselectedIcon = Icons.Outlined.Radio,
                        label = "Radio"
                    ),
                    BottomNavigationItemData(
                        route = "music",
                        selectedIcon = Icons.Filled.MusicNote,
                        unselectedIcon = Icons.Outlined.MusicNote,
                        label = "Music"
                    ),
                    BottomNavigationItemData(
                        route = "video",
                        selectedIcon = Icons.Filled.VideoLibrary,
                        unselectedIcon = Icons.Outlined.VideoLibrary,
                        label = "Video"
                    ),
                    BottomNavigationItemData(
                        route = "progress",
                        selectedIcon = Icons.Filled.BarChart,
                        unselectedIcon = Icons.Outlined.BarChart,
                        label = "Progress"
                    ),
                    BottomNavigationItemData(
                        route = "utilities",
                        selectedIcon = Icons.Filled.Build,
                        unselectedIcon = Icons.Outlined.Build,
                        label = "Utilities"
                    )
                )

    val barShape = RoundedCornerShape(28.dp)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp)
            .navigationBarsPadding()
            .padding(bottom = 0.dp)
            .offset(y = 6.dp),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .clip(barShape)
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            NeonColorsRadio.BackgroundDark.copy(alpha = 0.95f),
                            NeonColorsRadio.BackgroundDark.copy(alpha = 0.78f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.08f),
                    shape = barShape
                )
                .padding(vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(56.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(NeonColorsRadio.Cyan400, NeonColorsRadio.Purple500)
                        )
                    )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val selected = currentRoute == item.route
                    val iconTint by animateColorAsState(
                        targetValue = if (selected) Color.White else Color.White.copy(alpha = 0.6f),
                        label = "iconTint"
                    )
                    val labelAlpha by animateFloatAsState(
                        targetValue = if (selected) 1f else 0f,
                        label = "labelAlpha"
                    )
                    val scale by animateFloatAsState(
                        targetValue = if (selected) 1f else 0.94f,
                        label = "navScale"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                brush = if (selected) Brush.verticalGradient(
                                    listOf(
                                        NeonColorsRadio.Cyan400.copy(alpha = 0.35f),
                                        NeonColorsRadio.Purple500.copy(alpha = 0.25f)
                                    )
                                ) else Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Transparent)
                                )
                            )
                            .border(
                                width = if (selected) 1.2.dp else 1.dp,
                                brush = if (selected) Brush.verticalGradient(
                                    listOf(NeonColorsRadio.Cyan400, NeonColorsRadio.Purple500)
                                ) else Brush.verticalGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.08f),
                                        Color.White.copy(alpha = 0.02f)
                                    )
                                ),
                                shape = RoundedCornerShape(22.dp)
                            )
                            .clickable {
                                if (!selected) {
                                    navController.navigate(item.route) {
                                        launchSingleTop = true
                                        restoreState = true
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                    }
                                }
                            }
                            .padding(vertical = 6.dp, horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.fillMaxHeight(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label,
                                tint = iconTint,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.label,
                                color = Color.White.copy(alpha = labelAlpha),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

data class BottomNavigationItemData(
    val route: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String
)
