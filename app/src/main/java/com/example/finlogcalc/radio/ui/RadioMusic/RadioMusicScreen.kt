package com.example.finlogcalc.radio.ui.RadioMusic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioMusicScreen() {
    val context = LocalContext.current
    val viewModel: MusicViewModel = viewModel(factory = MusicViewModel.Factory(context))

    val songs by viewModel.filteredSongs.collectAsState()
    val currentSong by viewModel.currentPlayingSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playbackPosition by viewModel.playbackPosition.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()

    var showSearchBar by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showSongMenu by remember { mutableStateOf<Song?>(null) }

    NeonBackgroundMusic {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Верхняя навигация
            TopNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { viewModel.setSelectedTab(it) },
                onSearchClick = { showSearchBar = !showSearchBar },
                onFilterClick = { showSortDialog = true },
                searchQuery = searchQuery,
                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                showSearchBar = showSearchBar,
                modifier = Modifier.fillMaxWidth()
            )

            // Основной контент
            when (selectedTab) {
                MusicViewModel.MusicTab.PLAYLISTS -> {
                    // Экран плейлистов
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) {
                        PlaylistsScreen()
                    }
                }
                MusicViewModel.MusicTab.FOLDERS -> {
                    // TODO: Реализовать экран папок
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Экран папок (в разработке)",
                            color = RadioColor.Gray400
                        )
                    }
                }
                MusicViewModel.MusicTab.SONGS -> {
                    // Экран песен
                    if (isLoading && songs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = RadioColor.Cyan400
                            )
                        }
                    } else {
                        val listState = rememberLazyListState()

                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            // Область "Сейчас играет"
                            item {
                                NowPlayingArea(
                                    song = currentSong,
                                    isPlaying = isPlaying,
                                    playbackPosition = playbackPosition,
                                    totalDuration = currentSong?.duration ?: 0L,
                                    onPlayClick = { viewModel.togglePlayPause() },
                                    onShuffleClick = { viewModel.playShuffle() },
                                    onPreviousClick = { viewModel.playPreviousSong() },
                                    onNextClick = { viewModel.playNextSong() },
                                    onSeek = { newProgress -> viewModel.seekToPosition(newProgress) }
                                )
                            }

                            // Заголовок списка
                            if (songs.isNotEmpty()) {
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (currentSong != null) "NEXT IN QUEUE" else "МОЯ МУЗЫКА",
                                            color = Color.White,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        Text(
                                            text = "${songs.size} ${if (songs.size == 1) "песня" else "песен"}",
                                            color = RadioColor.Gray400,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }

                            // Список песен
                            items(
                                items = songs,
                                key = { it.id }
                            ) { song ->
                                SongItem(
                                    song = song,
                                    isCurrentlyPlaying = currentSong?.id == song.id && isPlaying,
                                    onSongClick = {
                                        if (currentSong?.id == song.id) {
                                            viewModel.togglePlayPause()
                                        } else {
                                            viewModel.playSong(song)
                                        }
                                    },
                                    onMoreClick = { showSongMenu = song }
                                )

                                HorizontalDivider(
                                    color = RadioColor.White10,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }

                            // Пустое состояние
                            if (songs.isEmpty() && !isLoading) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.MusicOff,
                                                contentDescription = null,
                                                tint = RadioColor.Gray400,
                                                modifier = Modifier.size(64.dp)
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = if (searchQuery.isNotBlank())
                                                    "Ничего не найдено"
                                                else
                                                    "Музыка не найдена",
                                                color = RadioColor.Gray400,
                                                fontSize = 16.sp
                                            )
                                            if (searchQuery.isBlank()) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = "Добавьте музыку на устройство",
                                                    color = RadioColor.Gray500,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Диалог сортировки
    if (showSortDialog) {
        SortDialog(
            onDismiss = { showSortDialog = false },
            onSortSelected = { sortType ->
                viewModel.sortSongs(sortType)
                showSortDialog = false
            }
        )
    }

    // Контекстное меню песни (Bottom Sheet)
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // Показываем bottom sheet когда выбран song
    showSongMenu?.let { song ->
        LaunchedEffect(song) {
            sheetState.show()
        }

        SongContextBottomSheet(
            song = song,
            sheetState = sheetState,
            onDismiss = {
                scope.launch {
                    sheetState.hide()
                }.invokeOnCompletion {
                    showSongMenu = null
                }
            },
            onAddToFavorites = {
                // TODO: Реализовать добавление в любимые
                scope.launch {
                    sheetState.hide()
                    showSongMenu = null
                }
            },
            onChangeCover = {
                // TODO: Реализовать смену обложки
                scope.launch {
                    sheetState.hide()
                    showSongMenu = null
                }
            },
            onRename = {
                // TODO: Реализовать переименование файла
                scope.launch {
                    sheetState.hide()
                    showSongMenu = null
                }
            },
            onShare = {
                // TODO: Реализовать поделиться треком
                scope.launch {
                    sheetState.hide()
                    showSongMenu = null
                }
            },
            onSearchYouTube = {
                // TODO: Реализовать поиск на YouTube
                scope.launch {
                    sheetState.hide()
                    showSongMenu = null
                }
            },
            onShowInfo = {
                // TODO: Реализовать показ информации о файле
                scope.launch {
                    sheetState.hide()
                    showSongMenu = null
                }
            }
        )
    }
}

@Composable
fun TopNavigationBar(
    selectedTab: MusicViewModel.MusicTab,
    onTabSelected: (MusicViewModel.MusicTab) -> Unit,
    onSearchClick: () -> Unit,
    onFilterClick: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    showSearchBar: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(RadioColor.Slate900.copy(alpha = 0.9f))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (showSearchBar) {
            // Панель поиска
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Поиск...", color = RadioColor.Gray400) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = RadioColor.Gray400
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Clear",
                                    tint = RadioColor.Gray400
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = RadioColor.Cyan400,
                        unfocusedBorderColor = RadioColor.Slate700,
                        focusedContainerColor = RadioColor.Slate800,
                        unfocusedContainerColor = RadioColor.Slate800
                    ),
                    singleLine = true
                )
            }
        } else {
            // Обычная панель навигации
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Песни",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Row {
                    IconButton(onClick = onFilterClick) {
                        Icon(
                            imageVector = Icons.Filled.FilterList,
                            contentDescription = "Filter",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Сегментированный контроль вкладок
            MusicTabSegmentedControl(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected
            )
        }
    }
}

