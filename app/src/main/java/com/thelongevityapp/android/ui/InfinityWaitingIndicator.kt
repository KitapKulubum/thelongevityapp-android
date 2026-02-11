package com.thelongevityapp.android.ui

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.thelongevityapp.android.ui.theme.PrimaryGreen

/** §4.10 Infinity (∞) waiting: two circles, stroke only, breathing scale + opacity. 32×16 dp frame. */
@Composable
fun InfinityWaitingIndicator(
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "infinity_breath")
    val breath by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = if (reduceMotion) infiniteRepeatable(
            animation = tween(0),
            repeatMode = RepeatMode.Restart
        ) else infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )
    val scale = if (reduceMotion) 1f else (0.94f + breath * (1.06f - 0.94f))
    val alpha = if (reduceMotion) 1f else (0.5f + breath * 0.5f)

    Canvas(
        modifier = modifier
            .size(32.dp, 16.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
    ) {
        // Coordinates in dp: left (12,8), right (20,8), radius 4, stroke 2, shadow radius 6
        val w = size.width
        val h = size.height
        val leftCx = w * (12f / 32f)
        val rightCx = w * (20f / 32f)
        val cy = h * (8f / 16f)
        val radius = w * (4f / 32f)
        val strokeWidth = w * (2f / 32f)
        val shadowRadius = w * (6f / 32f)

        // Shadow: primary green opacity 0.2
        drawCircle(
            color = PrimaryGreen.copy(alpha = 0.2f),
            radius = radius + shadowRadius,
            center = Offset(leftCx, cy)
        )
        drawCircle(
            color = PrimaryGreen.copy(alpha = 0.2f),
            radius = radius + shadowRadius,
            center = Offset(rightCx, cy)
        )
        // Stroke circles: 2 dp stroke, no fill
        drawCircle(
            color = PrimaryGreen,
            radius = radius,
            center = Offset(leftCx, cy),
            style = Stroke(width = strokeWidth)
        )
        drawCircle(
            color = PrimaryGreen,
            radius = radius,
            center = Offset(rightCx, cy),
            style = Stroke(width = strokeWidth)
        )
    }
}
