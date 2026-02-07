package com.thelongevityapp.android.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thelongevityapp.android.api.DailyMetricsPayload
import com.thelongevityapp.android.data.ApiRepository
import com.thelongevityapp.android.data.OptionItem
import com.thelongevityapp.android.data.QuestionBanks
import com.thelongevityapp.android.data.SessionRepository
import com.thelongevityapp.android.ui.theme.CardStroke
import com.thelongevityapp.android.ui.theme.ContentGradientBottom
import com.thelongevityapp.android.ui.theme.DarkBgTop
import com.thelongevityapp.android.ui.theme.GlassFill
import com.thelongevityapp.android.ui.theme.PrimaryGreen
import com.thelongevityapp.android.ui.theme.TextSecondary
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

@Composable
fun DailyCheckInScreen(
    session: SessionRepository,
    apiRepo: ApiRepository,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val options = remember { QuestionBanks.dailyOptions() }
    var step by remember { mutableStateOf(0) }
    var answers by remember { mutableStateOf<Map<Int, Double>>(emptyMap()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkBgTop, ContentGradientBottom)))
            .padding(horizontal = 32.dp)
            .padding(top = 40.dp, bottom = 24.dp)
    ) {
        Text("Daily Check-In", color = Color.White, fontSize = 28.sp)
        LinearProgressIndicator(
            progress = (step + 1).toFloat() / dailyPrompts.size.coerceAtLeast(1),
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = PrimaryGreen
        )
        Spacer(modifier = Modifier.height(24.dp))
        if (step < dailyPrompts.size) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(dailyPrompts[step], color = TextSecondary, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))
                options.forEach { opt ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable {
                                answers = answers + (step to opt.value.value)
                                step++
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = GlassFill),
                        border = BorderStroke(1.dp, CardStroke)
                    ) {
                        Text(opt.title, modifier = Modifier.padding(16.dp), color = TextSecondary, fontSize = 16.sp)
                    }
                }
            }
        } else {
            var submitted by remember { mutableStateOf(false) }
            if (answers.size == dailyPrompts.size && !submitted) {
                androidx.compose.runtime.LaunchedEffect(answers.size) {
                    submitted = true
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
                        onDone()
                    } catch (e: Exception) {
                        error = e.message
                    } finally {
                        loading = false
                    }
                }
            }
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (loading) CircularProgressIndicator(color = PrimaryGreen)
                error?.let { Text(it, color = Color.Red) }
            }
        }
    }
}
