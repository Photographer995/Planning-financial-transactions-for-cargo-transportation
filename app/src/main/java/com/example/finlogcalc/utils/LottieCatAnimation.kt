package com.example.finlogcalc.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.example.finlogcalc.R

@Composable
fun LottieCatAnimation(
    modifier: Modifier = Modifier,
    isPlaying: Boolean, // Control animation playback
    onCatClick: () -> Unit
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.cat))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = isPlaying,
        speed = 1f,
        restartOnPlay = false
    )

    LottieAnimation(
        composition = composition,
        progress = { progress }, // Pass progress as a lambda
        modifier = modifier
            .size(150.dp)
            .clickable { onCatClick() }
    )
}