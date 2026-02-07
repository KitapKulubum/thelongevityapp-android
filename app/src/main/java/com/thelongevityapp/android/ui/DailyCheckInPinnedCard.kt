package com.thelongevityapp.android.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thelongevityapp.android.api.DailyMetricsPayload
import com.thelongevityapp.android.data.ApiRepository
import com.thelongevityapp.android.data.QuestionBanks
import com.thelongevityapp.android.data.SessionRepository
import com.thelongevityapp.android.ui.components.OptionButton
import com.thelongevityapp.android.ui.theme.PrimaryGreen
import com.thelongevityapp.android.ui.theme.TextSecondary
import com.thelongevityapp.android.ui.theme.WarningOrange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val dailyPrompts = listOf(
    "How was your sleep last night?",
    "How much movement did you get today?",
    "How would you rate today's food quality?",
    "How much added sugar did you consume today?",
    "How stressed did you feel today?",
    "How would you rate your mental workload today?",
    "How was your mood and social connection today?",
    "How would you rate your physical wellbeing today?",
    "How would you rate your recovery status?",
    "How was your self-care today?"
)

// §4.11 DailyCheckInPinnedCard — tamamlanmamış: başlık + "Takes ~30 seconds" + chevron, açılınca progress + soru + OptionButton. Tamamlanmış: checkmark + "Daily Check-in Complete"
@Composable
fun DailyCheckInPinnedCard(
    session: SessionRepository,
    apiRepo: ApiRepository,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var step by remember { mutableStateOf(0) }
    var answers by remember { mutableStateOf<Map<Int, Double>>(emptyMap()) }
    var loading by remember { mutableStateOf(false) }
    val options = remember { QuestionBanks.dailyOptions() }
    val isTodaySubmitted = session.appState.isTodaySubmitted

    if (isTodaySubmitted) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
            border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.size(12.dp))
                Text("Daily Check-in Complete", color = PrimaryGreen, fontSize = 16.sp)
            }
        }
        return
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Daily Check-in", color = Color.White, fontSize = 16.sp)
                    Text("Takes ~30 seconds", color = TextSecondary, fontSize = 13.sp)
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = Color.White.copy(alpha = 0.6f)
                )
            }
            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Complete daily check-in to track your score",
                    color = WarningOrange.copy(alpha = 0.9f),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = (step + 1).toFloat() / dailyPrompts.size.coerceAtLeast(1),
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = PrimaryGreen
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (step < dailyPrompts.size) {
                    Text(dailyPrompts[step], color = TextSecondary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        options.forEach { opt ->
                            OptionButton(
                                text = opt.title,
                                onClick = {
                                    answers = answers + (step to opt.value.value)
                                    step++
                                },
                                selected = false,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                } else {
                    if (loading) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(color = PrimaryGreen, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.size(12.dp))
                            Text("Saving…", color = TextSecondary, fontSize = 14.sp)
                        }
                    } else {
                        LaunchedEffect(step) {
                            if (step != dailyPrompts.size || answers.size != dailyPrompts.size) return@LaunchedEffect
                            loading = true
                            try {
                                val values = (0 until 10).map { answers[it] ?: 0.0 }
                                val metrics = DailyMetricsPayload(
                                    sleepQuality = DailyMetricsPayload.scaleTo04(values[0]),
                                    movementLevel = DailyMetricsPayload.scaleTo04(values[1]),
                                    nutritionQuality = DailyMetricsPayload.scaleTo04(values[2]),
                                    addedSugarControl = DailyMetricsPayload.scaleTo04(values[3]),
                                    stressBalance = DailyMetricsPayload.scaleTo04(values[4]),
                                    mentalLoad = DailyMetricsPayload.scaleTo04(values[5]),
                                    moodSocial = DailyMetricsPayload.scaleTo04(values[6]),
                                    physicalWellbeing = DailyMetricsPayload.scaleTo04(values[7]),
                                    recoveryStatus = DailyMetricsPayload.scaleTo04(values[8]),
                                    selfCare = DailyMetricsPayload.scaleTo04(values[9])
                                )
                                apiRepo.postDailyUpdate(metrics)
                                try {
                                    session.persistSummary(apiRepo.getSummary())
                                } catch (_: Exception) { }
                            } finally {
                                loading = false
                            }
                        }
                    }
                }
            }
        }
    }
}
