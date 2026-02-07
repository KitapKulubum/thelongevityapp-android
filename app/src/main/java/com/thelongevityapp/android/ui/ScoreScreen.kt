package com.thelongevityapp.android.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thelongevityapp.android.api.HistoryPoint
import com.thelongevityapp.android.api.ImpactFactor
import com.thelongevityapp.android.api.RecentImpactFactorsResponse
import com.thelongevityapp.android.data.ApiRepository
import com.thelongevityapp.android.data.SessionRepository
import com.thelongevityapp.android.ui.components.PrimaryPillButton
import com.thelongevityapp.android.ui.theme.CardStroke
import com.thelongevityapp.android.ui.theme.ContentGradientBottom
import com.thelongevityapp.android.ui.theme.DarkBgTop
import com.thelongevityapp.android.ui.theme.GlassFill
import com.thelongevityapp.android.ui.theme.PrimaryGreen
import com.thelongevityapp.android.ui.theme.PrimaryGreenBiologicalAge
import com.thelongevityapp.android.ui.theme.TextSecondary
import com.thelongevityapp.android.ui.theme.WarningOrange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreScreen(
    session: SessionRepository,
    apiRepo: ApiRepository,
    modifier: Modifier = Modifier
) {
    var showDailyCheckIn by remember { mutableStateOf(false) }
    var selectedRange by remember { mutableStateOf("weekly") }
    var impactData by remember { mutableStateOf<RecentImpactFactorsResponse?>(null) }
    val summary = session.appState.summary
    val state = summary?.state
    val todayDelta = summary?.today?.deltaYears ?: 0.0
    val history: List<HistoryPoint> = when (selectedRange) {
        "monthly" -> summary?.monthlyHistory.orEmpty()
        "yearly" -> summary?.yearlyHistory.orEmpty()
        else -> summary?.weeklyHistory.orEmpty()
    }

    LaunchedEffect(Unit) {
        impactData = withContext(Dispatchers.IO) {
            kotlin.runCatching { apiRepo.getRecentImpactFactors() }.getOrNull()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkBgTop, ContentGradientBottom)))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 40.dp, bottom = 24.dp)
    ) {
        Text("Age", color = Color.White, fontSize = 28.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Biological Age Change",
            color = TextSecondary,
            fontSize = 14.sp
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Weekly" to "weekly", "Monthly" to "monthly", "Yearly" to "yearly").forEach { (label, value) ->
                val selected = selectedRange == value
                Text(
                    label,
                    color = if (selected) PrimaryGreen else Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    modifier = Modifier
                        .clickable { selectedRange = value }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .background(
                            if (selected) PrimaryGreen.copy(alpha = 0.2f) else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                )
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth().height(160.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = GlassFill),
            border = BorderStroke(1.dp, CardStroke)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (history.isEmpty()) {
                    Text("Complete daily check-in to see trend.", color = TextSecondary, fontSize = 14.sp)
                } else {
                    history.takeLast(7).forEach { point ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(point.date, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                            Text(
                                "%.1f yrs".format(point.biologicalAgeYears),
                                color = PrimaryGreenBiologicalAge,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Today's Δ", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
        Text(
            "%.2f years".format(todayDelta),
            color = when {
                todayDelta < 0 -> PrimaryGreen
                todayDelta > 0 -> WarningOrange
                else -> Color.White.copy(alpha = 0.6f)
            },
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = GlassFill),
            border = BorderStroke(1.dp, CardStroke)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                state?.let {
                    Text("Chronological: %.1f years".format(it.chronologicalAgeYears), color = TextSecondary, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Biological: %.1f years".format(it.currentBiologicalAgeYears ?: it.chronologicalAgeYears), color = PrimaryGreen, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Aging debt: %.2f years".format(it.agingDebtYears), color = TextSecondary, fontSize = 16.sp)
                    Text("%d day streak".format(it.rejuvenationStreakDays), color = TextSecondary, fontSize = 16.sp)
                } ?: Text("Complete daily check-in to see your score.", color = TextSecondary, fontSize = 16.sp)
            }
        }
        impactData?.message?.let { msg ->
            Spacer(modifier = Modifier.height(16.dp))
            Text("Recent impact", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
            Text(msg, color = TextSecondary, fontSize = 14.sp)
        }
        impactData?.factors?.let { factors ->
            Spacer(modifier = Modifier.height(8.dp))
            factors.forEach { factor ->
                ImpactFactorRow(factor = factor)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        PrimaryPillButton(
            text = if (session.appState.isTodaySubmitted) "Today's check-in done" else "Start Daily Check-In",
            onClick = { showDailyCheckIn = true },
            modifier = Modifier.fillMaxWidth()
        )
    }
    if (showDailyCheckIn) {
        ModalBottomSheet(
            onDismissRequest = { showDailyCheckIn = false },
            containerColor = ContentGradientBottom
        ) {
            DailyCheckInScreen(
                session = session,
                apiRepo = apiRepo,
                onDone = { showDailyCheckIn = false }
            )
        }
    }
}

@Composable
private fun ImpactFactorRow(factor: ImpactFactor) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                factor.label ?: "",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            factor.sign?.let { sign ->
                Text(
                    if (sign == "positive") "▲ Ages you" else "▼ Helps offset",
                    color = if (sign == "positive") WarningOrange else PrimaryGreen,
                    fontSize = 12.sp
                )
            }
        }
    }
}
