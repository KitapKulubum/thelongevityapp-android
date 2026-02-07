package com.thelongevityapp.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thelongevityapp.android.ui.components.PrimaryPillButton
import com.thelongevityapp.android.ui.theme.ContentGradientBottom
import com.thelongevityapp.android.ui.theme.DarkBgTop
import com.thelongevityapp.android.ui.theme.PrimaryGreen

// §5.3 MeaningOnboardingView — 3 sayfa, BreathingGreenBackgroundView, başlık + alt başlık, Continue/Start
private val pages = listOf(
    Pair(
        "Your biological age, in your control.",
        "Small daily choices add up. We help you see the impact and stay on track."
    ),
    Pair(
        "Science-backed insights.",
        "Based on research in longevity and aging, we translate data into simple actions."
    ),
    Pair(
        "One app for your longevity journey.",
        "Track, learn, and improve. Start with a few questions—we'll personalize everything."
    )
)

@Composable
fun MeaningOnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var page by remember { mutableStateOf(0) }
    val (title, subtitle) = pages[page]

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkBgTop, ContentGradientBottom)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .padding(top = 60.dp, bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { i ->
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(if (i == page) 10.dp else 8.dp)
                            .background(
                                if (i == page) PrimaryGreen else Color.White.copy(alpha = 0.3f),
                                CircleShape
                            )
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 26.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            PrimaryPillButton(
                text = if (page < pages.size - 1) "Continue" else "Start",
                onClick = {
                    if (page < pages.size - 1) page++ else onComplete()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
