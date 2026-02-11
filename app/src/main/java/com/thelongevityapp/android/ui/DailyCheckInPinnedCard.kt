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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thelongevityapp.android.R
import com.thelongevityapp.android.api.DailyMetricsPayload
import com.thelongevityapp.android.data.ApiRepository
import com.thelongevityapp.android.data.QuestionBanks
import com.thelongevityapp.android.auth.ApiException
import com.thelongevityapp.android.data.SessionRepository
import com.thelongevityapp.android.ui.components.OptionButton
import com.thelongevityapp.android.ui.theme.PrimaryGreen
import com.thelongevityapp.android.ui.theme.TextSecondary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Id -> backend metric field order for DailyMetricsPayload
private val idToPayloadOrder = listOf(
    "sleep", "movement", "foodQuality", "sugar", "stress", "mentalLoad",
    "moodSocial", "physicalWellbeing", "recoveryStatus", "selfCare"
)

private fun buildDailyMetricsPayload(answers: Map<String, Double>): DailyMetricsPayload {
    val scaled = idToPayloadOrder.map { id -> DailyMetricsPayload.scaleTo04(answers[id] ?: 0.0) }
    return DailyMetricsPayload(
        sleepQuality = scaled[0],
        movementLevel = scaled[1],
        nutritionQuality = scaled[2],
        addedSugarControl = scaled[3],
        stressBalance = scaled[4],
        mentalLoad = scaled[5],
        moodSocial = scaled[6],
        physicalWellbeing = scaled[7],
        recoveryStatus = scaled[8],
        selfCare = scaled[9]
    )
}

/**
 * Daily Check-in card for AI tab. States: completed (today submitted), inactive (collapsed), active (expanded with questions).
 * Outer padding: 20h, 12 top, 8 bottom. Controlled by parent (expanded / onToggle) so welcome overlay can auto-open.
 */
@Composable
fun DailyCheckInPinnedCard(
    session: SessionRepository,
    apiRepo: ApiRepository,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    onDailyComplete: (deltaYears: Double?) -> Unit = {},
    onSubscriptionRequired: () -> Unit = {},
    subscriptionRequiredMessage: String = "An active subscription is required to continue. Please choose a plan."
) {
    val lang = session.getStoredLanguage()
    val questions = remember(lang) { QuestionBanks.dailyQuestions(lang) }
    val options = remember { QuestionBanks.dailyOptions() }

    var answers by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }
    var isSubmitting by remember { mutableStateOf(false) }
    var submitError by remember { mutableStateOf<String?>(null) }

    val isTodaySubmitted = session.appState.isTodaySubmitted
    val currentIndex = answers.size
    val dailyProgress = if (questions.isNotEmpty()) (currentIndex.toFloat() / questions.size).coerceIn(0f, 1f) else 0f

    // Completed state
    if (isTodaySubmitted) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .padding(top = 12.dp, bottom = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
            border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    stringResource(R.string.ai_daily_complete),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        return
    }

    // Inactive / Active (expandable) state
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 8.dp)
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.daily_checkin_title), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Text(stringResource(R.string.daily_checkin_subtitle), color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                }
                if (expanded) {
                    Text(
                        "${(dailyProgress * 100).toInt()}%",
                        color = PrimaryGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                androidx.compose.material3.Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = Color.White.copy(alpha = 0.6f)
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = dailyProgress,
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = PrimaryGreen,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (currentIndex < questions.size) {
                    val q = questions[currentIndex]
                    Text(
                        stringResource(DailyCheckInStrings.promptResId(q.id)),
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        options.forEach { opt ->
                            OptionButton(
                                text = stringResource(DailyCheckInStrings.optionResId(opt.value)),
                                onClick = {
                                    answers = answers + (q.id to opt.value.value)
                                    if (answers.size == questions.size) {
                                        isSubmitting = true
                                        submitError = null
                                        val payload = buildDailyMetricsPayload(answers)
                                        CoroutineScope(Dispatchers.Main).launch {
                                            try {
                                                val result = apiRepo.postDailyUpdate(payload)
                                                session.persistSummary(apiRepo.getSummary())
                                                onDailyComplete(result.today?.deltaYears)
                                            } catch (e: Exception) {
                                                if (e is ApiException.HttpError && e.statusCode == 403 && e.body.contains("subscription_required", ignoreCase = true)) {
                                                    submitError = subscriptionRequiredMessage
                                                    session.persistSeenChoosePlan(false)
                                                    onSubscriptionRequired()
                                                } else {
                                                    submitError = e.message ?: "Something went wrong"
                                                }
                                            } finally {
                                                isSubmitting = false
                                            }
                                        }
                                    }
                                },
                                selected = false,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                } else if (isSubmitting) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
                        CircularProgressIndicator(
                            color = PrimaryGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.size(12.dp))
                        Text(stringResource(R.string.daily_saving), color = TextSecondary, fontSize = 14.sp)
                    }
                }
                submitError?.let { err ->
                    Text(err, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}
