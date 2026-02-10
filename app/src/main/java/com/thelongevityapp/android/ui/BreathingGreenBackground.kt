package com.thelongevityapp.android.ui

import android.content.Context
import android.provider.Settings
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.thelongevityapp.android.ui.theme.PrimaryGreen
import kotlin.math.max

fun isReduceMotionEnabled(context: Context): Boolean {
    return try {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) == 0f
    } catch (_: Exception) {
        false
    }
}

@Composable
fun BreathingGreenBackground(
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "breath")
    val breath by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = if (reduceMotion) infiniteRepeatable(
            animation = tween(0),
            repeatMode = RepeatMode.Restart
        ) else infiniteRepeatable(
            animation = tween(2400, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )

    // Outer: scale 0.8..1.0, opacity 0.35..0.8
    val outerScale = if (reduceMotion) 1f else (0.8f + breath * 0.2f)
    val outerAlpha = if (reduceMotion) 0.6f else (0.35f + breath * 0.45f)
    // Inner: scale 0.82..1.0, opacity 0.4..0.9
    val innerScale = if (reduceMotion) 1f else (0.82f + breath * 0.18f)
    val innerAlpha = if (reduceMotion) 0.65f else (0.4f + breath * 0.5f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(0.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = outerScale,
                    scaleY = outerScale,
                    alpha = outerAlpha
                )
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val centerX = w / 2f
                val centerY = h / 2f
                val sizeF = max(w, h) * 1.2f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PrimaryGreen.copy(alpha = 0.28f),
                            PrimaryGreen.copy(alpha = 0.1f),
                            Color.Transparent
                    ),
                        center = Offset(centerX, centerY),
                        radius = sizeF * 0.4f
                    ),
                    radius = sizeF / 2f,
                    center = Offset(centerX, centerY)
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = innerScale,
                    scaleY = innerScale,
                    alpha = innerAlpha
                )
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val centerX = w / 2f
                val centerY = h / 2f
                val sizeF = max(w, h) * 1.2f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PrimaryGreen.copy(alpha = 0.42f),
                            PrimaryGreen.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = Offset(centerX, centerY),
                        radius = sizeF * 0.25f
                    ),
                    radius = (sizeF * 0.6f) / 2f,
                    center = Offset(centerX, centerY)
                )
            }
        }
    }
}
