package com.thelongevityapp.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thelongevityapp.android.R
import com.thelongevityapp.android.api.OnboardingResultResponse
import com.thelongevityapp.android.data.ApiRepository
import com.thelongevityapp.android.data.SessionRepository
import com.thelongevityapp.android.ui.theme.DarkBgTop
import com.thelongevityapp.android.ui.theme.InitialAgeGradientBottom
import com.thelongevityapp.android.ui.theme.PrimaryGreen
import com.thelongevityapp.android.ui.theme.PrimaryGreenBiologicalAge
import com.thelongevityapp.android.ui.theme.WarningOrange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val SECTION_SPACING_DP = 36
private const val SPACING_AFTER_DISCLAIMER_DP = 24
private const val SPACING_AFTER_CTA_DP = 48

private fun colorForBiologicalAge(biological: Double, chronological: Double): Color {
    val diff = biological - chronological
    return when {
        diff < -0.5 -> PrimaryGreenBiologicalAge
        kotlin.math.abs(diff) <= 0.5 -> Color(0xFF999999) // white 0.6
        else -> WarningOrange
    }
}

private fun deltaExplanation(context: android.content.Context, delta: Double): String = when {
    kotlin.math.abs(delta) < 0.5 -> context.getString(R.string.initial_age_delta_close)
    delta > 0 -> context.getString(R.string.initial_age_delta_above, delta)
    else -> context.getString(R.string.initial_age_delta_younger, -delta)
}

@Composable
fun InitialAgeInsightScreen(
    session: SessionRepository,
    apiRepo: ApiRepository,
    onContinue: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isLoadingResult by remember { mutableStateOf(true) }
    var onboardingResult by remember { mutableStateOf<OnboardingResultResponse?>(null) }

    LaunchedEffect(Unit) {
        isLoadingResult = true
        try {
            val result = withContext(Dispatchers.IO) { apiRepo.getOnboardingResult() }
            onboardingResult = result
            session.appState.setInitialAges(result.chronologicalAgeYears, result.biologicalAgeYears)
        } catch (_: Exception) {
            // use fallbacks; onboardingResult stays null
        } finally {
            isLoadingResult = false
        }
    }

    val chronologicalYears: Double? = onboardingResult?.chronologicalAgeYears
        ?: session.appState.initialChronologicalAgeYears
        ?: session.appState.userChronologicalAge
        ?: session.appState.summary?.state?.chronologicalAgeYears

    val biologicalYears: Double? = onboardingResult?.biologicalAgeYears
        ?: session.appState.initialBiologicalAgeYears
        ?: session.appState.summary?.state?.currentBiologicalAgeYears
        ?: session.appState.summary?.state?.baselineBiologicalAgeYears
        ?: session.appState.summary?.state?.chronologicalAgeYears

    val deltaYears: Double = onboardingResult?.deltaYears
        ?: (if (chronologicalYears != null && biologicalYears != null) biologicalYears - chronologicalYears else 0.0)

    val chrono = chronologicalYears ?: 0.0
    val bio = biologicalYears ?: 0.0
    val hasData = chronologicalYears != null && biologicalYears != null

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkBgTop, InitialAgeGradientBottom)))
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
                .padding(top = 24.dp, bottom = SPACING_AFTER_CTA_DP.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                stringResource(R.string.initial_age_top_context),
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(SECTION_SPACING_DP.dp))

            if (isLoadingResult) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 48.dp)
                    )
                    Text(
                        stringResource(R.string.initial_age_loading),
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(48.dp))
                }
            } else {
            if (hasData) {
                val ageColor = colorForBiologicalAge(bio, chrono)
                Text(
                    "%.2f".format(bio),
                    color = ageColor,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    stringResource(R.string.initial_age_biological_label),
                    color = ageColor.copy(alpha = 0.5f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.height(SECTION_SPACING_DP.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.04f))
                        .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                stringResource(R.string.initial_age_biological_age),
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "%.1f".format(bio),
                                color = ageColor,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                stringResource(R.string.initial_age_chronological_age),
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "%.1f".format(chrono),
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        deltaExplanation(context, deltaYears),
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 13.sp,
                        lineHeight = 17.sp
                    )
                }
                Spacer(modifier = Modifier.height(SECTION_SPACING_DP.dp))
            } else {
                Text(
                    stringResource(R.string.initial_age_no_data),
                    color = Color.White.copy(alpha = 0.92f),
                    fontSize = 24.sp,
                    lineHeight = 32.sp
                )
                Spacer(modifier = Modifier.height(SECTION_SPACING_DP.dp))
            }

            Text(
                stringResource(R.string.initial_age_verdict),
                color = Color.White.copy(alpha = 0.88f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                stringResource(R.string.initial_age_dynamic),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 15.sp,
                lineHeight = 21.sp
            )
            Spacer(modifier = Modifier.height(SECTION_SPACING_DP.dp))
            Text(
                stringResource(R.string.initial_age_disclaimer),
                color = Color.White.copy(alpha = 0.35f),
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(SPACING_AFTER_DISCLAIMER_DP.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(percent = 50))
                    .background(PrimaryGreen)
                    .clickable {
                        session.persistSeenInitialAgeInsight(true)
                        onContinue()
                    }
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    stringResource(R.string.initial_age_cta),
                    color = Color.Black,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(SPACING_AFTER_CTA_DP.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.not_you_log_out),
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 13.sp,
                    modifier = Modifier.clickable { onSignOut() }
                )
            }
            }
        }
    }
}
