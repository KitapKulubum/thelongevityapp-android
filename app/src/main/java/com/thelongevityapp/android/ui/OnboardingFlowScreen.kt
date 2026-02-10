package com.thelongevityapp.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.thelongevityapp.android.R
import com.thelongevityapp.android.api.OnboardingAnswersPayload
import com.thelongevityapp.android.api.OnboardingResultDTO
import com.thelongevityapp.android.api.OnboardingSubmitRequest
import com.thelongevityapp.android.auth.ApiException
import com.thelongevityapp.android.data.ApiRepository
import com.thelongevityapp.android.data.QuestionBanks
import com.thelongevityapp.android.data.SessionRepository
import com.thelongevityapp.android.ui.components.ChatBubbleView
import com.thelongevityapp.android.ui.components.OnboardingProgressBar
import com.thelongevityapp.android.ui.components.OptionButton
import com.thelongevityapp.android.ui.theme.MeaningBackground
import com.thelongevityapp.android.ui.theme.PrimaryGreen
import kotlinx.coroutines.delay

private const val ONBOARDING_TOTAL_QUESTIONS = 10

@Composable
fun OnboardingFlowScreen(
    session: SessionRepository,
    apiRepo: ApiRepository,
    onComplete: (chronologicalYears: Double, biologicalYears: Double) -> Unit,
    onSignOut: () -> Unit
) {
    var contentReady by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(0) // yield to next frame so first composition is light
        contentReady = true
    }
    if (!contentReady) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MeaningBackground),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PrimaryGreen)
        }
        return
    }

    val context = LocalContext.current
    val lang = session.getStoredLanguage()
    val questions = remember(lang) { QuestionBanks.onboardingQuestions(lang) }

    var messages by remember { mutableStateOf<List<Pair<Boolean, String>>>(emptyList()) }
    var currentOnboardingQuestionIndex by remember { mutableStateOf(0) }
    var onboardingAnswers by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }
    var isSubmitting by remember { mutableStateOf(false) }
    var submitFailed by remember { mutableStateOf(false) }

    val introText = stringResource(R.string.onboarding_intro)
    val calculatingText = stringResource(R.string.onboarding_calculating)
    var nextStepTrigger by remember { mutableStateOf(0) }
    var submitTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        if (messages.isEmpty()) {
            messages = listOf(false to introText)
            currentOnboardingQuestionIndex = 0
            onboardingAnswers = emptyMap()
            nextStepTrigger = 1
        }
    }

    LaunchedEffect(nextStepTrigger) {
        if (nextStepTrigger == 0) return@LaunchedEffect
        delay(if (nextStepTrigger == 1) 100L else 300L)
        if (currentOnboardingQuestionIndex >= ONBOARDING_TOTAL_QUESTIONS) {
            messages = messages + (false to calculatingText)
            isSubmitting = true
            submitFailed = false
            val chrono = chronologicalAgeForOnboardingSubmit(session)
            val payload = OnboardingAnswersPayload(
                sleep = onboardingAnswers["sleep"] ?: 0.0,
                activity = onboardingAnswers["activity"] ?: 0.0,
                muscle = onboardingAnswers["muscle"] ?: 0.0,
                visceralFat = onboardingAnswers["visceralFat"] ?: 0.0,
                nutritionPattern = onboardingAnswers["nutritionPattern"] ?: 0.0,
                sugar = onboardingAnswers["sugar"] ?: 0.0,
                stress = onboardingAnswers["stress"] ?: 0.0,
                smokingAlcohol = onboardingAnswers["smokingAlcohol"] ?: 0.0,
                metabolicHealth = onboardingAnswers["metabolicHealth"] ?: 0.0,
                energyFocus = onboardingAnswers["energyFocus"] ?: 0.0
            )
            try {
                val result = apiRepo.postOnboardingSubmit(OnboardingSubmitRequest(chrono, payload))
                messages = messages.dropLast(1) + (false to formatWelcomeMessage(context, result))
                session.appState.setInitialAges(result.chronologicalAgeYears, result.baselineBiologicalAgeYears)
                session.persistOnboarding(true)
                onComplete(result.chronologicalAgeYears, result.baselineBiologicalAgeYears)
            } catch (e: ApiException.HttpError) {
                if (e.statusCode == 403 && e.body.contains("subscription_required", ignoreCase = true)) {
                    messages = messages.dropLast(1)
                    session.appState.setInitialAges(chrono, chrono)
                    session.persistOnboarding(true)
                    onComplete(chrono, chrono)
                } else {
                    val errMsg = e.body.ifBlank { null } ?: context.getString(R.string.onboarding_error_generic)
                    messages = messages.dropLast(1) + (false to errMsg)
                    submitFailed = true
                }
            } catch (_: Exception) {
                messages = messages.dropLast(1) + (false to context.getString(R.string.onboarding_error_generic))
                submitFailed = true
            } finally {
                isSubmitting = false
            }
        } else {
            val q = questions[currentOnboardingQuestionIndex]
            messages = messages + (false to q.prompt)
        }
    }

    LaunchedEffect(submitTrigger) {
        if (submitTrigger == 0) return@LaunchedEffect
        messages = messages + (false to calculatingText)
        isSubmitting = true
        submitFailed = false
        val chrono = chronologicalAgeForOnboardingSubmit(session)
        val payload = OnboardingAnswersPayload(
            sleep = onboardingAnswers["sleep"] ?: 0.0,
            activity = onboardingAnswers["activity"] ?: 0.0,
            muscle = onboardingAnswers["muscle"] ?: 0.0,
            visceralFat = onboardingAnswers["visceralFat"] ?: 0.0,
            nutritionPattern = onboardingAnswers["nutritionPattern"] ?: 0.0,
            sugar = onboardingAnswers["sugar"] ?: 0.0,
            stress = onboardingAnswers["stress"] ?: 0.0,
            smokingAlcohol = onboardingAnswers["smokingAlcohol"] ?: 0.0,
            metabolicHealth = onboardingAnswers["metabolicHealth"] ?: 0.0,
            energyFocus = onboardingAnswers["energyFocus"] ?: 0.0
        )
        try {
            val result = apiRepo.postOnboardingSubmit(OnboardingSubmitRequest(chrono, payload))
            messages = messages.dropLast(1) + (false to formatWelcomeMessage(context, result))
            session.appState.setInitialAges(result.chronologicalAgeYears, result.baselineBiologicalAgeYears)
            session.persistOnboarding(true)
            onComplete(result.chronologicalAgeYears, result.baselineBiologicalAgeYears)
        } catch (e: ApiException.HttpError) {
            if (e.statusCode == 403 && e.body.contains("subscription_required", ignoreCase = true)) {
                messages = messages.dropLast(1)
                session.appState.setInitialAges(chrono, chrono)
                session.persistOnboarding(true)
                onComplete(chrono, chrono)
            } else {
                val errMsg = e.body.ifBlank { null } ?: context.getString(R.string.onboarding_error_generic)
                messages = messages.dropLast(1) + (false to errMsg)
                submitFailed = true
            }
        } catch (_: Exception) {
            messages = messages.dropLast(1) + (false to context.getString(R.string.onboarding_error_generic))
            submitFailed = true
        } finally {
            isSubmitting = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MeaningBackground)
    ) {
        BreathingGreenBackground(reduceMotion = remember { isReduceMotionEnabled(context) })

        Column(modifier = Modifier.fillMaxSize()) {
            if (messages.isNotEmpty()) {
                OnboardingProgressBar(
                    progress = (onboardingAnswers.size / ONBOARDING_TOTAL_QUESTIONS.toFloat()).coerceIn(0f, 1f),
                    currentQuestion = (currentOnboardingQuestionIndex + 1).coerceIn(1, ONBOARDING_TOTAL_QUESTIONS),
                    totalQuestions = ONBOARDING_TOTAL_QUESTIONS,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp, 8.dp, 20.dp, 12.dp)
                )
            }

            val scrollState = rememberScrollState()
            LaunchedEffect(messages.size) {
                delay(50)
                scrollState.animateScrollTo(scrollState.maxValue)
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                if (messages.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryGreen)
                    }
                } else {
                    messages.forEach { (isUser, text) ->
                        ChatBubbleView(text = text, isUser = isUser, modifier = Modifier.padding(vertical = 6.dp))
                    }

                    val currentQuestion = questions.getOrNull(currentOnboardingQuestionIndex)
                    if (currentQuestion != null && !isSubmitting) {
                        Spacer(modifier = Modifier.height(12.dp))
                        currentQuestion.options.forEach { opt ->
                            OptionButton(
                                text = opt.title,
                                onClick = {
                                    onboardingAnswers = onboardingAnswers + (currentQuestion.id to opt.value.value)
                                    messages = messages + (true to opt.title)
                                    currentOnboardingQuestionIndex++
                                    nextStepTrigger++
                                },
                                selected = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            )
                        }
                    }

                    if (submitFailed) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(percent = 50))
                                    .background(PrimaryGreen.copy(alpha = 0.2f))
                                    .border(1.dp, PrimaryGreen.copy(alpha = 0.4f), RoundedCornerShape(percent = 50))
                                    .padding(horizontal = 24.dp, vertical = 12.dp)
                                    .clickable { submitTrigger++ }
                            ) {
                                Text(
                                    stringResource(R.string.onboarding_retry),
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }

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

/**
 * Chronological age for onboarding submit.
 * Priority: 1) auth/me profile (userChronologicalAge), 2) summary.state, 3) DoB fallback only when both missing, 4) 0.
 * DoB fallback: yyyy-MM-dd → days between today and DoB / 365.25 (fractional years). Used only when backend did not provide age.
 */
private fun chronologicalAgeForOnboardingSubmit(session: SessionRepository): Double {
    val fromUser = session.appState.userChronologicalAge
    if (fromUser != null && fromUser > 0) return fromUser
    val fromSummary = session.appState.summary?.state?.chronologicalAgeYears
    if (fromSummary != null && fromSummary > 0) return fromSummary
    // Fallback only when auth/me and summary did not provide age (e.g. old user, API change): compute from stored DoB
    val fromDob = session.getStoredDateOfBirth()?.let { dobStr ->
        try {
            val dob = java.time.LocalDate.parse(dobStr) // yyyy-MM-dd
            val today = java.time.LocalDate.now()
            java.time.temporal.ChronoUnit.DAYS.between(dob, today) / 365.25
        } catch (_: Exception) {
            null
        }
    }
    if (fromDob != null && fromDob > 0) {
        val age = fromDob.coerceIn(1.0, 120.0)
        android.util.Log.d("OnboardingFlow", "Using age from stored DoB: $age")
        return age
    }
    android.util.Log.w("OnboardingFlow", "No chronological age found, using 0")
    return 0.0
}

private fun formatWelcomeMessage(context: android.content.Context, result: OnboardingResultDTO): String {
    val delta = result.baselineBiologicalAgeYears - result.chronologicalAgeYears
    val olderYounger = if (delta >= 0) context.getString(R.string.onboarding_welcome_older) else context.getString(R.string.onboarding_welcome_younger)
    return context.getString(
        R.string.onboarding_welcome_body,
        result.baselineBiologicalAgeYears,
        result.chronologicalAgeYears,
        kotlin.math.abs(delta),
        olderYounger
    ).let { body ->
        context.getString(R.string.onboarding_welcome_title) + "\n\n" + body
    }
}
