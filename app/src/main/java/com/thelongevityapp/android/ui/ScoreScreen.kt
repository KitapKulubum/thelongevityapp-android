package com.thelongevityapp.android.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thelongevityapp.android.R
import com.thelongevityapp.android.api.BiologicalAgeChartPoint
import com.thelongevityapp.android.api.ImpactFactor
import com.thelongevityapp.android.api.RecentImpactFactorsResponse
import com.thelongevityapp.android.data.ApiRepository
import com.thelongevityapp.android.data.SessionRepository
import com.thelongevityapp.android.ui.theme.ContentGradientBottom
import com.thelongevityapp.android.ui.theme.DarkBgTop
import com.thelongevityapp.android.ui.theme.PrimaryGreen
import com.thelongevityapp.android.ui.theme.PrimaryGreenBiologicalAge
import com.thelongevityapp.android.ui.theme.WarningOrange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private fun colorForBiologicalAge(biological: Double, chronological: Double): Color = when {
    biological - chronological < -0.5 -> PrimaryGreenBiologicalAge
    kotlin.math.abs(biological - chronological) <= 0.5 -> Color.White.copy(alpha = 0.6f)
    else -> WarningOrange
}

@Composable
fun ScoreScreen(
    session: SessionRepository,
    apiRepo: ApiRepository,
    modifier: Modifier = Modifier
) {
    var selectedRange by remember { mutableStateOf("weekly") }
    var chartData by remember { mutableStateOf<List<BiologicalAgeChartPoint>>(emptyList()) }
    var chartDelta by remember { mutableStateOf<Double?>(null) }
    var chartLoading by remember { mutableStateOf(false) }
    var chartError by remember { mutableStateOf<String?>(null) }
    var impactData by remember { mutableStateOf<RecentImpactFactorsResponse?>(null) }

    val summary = session.appState.summary
    val state = summary?.state
    val bioAge = state?.currentBiologicalAgeYears ?: state?.baselineBiologicalAgeYears ?: state?.chronologicalAgeYears ?: 0.0
    val chronoAge = state?.chronologicalAgeYears ?: 0.0
    val diff = bioAge - chronoAge
    val ageColor = colorForBiologicalAge(bioAge, chronoAge)

    fun loadChart(range: String) {
        chartLoading = true
        chartError = null
        kotlinx.coroutines.CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                kotlin.runCatching {
                    apiRepo.getBiologicalAgeChart(range)
                }
            }
            chartLoading = false
            result.fold(
                onSuccess = { resp ->
                    val list = resp.series?.sortedBy { it.date }.orEmpty()
                    chartData = if (range == "yearly") aggregateYearlyToMonthly(list) else list
                    chartDelta = resp.rangeDeltaYears
                    chartError = null
                },
                onFailure = { e ->
                    chartData = emptyList()
                    chartDelta = null
                    chartError = e.message ?: "Something went wrong"
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            kotlin.runCatching { apiRepo.getSummary() }.onSuccess { session.persistSummary(it) }
            kotlin.runCatching { apiRepo.getRecentImpactFactors() }.onSuccess { impactData = it }
        }
    }
    LaunchedEffect(selectedRange) {
        loadChart(selectedRange)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(DarkBgTop, ContentGradientBottom)))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp, bottom = 100.dp)
    ) {
        // Header
        Text(
            stringResource(R.string.age_header),
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Hero – biological age
        Text(
            "%.1f".format(bioAge),
            color = ageColor,
            fontSize = 72.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            stringResource(R.string.age_biological_label),
            color = ageColor.copy(alpha = 0.65f),
            fontSize = 9.sp,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
        val situationText = when {
            diff < -0.5 -> stringResource(R.string.age_bio_below, -diff)
            diff > 0.5 -> stringResource(R.string.age_bio_above, diff)
            else -> stringResource(R.string.age_bio_aligned)
        }
        Text(
            situationText,
            color = Color.White.copy(alpha = if (diff > 0.5) 0.5f else 0.6f),
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "%.1f".format(chronoAge),
            color = Color.White.copy(alpha = 0.25f),
            fontSize = 24.sp
        )
        Text(
            stringResource(R.string.age_chronological_label),
            color = Color.White.copy(alpha = 0.25f),
            fontSize = 8.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Range selector – capsule: white 0.05 bg, white 0.1 stroke, 6 dp inner padding
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(percent = 50))
                .background(Color.White.copy(alpha = 0.05f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(percent = 50))
                .padding(6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(
                stringResource(R.string.age_range_weekly) to "weekly",
                stringResource(R.string.age_range_monthly) to "monthly",
                stringResource(R.string.age_range_yearly) to "yearly"
            ).forEach { (label, value) ->
                val selected = selectedRange == value
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(
                            if (selected) PrimaryGreen.copy(alpha = 0.9f) else Color.Transparent
                        )
                        .clickable { selectedRange = value }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        color = if (selected) Color.Black else Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        // Chart section
        Text(
            stringResource(R.string.age_chart_title),
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
        val deltaText = when {
            chartDelta == null -> null
            chartDelta!! > 0 -> stringResource(R.string.age_chart_delta_up, chartDelta!!)
            chartDelta!! < 0 -> stringResource(R.string.age_chart_delta_down, chartDelta!!)
            else -> stringResource(R.string.age_chart_no_change)
        }
        if (deltaText != null) {
            Text(
                deltaText,
                color = when {
                    (chartDelta ?: 0.0) < 0 -> PrimaryGreen
                    (chartDelta ?: 0.0) > 0 -> WarningOrange
                    else -> Color.White.copy(alpha = 0.6f)
                },
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        BiologicalAgeChart(
            series = chartData,
            rangeDeltaYears = chartDelta,
            isLoading = chartLoading,
            error = chartError,
            modifier = Modifier.fillMaxWidth(),
            range = selectedRange
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Recent Impact Factors
        Text(
            stringResource(R.string.age_impact_title),
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
        impactData?.let { data ->
            if (data.date != null || data.deltaYears != null) {
                val deltaStr = data.deltaYears?.let { d -> " Δ${if (d >= 0) "+" else ""}%.2f".format(d) } ?: ""
                Text(
                    "• ${data.date ?: ""}$deltaStr",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Text(
                stringResource(R.string.age_impact_subtitle),
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
            data.message?.let { msg ->
                Text(
                    msg,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            val factors = data.factors?.take(4).orEmpty()
            if (factors.isEmpty()) {
                Text(
                    stringResource(R.string.age_impact_no_checkin),
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 12.dp)
                )
            } else {
                factors.forEach { factor ->
                    Spacer(modifier = Modifier.height(8.dp))
                    ImpactFactorCardView(factor = factor)
                }
            }
        } ?: run {
            Text(
                stringResource(R.string.age_impact_no_checkin),
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun ImpactFactorCardView(factor: ImpactFactor) {
    val isPositive = factor.sign == "positive"
    val accentColor = if (isPositive) WarningOrange else PrimaryGreen
    val signText = if (isPositive) stringResource(R.string.age_impact_ages_you) else stringResource(R.string.age_impact_helps_offset)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                factor.label ?: "",
                color = Color.White.copy(alpha = 0.98f),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                signText,
                color = accentColor,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
            factor.score?.let { _ ->
                // Optional: map score to option text from QuestionBanks if needed
            }
        }
    }
}
