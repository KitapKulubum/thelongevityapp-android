package com.thelongevityapp.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.thelongevityapp.android.data.ApiRepository
import com.thelongevityapp.android.data.SessionRepository
import com.thelongevityapp.android.ui.components.PrimaryPillButton
import com.thelongevityapp.android.ui.theme.ContentGradientBottom
import com.thelongevityapp.android.ui.theme.DarkBgTop
import com.thelongevityapp.android.ui.theme.PrimaryGreen
import com.thelongevityapp.android.ui.theme.PrimaryGreenBiologicalAge
import com.thelongevityapp.android.ui.theme.TextSecondary
import com.thelongevityapp.android.ui.theme.WarningOrange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private fun colorForBiologicalAge(biological: Double, chronological: Double): Color {
    val diff = biological - chronological
    return when {
        diff < -0.5 -> PrimaryGreenBiologicalAge
        kotlin.math.abs(diff) <= 0.5 -> Color(0xFF999999)
        else -> WarningOrange
    }
}

private fun deltaDescription(delta: Double): String = when {
    kotlin.math.abs(delta) < 0.5 -> "Very close to your chronological age."
    delta > 0 -> "Currently about %.1f years above your chronological age.".format(delta)
    else -> "Currently about %.1f years younger than your chronological age.".format(-delta)
}

// §10.6 Initial Biological Age screen — color rules, comparison card, layout
@Composable
fun InitialAgeInsightScreen(
    session: SessionRepository,
    apiRepo: ApiRepository,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var chronological by remember { mutableStateOf(0.0) }
    var biological by remember { mutableStateOf(0.0) }
    var deltaYears by remember { mutableStateOf(0.0) }

    LaunchedEffect(Unit) {
        loading = true
        error = null
        try {
            val result = withContext(Dispatchers.IO) { apiRepo.getOnboardingResult() }
            chronological = result.chronologicalAgeYears
            biological = result.biologicalAgeYears
            deltaYears = result.deltaYears
            session.appState.setInitialAges(chronological, biological)
        } catch (e: Exception) {
            error = e.message
        } finally {
            loading = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkBgTop, ContentGradientBottom)))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp)
            .padding(top = 40.dp, bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Your biological age today",
            color = Color.White.copy(alpha = 0.45f),
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.height(36.dp))
        if (loading) {
            CircularProgressIndicator(color = Color.White.copy(alpha = 0.5f))
            Text("Loading…", color = Color.White.copy(alpha = 0.4f), fontSize = 14.sp, modifier = Modifier.padding(vertical = 48.dp))
        } else error?.let {
            Text(it, color = Color.Red, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(24.dp))
            PrimaryPillButton(
                text = "See how to improve this",
                onClick = {
                    session.persistSeenInitialAgeInsight(true)
                    onContinue()
                },
                modifier = Modifier.fillMaxWidth()
            )
        } ?: run {
            val ageColor = colorForBiologicalAge(biological, chronological)
            Text(
                "%.2f".format(biological),
                color = ageColor,
                fontSize = 72.sp
            )
            Text(
                "BIOLOGICAL",
                color = ageColor.copy(alpha = 0.5f),
                fontSize = 8.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            // Comparison card §10.6
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(14.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Biological age", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                        Text("%.1f".format(biological), color = ageColor, fontSize = 18.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Chronological age", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                        Text("%.1f".format(chronological), color = Color.White.copy(alpha = 0.7f), fontSize = 18.sp)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    deltaDescription(deltaYears),
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "This is a starting point — not a verdict.",
                color = Color.White.copy(alpha = 0.88f),
                fontSize = 16.sp,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                "Your biological age is dynamic. With small daily choices, this number can move in a better direction over time.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 15.sp,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Based on your onboarding answers. This is not medical advice.",
                color = Color.White.copy(alpha = 0.35f),
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            PrimaryPillButton(
                text = "See how to improve this",
                onClick = {
                    session.persistSeenInitialAgeInsight(true)
                    onContinue()
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
