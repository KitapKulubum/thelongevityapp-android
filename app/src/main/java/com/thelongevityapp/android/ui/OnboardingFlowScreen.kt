package com.thelongevityapp.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thelongevityapp.android.api.OnboardingAnswersPayload
import com.thelongevityapp.android.api.OnboardingSubmitRequest
import com.thelongevityapp.android.data.ApiRepository
import com.thelongevityapp.android.data.QuestionBanks
import com.thelongevityapp.android.data.SessionRepository
import com.thelongevityapp.android.ui.components.OnboardingProgressBar
import com.thelongevityapp.android.ui.components.OptionButton as SpecOptionButton
import com.thelongevityapp.android.ui.theme.ContentGradientBottom
import com.thelongevityapp.android.ui.theme.DarkBgTop
import com.thelongevityapp.android.ui.theme.PrimaryGreen
import com.thelongevityapp.android.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun OnboardingFlowScreen(
    session: SessionRepository,
    apiRepo: ApiRepository,
    onComplete: (chronologicalYears: Double, biologicalYears: Double) -> Unit
) {
    val lang = session.getStoredLanguage()
    val questions = remember(lang) { QuestionBanks.onboardingQuestions(lang) }
    var step by remember { mutableStateOf(0) }
    var answers by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var submitted by remember { mutableStateOf(false) }

    val chronologicalAge = session.appState.userChronologicalAge ?: 30.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkBgTop, ContentGradientBottom)))
            .padding(horizontal = 32.dp)
            .padding(top = 40.dp, bottom = 24.dp)
    ) {
        OnboardingProgressBar(
            progress = (step + 1).toFloat() / questions.size.coerceAtLeast(1),
            currentQuestion = step + 1,
            totalQuestions = questions.size
        )
        Spacer(modifier = Modifier.height(24.dp))
        if (step < questions.size) {
            val q = questions[step]
            Text(q.prompt, color = Color.White, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(16.dp))
            q.options.forEach { opt ->
                SpecOptionButton(
                    text = opt.title,
                    onClick = { answers = answers + (q.id to opt.value.value); step++ },
                    selected = answers[q.id] == opt.value.value,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (loading) CircularProgressIndicator(color = PrimaryGreen)
                else error?.let { Text(it, color = Color.Red) }
            }
            if (answers.size == questions.size && !submitted) {
                LaunchedEffect(answers.size) {
                    submitted = true
                    loading = true
                    try {
                        val payload = OnboardingAnswersPayload(
                            sleep = answers["sleep"] ?: 0.0,
                            activity = answers["activity"] ?: 0.0,
                            muscle = answers["muscle"] ?: 0.0,
                            visceralFat = answers["visceralFat"] ?: 0.0,
                            nutritionPattern = answers["nutritionPattern"] ?: 0.0,
                            sugar = answers["sugar"] ?: 0.0,
                            stress = answers["stress"] ?: 0.0,
                            smokingAlcohol = answers["smokingAlcohol"] ?: 0.0,
                            metabolicHealth = answers["metabolicHealth"] ?: 0.0,
                            energyFocus = answers["energyFocus"] ?: 0.0
                        )
                        apiRepo.postOnboardingSubmit(
                            OnboardingSubmitRequest(chronologicalAge, payload)
                        )
                        val result = apiRepo.getOnboardingResult()
                        session.persistOnboarding(true)
                        onComplete(result.chronologicalAgeYears, result.biologicalAgeYears)
                    } catch (e: Exception) {
                        error = e.message
                    } finally {
                        loading = false
                    }
                }
            }
        }
    }
}
