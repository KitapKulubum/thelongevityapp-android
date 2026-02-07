package com.thelongevityapp.android.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.thelongevityapp.android.R
import com.thelongevityapp.android.ui.theme.PrimaryGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// §1.3.1 Splash: black bg, radial glow, halo breathing, icon animations, callback
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val contentAlpha = remember { Animatable(1f) }
    val logoScale = remember { Animatable(0.92f) }
    val logoAlpha = remember { Animatable(0f) }
    val haloOpacity = remember { Animatable(0f) }
    val haloScale = remember { Animatable(0.95f) }

    LaunchedEffect(Unit) {
        launch {
            logoAlpha.animateTo(1f, animationSpec = tween(450, easing = EaseOut))
            logoScale.animateTo(1f, animationSpec = tween(450, easing = EaseOut))
        }
        delay(200) // halo starts at 200 ms
        haloOpacity.animateTo(0.35f, animationSpec = tween(520, easing = LinearEasing))
        haloScale.animateTo(1.08f, animationSpec = tween(520, easing = LinearEasing))
        haloOpacity.animateTo(0f, animationSpec = tween(265, easing = LinearEasing))
        haloScale.animateTo(1.05f, animationSpec = tween(265, easing = LinearEasing))
        delay(235) // buffer to ~1100 ms total
        contentAlpha.animateTo(0f, animationSpec = tween(200, easing = LinearEasing))
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current
        Box(
            modifier = Modifier
                .alpha(contentAlpha.value)
                .size(120.dp)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                PrimaryGreen.copy(alpha = 0.12f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = size.minDimension / 2f
                        )
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Halo: thin ring 1.5 dp stroke, primary green, breathing
            Box(
                modifier = Modifier
                    .size((72 * haloScale.value).dp)
                    .drawBehind {
                        drawCircle(
                            color = PrimaryGreen.copy(alpha = haloOpacity.value),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = with(density) { 1.5.dp.toPx() }
                            )
                        )
                    }
            )
            // App icon 64×64 dp, circular
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.mipmap.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
