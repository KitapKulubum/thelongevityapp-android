package com.thelongevityapp.android.ui

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.thelongevityapp.android.auth.ApiException
import com.thelongevityapp.android.auth.AuthEvents
import com.thelongevityapp.android.auth.AuthManager
import com.thelongevityapp.android.auth.ErrorMessageHelper
import com.thelongevityapp.android.data.ApiRepository
import com.thelongevityapp.android.data.SessionRepository
import androidx.compose.ui.graphics.Color
import com.thelongevityapp.android.ui.theme.PrimaryGreen
@Composable
fun RootNav() {
    val context = LocalContext.current
    val app = context.applicationContext as Application
    val session = remember { SessionRepository(app) }
    val apiRepo = remember { ApiRepository(session) }

    var showSplash by remember { mutableStateOf(true) }
    var bootstrapDone by remember { mutableStateOf(false) }
    var authScreenError by remember { mutableStateOf<String?>(null) }
    var authTrigger by remember { mutableStateOf(0) }
    var navTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        AuthEvents.invalidated.collect { authTrigger++ }
    }

    key(authTrigger, navTrigger) {
    when {
        showSplash -> SplashScreen(onSplashFinished = { showSplash = false })
        AuthManager.currentUser == null -> AuthScreen(
            session = session,
            apiRepo = apiRepo,
            onAuthSuccess = { authScreenError = null; authTrigger++ },
            initialError = authScreenError
        )
        !bootstrapDone -> {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
            LaunchedEffect(Unit) {
                session.setRetrofitProviders()
                val result = session.bootstrap(requireBackend = true)
                result.fold(
                    onSuccess = { bootstrapDone = true },
                    onFailure = { e ->
                        AuthManager.signOut()
                        authScreenError = ErrorMessageHelper.getMessage(e, ErrorMessageHelper.Context.Bootstrap)
                        bootstrapDone = true
                        authTrigger++
                    }
                )
            }
        }
        // §1.3.5: Meaning only if both flags; then Onboarding → Initial Age → Choose Plan → MainTab
        !session.appState.hasSeenWhyThisAppScreen && !session.appState.hasCompletedOnboarding -> MeaningOnboardingScreen(
            onComplete = {
                session.persistSeenWhyThisApp(true)
                navTrigger++
            }
        )
        !session.appState.hasCompletedOnboarding -> OnboardingFlowScreen(
            session = session,
            apiRepo = apiRepo,
            onComplete = { chronologicalYears, biologicalYears ->
                session.persistOnboarding(true)
                session.appState.setInitialAges(chronologicalYears, biologicalYears)
                navTrigger++
            },
            onSignOut = { AuthManager.signOut(); authTrigger++ }
        )
        session.appState.isSubscriptionActive -> MainTabScreen(session = session, apiRepo = apiRepo, onSignOut = { AuthManager.signOut(); authTrigger++ }, onSubscriptionRequired = { navTrigger++ })
        !session.appState.hasSeenInitialAgeInsightScreen -> InitialAgeInsightScreen(
            session = session,
            apiRepo = apiRepo,
            onContinue = {
                session.persistSeenInitialAgeInsight(true)
                navTrigger++
            },
            onSignOut = { AuthManager.signOut(); authTrigger++ }
        )
        !session.appState.hasSeenChoosePlanScreen -> ChooseYourPlanScreen(
            session = session,
            onContinue = { navTrigger++ },
            onSignOut = { AuthManager.signOut(); authTrigger++ }
        )
        else -> MainTabScreen(session = session, apiRepo = apiRepo, onSignOut = { AuthManager.signOut(); authTrigger++ }, onSubscriptionRequired = { navTrigger++ })
    }
    }
}
