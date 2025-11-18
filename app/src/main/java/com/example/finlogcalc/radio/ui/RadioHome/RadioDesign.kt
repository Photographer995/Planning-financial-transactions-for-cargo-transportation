package com.example.finlogcalc.radio.ui.RadioHome

import android.graphics.BitmapFactory
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import com.example.finlogcalc.radio.data.RadioStation
import androidx.compose.animation.AnimatedVisibility // For AnimatedVisibility
import androidx.compose.animation.fadeIn // For fadeIn
import androidx.compose.animation.fadeOut // For fadeOut
import androidx.compose.animation.expandHorizontally // For expandHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically

// --- ОСНОВНОЙ ФОН ---
@Composable
fun NeonBackgroundRadio(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF10101A))
    ) {
        content()
    }
}

// --- ДИНАМИЧЕСКИЙ ФОН ПО ОБЛОЖКЕ ---
@Composable
fun DynamicArtworkBackground(
    imageRes: Int,
    blurRadius: Dp = 36.dp,
    overlayAlpha: Float = 0.85f,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val targetColors = remember(imageRes) {
        val bmp = BitmapFactory.decodeResource(context.resources, imageRes)
        val p = Palette.from(bmp).clearFilters().generate()
        val dominant = Color(p.getDominantColor(0xFF1A0A0A.toInt()))
        val vibrant = Color(p.getVibrantColor(0xFF8A2A2A.toInt()))
        val darkVibrant = Color(p.getDarkVibrantColor(0xFF3A0F0F.toInt()))
        Triple(dominant, vibrant, darkVibrant)
    }
    val dominant by animateColorAsState(targetColors.first, label = "bgDominant")
    val vibrant by animateColorAsState(targetColors.second, label = "bgVibrant")
    val darkVibrant by animateColorAsState(targetColors.third, label = "bgDarkVibrant")

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
                .blur(blurRadius)
                .alpha(0.9f)
        )
        // Цветовая заливка, привязанная к палитре
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            darkVibrant.copy(alpha = overlayAlpha),
                            dominant.copy(alpha = overlayAlpha * 0.9f),
                            vibrant.copy(alpha = overlayAlpha * 0.6f),
                            Color.Black.copy(alpha = 0.55f)
                        )
                    )
                )
        )
        // Лёгкое затемнение сверху/снизу для лучшей читаемости
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.25f),
                            Color.Transparent,
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.35f)
                        )
                    )
                )
        )
        content()
    }
}

// --- АНИМАЦИЯ ВОЛНЫ ---
@Composable
fun SoundWaveIndicator(
    modifier: Modifier = Modifier,
    color: Color = NeonColorsRadio.Cyan400
) {
    val transition = rememberInfiniteTransition()
    val animatedValues = (1..4).map {
        transition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 800 + (it * 100)
                    0.2f at 0
                    1f at 400
                    0.2f at 800
                },
                repeatMode = RepeatMode.Restart
            )
        )
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        animatedValues.forEach { animatedValue ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(16.dp * animatedValue.value)
                    .background(color, RoundedCornerShape(1.dp))
            )
        }
    }
}

@Composable
fun NeonFavoriteIcon(
    isFavorite: Boolean,
    modifier: Modifier = Modifier
) {
    val heartColor = if (isFavorite) NeonColorsRadio.Pink500 else Color.White.copy(alpha = 0.6f)

    Icon(
        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
        contentDescription = "Favorite",
        tint = heartColor,
        modifier = modifier
            .size(30.dp)
            .graphicsLayer(
                shadowElevation = if (isFavorite) 10f else 0f,
                spotShadowColor = NeonColorsRadio.Pink500
            )
    )
}

