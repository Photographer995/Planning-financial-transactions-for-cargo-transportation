package com.example.finlogcalc.radio.ui.RadioMusic

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.util.Locale
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

// --- Сегментированный контроль для вкладок ---
@Composable
fun MusicTabSegmentedControl(
    selectedTab: MusicViewModel.MusicTab,
    onTabSelected: (MusicViewModel.MusicTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RadioColor.Slate800)
            .border(1.dp, RadioColor.White10, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MusicViewModel.MusicTab.entries.forEach { tab ->
            val isSelected = selectedTab == tab
            val tabName = when (tab) {
                MusicViewModel.MusicTab.SONGS -> "Песни"
                MusicViewModel.MusicTab.PLAYLISTS -> "Плейлисты"
                MusicViewModel.MusicTab.FOLDERS -> "Папки"
            }
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onTabSelected(tab) }
                    .background(
                        if (isSelected) {
                            Brush.horizontalGradient(RadioColor.PlayButtonGradient)
                        } else {
                            Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                        }
                    )
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tabName,
                    color = if (isSelected) Color.White else RadioColor.Gray400,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// --- Область "Сейчас играет" с большой обложкой ---
@Composable
fun NowPlayingArea(
    song: Song?,
    isPlaying: Boolean,
    playbackPosition: Long,
    totalDuration: Long,
    onPlayClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    song?.let {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Большая обложка альбома
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(RadioColor.Slate800)
            ) {
                if (it.albumArtUri != null) {
                    AsyncImage(
                        model = it.albumArtUri,
                        contentDescription = it.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(RadioColor.PlayButtonGradient)
                            )
                            .graphicsLayer(alpha = 0.99f) // Для эффекта размытия
                            .drawWithContent {
                                drawContent()
                                drawRect(
                                    color = Color.Black.copy(alpha = 0.3f),
                                    blendMode = BlendMode.SrcOver
                                )
                            } // Эффект "стекла"
                            .blur(radius = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MusicNote,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }
                
                // Текст поверх обложки
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                )
                            )
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = it.title,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = it.artist,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = it.album,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Кнопки управления
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Кнопка Previous
                IconButton(
                    onClick = onPreviousClick,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .border(1.dp, RadioColor.Gray400, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipPrevious,
                        contentDescription = "Previous",
                        tint = RadioColor.Gray400,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Кнопка Play/Pause
                IconButton(
                    onClick = onPlayClick,
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            Brush.radialGradient(RadioColor.PlayButtonGradient),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Кнопка Next
                IconButton(
                    onClick = onNextClick,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .border(1.dp, RadioColor.Gray400, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "Next",
                        tint = RadioColor.Gray400,
                        modifier = Modifier.size(28.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Кнопка Shuffle
                OutlinedButton(
                    onClick = onShuffleClick,
                    modifier = Modifier
                        .height(48.dp)
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, RadioColor.Cyan400)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shuffle,
                        contentDescription = "Shuffle",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Прогресс-бар
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                val currentSeconds = (playbackPosition / 1000).toInt()
                val totalSeconds = (totalDuration / 1000).toInt()
                val currentMin = currentSeconds / 60
                val currentSec = currentSeconds % 60
                val totalMin = totalSeconds / 60
                val totalSec = totalSeconds % 60
                
                val progress = if (totalDuration > 0) {
                    playbackPosition.toFloat() / totalDuration.toFloat()
                } else {
                    0f
                }

                WaveformSeekBar(
                    progress = progress,
                    onSeek = onSeek,
                    waveformAnimation = if (isPlaying) WaveformAnimation.LINEAR else WaveformAnimation.STATIC
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = String.format(Locale.getDefault(), "%d:%02d", currentMin, currentSec),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "%d:%02d", totalMin, totalSec),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// --- Элемент списка песен ---
@Composable
fun SongItem(
    song: Song,
    isCurrentlyPlaying: Boolean,
    onSongClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onSongClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Миниатюра обложки
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(RadioColor.Slate800)
        ) {
            if (song.albumArtUri != null) {
                AsyncImage(
                    model = song.albumArtUri,
                    contentDescription = song.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(RadioColor.PlayButtonGradient)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.MusicNote,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Информация о песне
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = song.title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = if (isCurrentlyPlaying) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = song.artist,
                color = RadioColor.Gray400,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        // Иконка "Ещё"
        IconButton(
            onClick = onMoreClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "More options",
                tint = RadioColor.Gray400,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// --- Визуализатор волн и SeekBar ---
enum class WaveformAnimation {
    STATIC,
    LINEAR
}

@Composable
fun WaveformSeekBar(
    progress: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    waveformAnimation: WaveformAnimation = WaveformAnimation.STATIC,
    activeColor: Color = RadioColor.Cyan400,
    inactiveColor: Color = RadioColor.Slate700
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val width = constraints.maxWidth
        val height = with(LocalDensity.current) { 80.dp.toPx() }
        
        val density = LocalDensity.current
        val thumbWidth = 10.dp
        val thumbHeight = 40.dp
        val thumbGlowSize = 48.dp
        val thumbWidthPx = with(density) { thumbWidth.toPx() }
        val thumbHeightPx = with(density) { thumbHeight.toPx() }
        val thumbGlowSizePx = with(density) { thumbGlowSize.toPx() }

        var phase by remember { mutableStateOf(0f) }

        if (waveformAnimation == WaveformAnimation.LINEAR) {
            LaunchedEffect(Unit) {
                while (true) {
                    withFrameNanos { 
                        phase = (it / 1_000_000_000f) * 5f
                    }
                }
            }
        }

        val numBars = 50
        val totalBarWidthPx = width.toFloat() / numBars
        val barWidthPx = totalBarWidthPx * 0.6f
        val barSpacingPx = totalBarWidthPx - barWidthPx
        val effectiveTrackStartX = barSpacingPx / 2
        val effectiveTrackWidth = width.toFloat() - barSpacingPx

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .pointerInput(effectiveTrackWidth, effectiveTrackStartX, onSeek) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val newProgress = ((offset.x - effectiveTrackStartX) / effectiveTrackWidth)
                                .coerceIn(0f, 1f)
                            onSeek(newProgress)
                        },
                        onDrag = { change, _ ->
                            val newProgress = ((change.position.x - effectiveTrackStartX) / effectiveTrackWidth)
                                .coerceIn(0f, 1f)
                            onSeek(newProgress)
                        }
                    )
                }
        ) {
            drawNeonEqualizer(
                progress = progress,
                phase = phase,
                width = width.toFloat(),
                height = height,
                activeColor = activeColor,
                inactiveColor = inactiveColor,
                numBars = numBars,
                barWidth = barWidthPx,
                barSpacing = barSpacingPx,
                effectiveTrackStartX = effectiveTrackStartX,
                effectiveTrackWidth = effectiveTrackWidth
            )
        }
        
        // Custom Thumb
        Box(
            modifier = Modifier
                .offset {
                    val thumbX = effectiveTrackStartX + (effectiveTrackWidth * progress) - (thumbGlowSizePx / 2)
                    IntOffset(
                        thumbX.roundToInt(),
                        (height / 2 - thumbGlowSizePx / 2).roundToInt()
                    )
                }
        ) {
            // Glow effect
            Box(
                modifier = Modifier
                    .size(thumbGlowSize)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                activeColor.copy(alpha = 0.5f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    )
            )
            // Vertical bar thumb
            Box(
                modifier = Modifier
                    .width(thumbWidth)
                    .height(thumbHeight)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.White, activeColor)
                        ),
                        RoundedCornerShape(thumbWidth / 2)
                    )
                    .align(Alignment.Center)
            )
        }
    }
}

private fun DrawScope.drawNeonEqualizer(
    progress: Float,
    phase: Float,
    width: Float,
    height: Float,
    activeColor: Color,
    inactiveColor: Color,
    numBars: Int = 50,
    barWidth: Float,
    barSpacing: Float,
    effectiveTrackStartX: Float,
    effectiveTrackWidth: Float
) {
    val midY = height / 2

    val activeBrush = Brush.verticalGradient(
        colors = listOf(
            activeColor.copy(alpha = 0.5f),
            activeColor,
            activeColor.copy(alpha = 0.5f)
        ),
        startY = 0f,
        endY = height
    )

    for (i in 0 until numBars) {
        val x = effectiveTrackStartX + i * (barWidth + barSpacing)
        
        val sin1 = sin(i * 0.4f + phase)
        val sin2 = cos(i * 0.1f + phase * 0.5f)
        val randomFactor = Random(i.toLong()).nextFloat() * 0.3f + 0.7f
        
        val normalizedHeight = (sin1 + sin2) / 2f * randomFactor
        val barHeight = (normalizedHeight.pow(2) * 0.7f + 0.3f) * height * 0.8f
        
        val barTop = midY - barHeight / 2
        
        val color = if (((x + barWidth / 2) - effectiveTrackStartX) / effectiveTrackWidth <= progress) activeBrush else SolidColor(inactiveColor)
        
        drawRoundRect(
            brush = color,
            topLeft = Offset(x, barTop),
            size = Size(barWidth, barHeight),
            cornerRadius = CornerRadius(barWidth / 2)
        )
    }
}