@Composable
fun SortDialog(
    onDismiss: () -> Unit,
    onSortSelected: (MusicViewModel.SortType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Сортировка",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                MusicViewModel.SortType.entries.forEach { sortType ->
                    val sortName = when (sortType) {
                        MusicViewModel.SortType.TITLE -> "По названию"
                        MusicViewModel.SortType.ARTIST -> "По исполнителю"
                        MusicViewModel.SortType.ALBUM -> "По альбому"
                        MusicViewModel.SortType.DATE_ADDED -> "По дате добавления"
                        MusicViewModel.SortType.DURATION -> "По длительности"
                    }

                    TextButton(
                        onClick = { onSortSelected(sortType) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = sortName,
                            color = Color.White
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть", color = Color.White)
            }
        },
        containerColor = RadioColor.Slate900,
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongContextBottomSheet(
    song: Song,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onAddToFavorites: () -> Unit,
    onChangeCover: () -> Unit,
    onRename: () -> Unit,
    onShare: () -> Unit,
    onSearchYouTube: () -> Unit,
    onShowInfo: () -> Unit
) {
    var isFavorite by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = RadioColor.Slate900,
        contentColor = Color.White,
        dragHandle = {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(
                        RadioColor.Gray400.copy(alpha = 0.5f),
                        RoundedCornerShape(2.dp)
                    )
            )
        },
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Заголовок с информацией о песне
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = song.title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = song.artist,
                    color = RadioColor.Gray400,
                    fontSize = 16.sp,
                    maxLines = 1
                )
            }

            HorizontalDivider(
                color = RadioColor.White10,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Добавить в Любимые
            SongMenuOption(
                icon = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                text = if (isFavorite) "Удалить из Любимых" else "Добавить в Любимые",
                onClick = {
                    isFavorite = !isFavorite
                    onAddToFavorites()
                },
                iconTint = if (isFavorite) Color.Red else Color.White
            )

            // Сменить Обложку
            SongMenuOption(
                icon = Icons.Filled.Image,
                text = "Сменить Обложку",
                onClick = onChangeCover
            )

            // Переименовать Файл
            SongMenuOption(
                icon = Icons.Filled.Edit,
                text = "Переименовать Файл",
                onClick = onRename
            )

            // Поделиться Треком
            SongMenuOption(
                icon = Icons.Filled.Share,
                text = "Поделиться Треком",
                onClick = onShare
            )

            // Искать на YouTube
            SongMenuOption(
                icon = Icons.Filled.PlayCircle,
                text = "Искать на YouTube",
                onClick = onSearchYouTube,
                iconTint = Color.Red
            )

            // Информация о Файле
            SongMenuOption(
                icon = Icons.Filled.Info,
                text = "Информация о Файле",
                onClick = onShowInfo
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SongMenuOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    iconTint: Color = Color.White
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            color = Color.White,
            fontSize = 16.sp
        )
    }
}
