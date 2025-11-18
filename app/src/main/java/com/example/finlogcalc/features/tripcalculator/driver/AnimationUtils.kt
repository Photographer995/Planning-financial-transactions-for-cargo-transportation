package com.example.finlogcalc.features.tripcalculator.driver

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity

/**
 * Утилиты для анимаций в стиле Deep Neon
 */

/**
 * Анимация появления элемента с эффектом свечения
 */
@Composable
fun AnimatedVisibilityWithGlow(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 500,
                easing = FastOutSlowInEasing
            )
        ) + expandVertically(
            animationSpec = tween(
                durationMillis = 500,
                easing = FastOutSlowInEasing
            )
        ),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutLinearInEasing
            )
        ) + shrinkVertically(
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutLinearInEasing
            )
        )
    ) {
        content()
    }
}

/**
 * Анимация пульсации для неоновых элементов
 */
@Composable
fun rememberPulseAnimation(
    durationMillis: Int = 2000,
    minAlpha: Float = 0.6f,
    maxAlpha: Float = 1f
): Float {
    return rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = minAlpha,
        targetValue = maxAlpha,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    ).value
}

/**
 * Анимация свечения для кнопок и интерактивных элементов
 */
@Composable
fun rememberGlowAnimation(
    durationMillis: Int = 2000
): Float {
    return rememberPulseAnimation(durationMillis = durationMillis)
}

/**
 * Анимация изменения числового значения с эффектом "глитча"
 */
@Composable
fun <T> animateValueAsState(
    targetValue: T,
    animationSpec: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    ),
    label: String = "animatedValue"
): T where T : Number {
    val animated = animateFloatAsState(
        targetValue = targetValue.toFloat(),
        animationSpec = animationSpec,
        label = label
    )
    
    @Suppress("UNCHECKED_CAST")
    return when (targetValue) {
        is Int -> animated.value.toInt() as T
        is Long -> animated.value.toLong() as T
        is Float -> animated.value as T
        is Double -> animated.value.toDouble() as T
        else -> targetValue
    }
}

/**
 * Параллакс-эффект при прокрутке
 */
@Composable
fun ParallaxBox(
    modifier: Modifier = Modifier,
    parallaxFactor: Float = 0.5f,
    content: @Composable BoxScope.() -> Unit
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    
    Box(
        modifier = modifier
            .graphicsLayer {
                translationY = scrollState.value * parallaxFactor * density.density
            }
    ) {
        content()
    }
}

/**
 * Анимация загрузки с эффектом "глитча"
 */
@Composable
fun GlitchLoadingAnimation(
    modifier: Modifier = Modifier,
    text: String = "Загрузка..."
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glitch")
    
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 100,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetX"
    )
    
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 150,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offsetY"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 500,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Box(
        modifier = modifier
            .graphicsLayer {
                translationX = (offsetX - 0.5f) * 4f
                translationY = (offsetY - 0.5f) * 4f
                this.alpha = alpha
            }
    ) {
        // Здесь можно добавить текст или индикатор загрузки
    }
}

/**
 * Микро-анимация при нажатии (scale эффект)
 */
@Composable
fun rememberPressAnimation(): Float {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "pressScale"
    )
    
    return scale
}

/**
 * Модификатор для микро-анимации при взаимодействии
 */
fun Modifier.microInteraction(
    scale: Float = 0.95f
): Modifier {
    return this.scale(scale)
}