@Composable
fun NeonFilterChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val backgroundBrush = if (selected) {
        Brush.linearGradient(
            listOf(
                NeonColorsRadio.Cyan400.copy(alpha = 0.35f),
                NeonColorsRadio.Purple500.copy(alpha = 0.35f)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = 0.05f),
                Color.White.copy(alpha = 0.02f)
            )
        )
    }

    val borderBrush = if (selected) {
        Brush.linearGradient(listOf(NeonColorsRadio.Cyan400, NeonColorsRadio.Purple500))
    } else {
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = 0.12f),
                Color.White.copy(alpha = 0.05f)
            )
        )
    }

    val contentColor by animateColorAsState(
        targetValue = if (selected) Color.White else Color.White.copy(alpha = 0.75f),
        label = "chipContent"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(brush = backgroundBrush)
            .border(1.dp, borderBrush, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            color = contentColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// --- ЭЛЕМЕНТ СПИСКА РАДИОСТАНЦИЙ (УЛУЧШЕННЫЙ ДИЗАЙН) ---
@Composable
fun RadioStationRow(
    station: RadioStation,
    isFavorite: Boolean,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(24.dp)
    val accentBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.08f),
            Color.White.copy(alpha = 0.02f)
        )
    )

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        if (isPlaying) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(cardShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                NeonColorsRadio.Cyan400.copy(alpha = 0.55f),
                                NeonColorsRadio.Purple500.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        )
                    )
                    .blur(30.dp)
            )
        }

        Row(
            modifier = Modifier
            .fillMaxWidth()
                .clip(cardShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.White.copy(alpha = 0.02f)
                        )
                    )
                )
            .border(
                    width = 1.5.dp,
                    brush = if (isPlaying) {
                        Brush.linearGradient(
                            colors = listOf(NeonColorsRadio.Cyan400, NeonColorsRadio.Purple500)
                        )
                    } else accentBrush,
                    shape = cardShape
            )
            .clickable(onClick = onPlayClick)
                .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(18.dp))
            ) {
            Image(
                painter = painterResource(id = station.logoRes),
                contentDescription = station.name,
                contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
            )

            if (isPlaying) {
                Box(
                    modifier = Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(alpha = 0.25f))
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(40))
                            .background(NeonColorsRadio.Cyan400.copy(alpha = 0.2f))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SoundWaveIndicator(
                                modifier = Modifier.height(12.dp),
                                color = NeonColorsRadio.Cyan400
                            )
                            Text(
                                text = "Live",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.width(18.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
            Text(
                text = station.name,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
            )
                station.description?.let {
            Text(
                        text = it,
                        color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }
                station.genre?.let {
                    Text(
                        text = it,
                        color = NeonColorsRadio.Cyan400.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }

        IconButton(
            onClick = onFavoriteClick,
                modifier = Modifier.size(44.dp)
        ) {
            NeonFavoriteIcon(isFavorite = isFavorite)
        }

            Spacer(modifier = Modifier.width(8.dp))

        IconButton(
            onClick = onPlayClick,
                modifier = Modifier.size(54.dp)
        ) {
            Box(
                modifier = Modifier
                        .size(54.dp)
                    .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    NeonColorsRadio.Cyan400,
                                    NeonColorsRadio.Cyan600
                                )
                            )
                        ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                        modifier = Modifier.size(26.dp)
                )
                }
            }
        }
    }
}

