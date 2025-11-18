package com.example.finlogcalc.radio.ui.RadioMusic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

data class Playlist(
    val id: String,
    val name: String,
    val songCount: Int,
    val coverArtUris: List<android.net.Uri>? = null,
    val isFavoritePlaylist: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen() {
    var playlists by remember { mutableStateOf<List<Playlist>>(emptyList()) }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    
    // Инициализация плейлистов
    LaunchedEffect(Unit) {
        // TODO: Загрузить плейлисты из базы данных или хранилища
        playlists = listOf(
            Playlist(
                id = "favorites",
                name = "Любимые Песни",
                songCount = 0,
                isFavoritePlaylist = true
            )
        )
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Заголовок
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(RadioColor.Slate900.copy(alpha = 0.9f))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Плейлисты",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Сетка плейлистов
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Карточка "Создать Новый Плейлист"
            item {
                CreatePlaylistCard(
                    onClick = { showCreatePlaylistDialog = true }
                )
            }
            
            // Карточка "Любимые Песни"
            val favoritesPlaylist = playlists.find { it.isFavoritePlaylist }
            if (favoritesPlaylist != null) {
                item {
                    FavoritePlaylistCard(
                        playlist = favoritesPlaylist,
                        onClick = {
                            // TODO: Открыть плейлист любимых песен
                        }
                    )
                }
            }
            
            // Остальные плейлисты
            val userPlaylists = playlists.filter { !it.isFavoritePlaylist }
            items(userPlaylists) { playlist ->
                PlaylistCard(
                    playlist = playlist,
                    onClick = {
                        // TODO: Открыть плейлист
                    },
                    onLongClick = {
                        // TODO: Показать контекстное меню плейлиста
                    }
                )
            }
        }
    }
    
    // Диалог создания нового плейлиста
    if (showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            onDismiss = { showCreatePlaylistDialog = false },
            onCreate = { playlistName ->
                // TODO: Создать новый плейлист
                playlists = playlists + Playlist(
                    id = System.currentTimeMillis().toString(),
                    name = playlistName,
                    songCount = 0
                )
                showCreatePlaylistDialog = false
            }
        )
    }
}

@Composable
fun CreatePlaylistCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(RadioColor.PlayButtonGradient)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Создать плейлист",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Создать Новый Плейлист",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun FavoritePlaylistCard(
    playlist: Playlist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFF6B9D),
                        Color(0xFFC2185B)
                    )
                )
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = "Любимые",
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = playlist.name,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${playlist.songCount} ${if (playlist.songCount == 1) "песня" else "песен"}",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun PlaylistCard(
    playlist: Playlist,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(RadioColor.Slate800)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Обложка плейлиста
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .background(RadioColor.Slate700)
        ) {
            if (playlist.coverArtUris != null && playlist.coverArtUris.isNotEmpty()) {
                // Композиция из 4 обложек
                if (playlist.coverArtUris.size >= 4) {
                    PlaylistCoverGrid(
                        coverUris = playlist.coverArtUris.take(4)
                    )
                } else {
                    // Если обложек меньше 4, показываем первую
                    AsyncImage(
                        model = playlist.coverArtUris.first(),
                        contentDescription = playlist.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                // Дефолтная обложка
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    RadioColor.Slate700,
                                    RadioColor.Slate800
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlaylistPlay,
                        contentDescription = null,
                        tint = RadioColor.Gray400,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
        
        // Информация о плейлисте
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = playlist.name,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${playlist.songCount} ${if (playlist.songCount == 1) "песня" else "песен"}",
                color = RadioColor.Gray400,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun PlaylistCoverGrid(
    coverUris: List<android.net.Uri>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Левая верхняя четверть
        AsyncImage(
            model = coverUris[0],
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .fillMaxHeight(0.5f)
                .align(Alignment.TopStart),
            contentScale = ContentScale.Crop
        )
        
        // Правая верхняя четверть
        AsyncImage(
            model = coverUris[1],
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .fillMaxHeight(0.5f)
                .align(Alignment.TopEnd),
            contentScale = ContentScale.Crop
        )
        
        // Левая нижняя четверть
        AsyncImage(
            model = coverUris[2],
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .fillMaxHeight(0.5f)
                .align(Alignment.BottomStart),
            contentScale = ContentScale.Crop
        )
        
        // Правая нижняя четверть
        AsyncImage(
            model = coverUris[3],
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .fillMaxHeight(0.5f)
                .align(Alignment.BottomEnd),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var playlistName by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Создать новый плейлист",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            OutlinedTextField(
                value = playlistName,
                onValueChange = { playlistName = it },
                placeholder = { Text("Название плейлиста", color = RadioColor.Gray400) },
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
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (playlistName.isNotBlank()) {
                        onCreate(playlistName)
                    }
                },
                enabled = playlistName.isNotBlank()
            ) {
                Text("Создать", color = RadioColor.Cyan400)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = Color.White)
            }
        },
        containerColor = RadioColor.Slate900,
        titleContentColor = Color.White,
        textContentColor = Color.White
    )
}