// --- КАРТОЧКА ИЗБРАННОЙ СТАНЦИИ ---
@Composable
fun FavoriteCard(
    station: RadioStation,
    isPlaying: Boolean,
    onToggleFavorite: () -> Unit,
    onPlay: () -> Unit,
    index: Int,
    modifier: Modifier = Modifier
) {
    val gradientPair = NeonColorsRadio.gradientPairs[index % NeonColorsRadio.gradientPairs.size]
    val scale by animateFloatAsState(
        targetValue = if (isPlaying) 1.05f else 1f,
        label = "scale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(192.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onPlay)
    ) {
        // Background Image
        Image(
            painter = painterResource(id = station.imageRes),
            contentDescription = station.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .scale(scale)
        )
        
        // Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            gradientPair.first.copy(alpha = 0.6f),
                            gradientPair.second.copy(alpha = 0.6f)
                        )
                    )
                )
        )
        
        // Dark gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.5f),
                            Color.Black.copy(alpha = 0.8f)
                        )
                    )
                )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                if (isPlaying) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SoundWaveIndicator(
                            modifier = Modifier.height(12.dp),
                            color = NeonColorsRadio.Cyan400
                        )
                        Text(
                            text = "Играет сейчас",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Spacer(modifier = Modifier)
                }
                
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(48.dp)
                ) {
                    NeonFavoriteIcon(isFavorite = true)
                }
            }

            // Bottom Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = station.name,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = station.description ?: station.genre ?: "",
                        color = Color.Gray.copy(alpha = 0.9f),
                        fontSize = 14.sp
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                IconButton(
                    onClick = onPlay,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                if (isPlaying) Color.White else Color.White.copy(alpha = 0.9f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

// --- СЕКЦИЯ ИЗБРАННЫХ ---
@Composable
fun FavoritesSection(
    stations: List<RadioStation>,
    onToggleFavorite: (String) -> Unit,
    onPlay: (RadioStation) -> Unit,
    currentPlaying: RadioStation?,
    modifier: Modifier = Modifier
) {
    if (stations.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                NeonColorsRadio.Pink600.copy(alpha = 0.2f),
                                NeonColorsRadio.Purple600.copy(alpha = 0.2f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = NeonColorsRadio.Pink500,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Нет избранных станций",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Добавьте станции в избранное, нажав на звездочку, чтобы видеть их здесь",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    } else {
        Column(modifier = modifier.padding(horizontal = 16.dp)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(NeonColorsRadio.Pink600, NeonColorsRadio.Purple600)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "Избранное",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ваши любимые радиостанции",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }

            // Cards Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(
                    items = stations,
                    key = { it.id }
                ) { station ->
                    val index = stations.indexOf(station)
                    FavoriteCard(
                        station = station,
                        isPlaying = currentPlaying?.id == station.id,
                        onToggleFavorite = { onToggleFavorite(station.id) },
                        onPlay = { onPlay(station) },
                        index = index
                    )
                }
            }
        }
    }
}

// --- РЕКОМЕНДУЕМЫЙ БАННЕР ---
@Composable
fun RecommendedBanner(
    station: RadioStation,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(192.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onPlay)
    ) {
        // Background Image
        Image(
            painter = painterResource(id = station.imageRes),
            contentDescription = station.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        
        // Dark gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.9f),
                            Color.Black.copy(alpha = 0.6f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Content
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(NeonColorsRadio.Pink600, NeonColorsRadio.Purple600)
                            )
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = "Топ выбор",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = station.name,
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = station.description ?: station.genre ?: "",
                    color = Color.Gray.copy(alpha = 0.9f),
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            IconButton(
                onClick = onPlay,
                modifier = Modifier.size(64.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

// --- ОБНОВЛЕННЫЙ МИНИ-ПЛЕЕР ---
@Composable
fun PersistentPlayerCard(
    station: RadioStation,
    isPlaying: Boolean,
    isFavorite: Boolean,
    onPlayPauseClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape),
        color = Color.Transparent,
        tonalElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0a0a12).copy(alpha = 0.98f),
                            Color(0xFF0a0a12)
                        )
                    )
                )
                .border(
                    width = 1.2.dp,
                    brush = Brush.linearGradient(
                        listOf(NeonColorsRadio.Cyan400.copy(alpha = 0.5f), NeonColorsRadio.Purple500.copy(alpha = 0.4f))
                    ),
                    shape = shape
                )
                .clickable(onClick = onExpand)
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(RoundedCornerShape(18.dp))
                ) {
                    Image(
                        painter = painterResource(id = station.logoRes),
                        contentDescription = station.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(alpha = 0.15f))
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .clip(RoundedCornerShape(40))
                            .background(NeonColorsRadio.Cyan400.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Live",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                        Text(
                            text = station.name,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                        Text(
                        text = "Сейчас играет",
                        color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }

                    IconButton(
                    onClick = {
                        onToggleFavorite()
                    },
                    modifier = Modifier.size(42.dp)
                ) {
                    NeonFavoriteIcon(isFavorite = isFavorite)
                }

                IconButton(
                    onClick = onPlayPauseClick,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(NeonColorsRadio.Purple500, NeonColorsRadio.Cyan400)
                            )
                        )
                    ) {
                        Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                    IconButton(
                    onClick = onExpand,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                        imageVector = Icons.Filled.UnfoldMore,
                        contentDescription = "Expand Player",
                        tint = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun FullScreenRadioPlayer(
    station: RadioStation,
    availableStations: List<RadioStation>,
    isPlaying: Boolean,
    isFavorite: Boolean,
    sleepRemainingMs: Long? = null,
    trackMetadata: com.example.finlogcalc.radio.ui.viewmodel.RadioPlayerViewModel.TrackMetadata? = null,
    onPlayPause: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDismiss: () -> Unit,
    onStationSelected: (RadioStation) -> Unit,
    onSleepTimer: (Int?) -> Unit
) {
    var currentIndex by remember(station, availableStations) {
        mutableIntStateOf(availableStations.indexOfFirst { it.id == station.id }.takeIf { it >= 0 } ?: 0)
    }
    var horizontalDragOffset by remember { mutableStateOf(0f) }
    var verticalDragOffset by remember { mutableStateOf(0f) }
    var isMetadataExpanded by remember { mutableStateOf(false) }
    val stationCount = availableStations.size
    var shuffleEnabled by remember { mutableStateOf(false) }
    var showTimerMenu by remember { mutableStateOf(false) }

    DynamicArtworkBackground(
        imageRes = station.imageRes,
        blurRadius = 48.dp,
        overlayAlpha = 0.9f
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ExpandMore,
                    contentDescription = "Close Player",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Верхняя секция: название, жанр, кнопка "+"
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = station.name,
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = station.genre ?: station.description ?: "Радиостанция",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 16.sp
                            )
                        }
                        
                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.Add,
                                    contentDescription = if (isFavorite) "Удалить из избранного" else "Добавить в избранное",
                                    tint = Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                // Центральная секция: обложка с превью по бокам (правильно центрированные)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(stationCount) {
                            detectHorizontalDragGestures(
                                onHorizontalDrag = { _, dragAmount ->
                                    horizontalDragOffset += dragAmount
                                },
                                onDragEnd = {
                                    if (stationCount > 1) {
                                        if (horizontalDragOffset < -80f) {
                                            val nextIndex = (currentIndex + 1) % stationCount
                                            currentIndex = nextIndex
                                            onStationSelected(availableStations[nextIndex])
                                        } else if (horizontalDragOffset > 80f) {
                                            val previousIndex = (currentIndex - 1 + stationCount) % stationCount
                                            currentIndex = previousIndex
                                            onStationSelected(availableStations[previousIndex])
                                        }
                                    }
                                    horizontalDragOffset = 0f
                                },
                                onDragCancel = { horizontalDragOffset = 0f }
                            )
                        }
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onVerticalDrag = { _, dragAmount ->
                                    verticalDragOffset += dragAmount
                                },
                                onDragEnd = {
                                    if (verticalDragOffset < -100f) {
                                        // Свайп вверх - показать метаданные
                                        isMetadataExpanded = true
                                    } else if (verticalDragOffset > 100f) {
                                        // Свайп вниз - скрыть метаданные
                                        isMetadataExpanded = false
                                    }
                                    verticalDragOffset = 0f
                                },
                                onDragCancel = { verticalDragOffset = 0f }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val previousStation = availableStations.takeIf { it.isNotEmpty() }?.let {
                        if (stationCount > 1) it[(currentIndex - 1 + stationCount) % stationCount] else null
                    }
                    val nextStation = availableStations.takeIf { it.isNotEmpty() }?.let {
                        if (stationCount > 1) it[(currentIndex + 1) % stationCount] else null
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Предыдущая станция (слева)
                        previousStation?.let { preview ->
                            Image(
                                painter = painterResource(id = preview.logoRes),
                                contentDescription = preview.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .alpha(0.4f)
                                    .padding(end = 16.dp)
                            )
                        } ?: Spacer(modifier = Modifier.width(80.dp))

                        // Основная обложка (центр)
                        Box(
                            modifier = Modifier
                                .size(280.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.radialGradient(
                                        listOf(
                                            NeonColorsRadio.Purple500.copy(alpha = 0.6f),
                                            NeonColorsRadio.Cyan400.copy(alpha = 0.4f),
                                            Color.Transparent
                                        )
                                    )
                                )
                                .border(
                                    width = 3.dp,
                                    brush = Brush.linearGradient(
                                        listOf(NeonColorsRadio.Cyan400, NeonColorsRadio.Purple500)
                                    ),
                                    shape = CircleShape
                                )
                                .padding(12.dp)
                        ) {
                            Image(
                                painter = painterResource(id = station.imageRes),
                                contentDescription = station.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }

                        // Следующая станция (справа)
                        nextStation?.let { preview ->
                            Image(
                                painter = painterResource(id = preview.logoRes),
                                contentDescription = preview.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .alpha(0.4f)
                                    .padding(start = 16.dp)
                            )
                        } ?: Spacer(modifier = Modifier.width(80.dp))
                    }
                }

                // Нижняя секция: контролы плеера
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Основные контролы: Shuffle, Previous, Play/Pause, Next, Sleep Timer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Shuffle
                        IconToggleButton(
                            checked = shuffleEnabled,
                            onCheckedChange = { shuffleEnabled = it },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.06f))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Shuffle,
                                contentDescription = "Shuffle",
                                tint = if (shuffleEnabled) Color(0xFF4CAF50) else Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Previous
                        IconButton(
                            onClick = {
                                if (stationCount > 1) {
                                    val previousIndex = (currentIndex - 1 + stationCount) % stationCount
                                    currentIndex = previousIndex
                                    onStationSelected(availableStations[previousIndex])
                                }
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.06f))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SkipPrevious,
                                contentDescription = "Previous",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Play/Pause (большая центральная кнопка)
                        IconButton(
                            onClick = onPlayPause,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = Color.Black,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        // Next
                        IconButton(
                            onClick = {
                                if (stationCount > 0) {
                                    val nextIndex = if (shuffleEnabled) {
                                        (0 until stationCount).filter { it != currentIndex }.random()
                                    } else {
                                        (currentIndex + 1) % stationCount
                                    }
                                    currentIndex = nextIndex
                                    onStationSelected(availableStations[nextIndex])
                                }
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.06f))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SkipNext,
                                contentDescription = "Next",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Sleep Timer
                        Box {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                    IconButton(
                                        onClick = { showTimerMenu = true },
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.06f))
                                            .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Schedule,
                                            contentDescription = "Таймер сна",
                                            tint = if (sleepRemainingMs != null) Color(0xFF4CAF50) else Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    // Оставшееся время с анимацией появления/скрытия
                                    AnimatedVisibility(
                                        visible = sleepRemainingMs != null,
                                        enter = fadeIn() + expandHorizontally(),
                                        exit = fadeOut()
                                    ) {
                                        val ms = sleepRemainingMs ?: 0L
                                        val totalSeconds = (ms / 1000).toInt().coerceAtLeast(0)
                                        val minutes = totalSeconds / 60
                                        val seconds = totalSeconds % 60
                                        Text(
                                            text = String.format("%02d:%02d", minutes, seconds),
                                            color = Color.White.copy(alpha = 0.8f),
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                            DropdownMenu(
                                expanded = showTimerMenu,
                                onDismissRequest = { showTimerMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("5 минут") },
                                    onClick = { showTimerMenu = false; onSleepTimer(5) }
                                )
                                DropdownMenuItem(
                                    text = { Text("10 минут") },
                                    onClick = { showTimerMenu = false; onSleepTimer(10) }
                                )
                                DropdownMenuItem(
                                    text = { Text("15 минут") },
                                    onClick = { showTimerMenu = false; onSleepTimer(15) }
                                )
                                DropdownMenuItem(
                                    text = { Text("30 минут") },
                                    onClick = { showTimerMenu = false; onSleepTimer(30) }
                                )
                                DropdownMenuItem(
                                    text = { Text("60 минут") },
                                    onClick = { showTimerMenu = false; onSleepTimer(60) }
                                )
                                DropdownMenuItem(
                                    text = { Text("90 минут") },
                                    onClick = { showTimerMenu = false; onSleepTimer(90) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Отменить") },
                                    onClick = { showTimerMenu = false; onSleepTimer(null) }
                                )
                            }
                        }
                    }
                }
                
                // Панель метаданных (появляется при свайпе вверх)
                AnimatedVisibility(
                    visible = isMetadataExpanded,
                    enter = fadeIn() + slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = androidx.compose.animation.core.tween(300)
                    ),
                    exit = fadeOut() + slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = androidx.compose.animation.core.tween(300)
                    )
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        color = Color.Black.copy(alpha = 0.7f),
                        tonalElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Информация о треке",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = { isMetadataExpanded = false },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ExpandMore,
                                        contentDescription = "Скрыть",
                                        tint = Color.White
                                    )
                                }
                            }
                            
                            Divider(color = Color.White.copy(alpha = 0.2f))
                            
                            trackMetadata?.let { metadata ->
                                if (metadata.title != null || metadata.artist != null) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        metadata.title?.let { title ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = "Название:",
                                                    color = Color.White.copy(alpha = 0.7f),
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    text = title,
                                                    color = Color.White,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                        
                                        metadata.artist?.let { artist ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = "Исполнитель:",
                                                    color = Color.White.copy(alpha = 0.7f),
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    text = artist,
                                                    color = Color.White,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                        
                                        metadata.album?.let { album ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = "Альбом:",
                                                    color = Color.White.copy(alpha = 0.7f),
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    text = album,
                                                    color = Color.White,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                        
                                        metadata.genre?.let { genre ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    text = "Жанр:",
                                                    color = Color.White.copy(alpha = 0.7f),
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    text = genre,
                                                    color = Color.White,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "Метаданные пока недоступны",
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            } ?: run {
                                Text(
                                    text = "Метаданные пока недоступны",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "↑ Свайпните вниз, чтобы скрыть",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FavoritesOverlay(
    stations: List<RadioStation>,
    recommended: List<RadioStation>,
    currentPlaying: RadioStation?,
    isPlaying: Boolean,
    onPlayStation: (RadioStation) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        Color(0xFF05050A).copy(alpha = 0.98f),
                        Color(0xFF14072A)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Избранное",
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ваши любимые радиостанции",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
                IconButton(onClick = onClose) {
                        Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Закрыть",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (stations.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Добавьте станции в избранное",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Нажмите на сердечко возле понравившейся станции",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(stations, key = { it.id }) { station ->
                        val playing = currentPlaying?.id == station.id && isPlaying
                        FavoriteLargeTile(
                            station = station,
                            isPlaying = playing,
                            onPlay = { onPlayStation(station) },
                            onToggleFavorite = { onToggleFavorite(station.id) }
                        )
                    }

                    if (recommended.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Рекомендуемое",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(recommended, key = { it.id }) { suggestion ->
                                    RecommendedTile(
                                        station = suggestion,
                                        onPlay = { onPlayStation(suggestion) }
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

@Composable
private fun FavoriteLargeTile(
    station: RadioStation,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val shape = RoundedCornerShape(28.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clip(shape)
            .clickable(onClick = onPlay)
    ) {
        Image(
            painter = painterResource(id = station.imageRes),
            contentDescription = station.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.linearGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.7f),
                            Color.Black.copy(alpha = 0.2f)
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    width = 2.dp,
                    brush = if (isPlaying) {
                        Brush.linearGradient(
                            listOf(NeonColorsRadio.Cyan400, NeonColorsRadio.Purple500)
                        )
                    } else Brush.linearGradient(
                        listOf(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.05f))
                    ),
                    shape = shape
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isPlaying) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.18f))
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SoundWaveIndicator(
                                modifier = Modifier.height(14.dp),
                                color = NeonColorsRadio.Cyan400
                            )
                            Text(
                                text = "Играет сейчас",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    IconButton(onClick = onToggleFavorite) {
                        NeonFavoriteIcon(isFavorite = true)
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = station.name,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = station.description ?: station.genre.orEmpty(),
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 14.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onPlay,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.radialGradient(
                                        listOf(NeonColorsRadio.Purple500, NeonColorsRadio.Cyan400)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                    ) {
                        Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendedTile(
    station: RadioStation,
    onPlay: () -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = Modifier
            .width(240.dp)
            .height(160.dp)
            .clip(shape)
            .clickable(onClick = onPlay)
    ) {
        Image(
            painter = painterResource(id = station.imageRes),
            contentDescription = station.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.75f),
                            Color.Black.copy(alpha = 0.2f)
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        listOf(NeonColorsRadio.Pink500, NeonColorsRadio.Purple500)
                    ),
                    shape = shape
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(NeonColorsRadio.Pink500.copy(alpha = 0.3f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Топ выбор",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column {
                    Text(
                        text = station.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = station.description ?: station.genre.orEmpty(),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
