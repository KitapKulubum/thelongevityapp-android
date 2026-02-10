package com.thelongevityapp.android.data

import android.content.Context
import com.thelongevityapp.android.api.RetrofitModule
import com.thelongevityapp.android.api.StatsSummaryResponse
import com.thelongevityapp.android.auth.AuthManager
import com.thelongevityapp.android.auth.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import com.google.gson.Gson
import retrofit2.HttpException
import java.util.Locale

class SessionRepository(private val context: Context) {

    private val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)
    private val languageKey get() = "language"
    private val dateOfBirthKey = "userDateOfBirth"
    private val onboardingKey get() = "hasCompletedOnboarding_${appState.userId}"
    private val summaryKey get() = "cachedSummary_${appState.userId}"
    private fun whyThisAppKey() = "hasSeenWhyThisAppScreen_${appState.userId}"
    private fun initialAgeInsightKey() = "hasSeenInitialAgeInsightScreen_${appState.userId}"
    private fun choosePlanKey() = "hasSeenChoosePlanScreen_${appState.userId}"
    private fun aiWelcomeKey() = "hasSeenAIWelcomeAfterOnboarding_${appState.userId}"

    val appState = AppState()

    fun getStoredLanguage(): String =
        prefs.getString(languageKey, null) ?: Locale.getDefault().language

    fun setLanguage(code: String) {
        prefs.edit().putString(languageKey, code).apply()
    }

    fun loadPersistedState() {
        appState.userId = AuthManager.uid ?: ""
        appState.hasCompletedOnboarding = prefs.getBoolean(onboardingKey, false)
        appState.hasSeenWhyThisAppScreen = prefs.getBoolean(whyThisAppKey(), false)
        appState.hasSeenInitialAgeInsightScreen = prefs.getBoolean(initialAgeInsightKey(), false)
        appState.hasSeenChoosePlanScreen = prefs.getBoolean(choosePlanKey(), false)
        appState.hasSeenAIWelcomeAfterOnboarding = prefs.getBoolean(aiWelcomeKey(), false)
        val json = prefs.getString(summaryKey, null)
        if (json != null) {
            try {
                appState.summary = parseSummary(json)
            } catch (_: Exception) { }
        }
    }

    fun persistSeenWhyThisApp(seen: Boolean) {
        appState.hasSeenWhyThisAppScreen = seen
        prefs.edit()
            .putBoolean(whyThisAppKey(), seen)
            .putBoolean("hasSeenWhyThisAppScreen_", seen)
            .apply()
    }

    fun persistSeenInitialAgeInsight(seen: Boolean) {
        appState.hasSeenInitialAgeInsightScreen = seen
        prefs.edit()
            .putBoolean(initialAgeInsightKey(), seen)
            .putBoolean("hasSeenInitialAgeInsightScreen_", seen)
            .apply()
    }

    fun persistSeenChoosePlan(seen: Boolean) {
        appState.hasSeenChoosePlanScreen = seen
        prefs.edit().putBoolean(choosePlanKey(), seen).apply()
    }

    fun persistSeenAIWelcome(seen: Boolean) {
        appState.hasSeenAIWelcomeAfterOnboarding = seen
        prefs.edit().putBoolean(aiWelcomeKey(), seen).apply()
    }

    fun persistOnboarding(completed: Boolean) {
        appState.hasCompletedOnboarding = completed
        prefs.edit().putBoolean(onboardingKey, completed).apply()
    }

    /** Persist DoB from signup (yyyy-MM-dd) for optional chronological age fallback. */
    fun persistDateOfBirth(dateOfBirth: String?) {
        if (dateOfBirth.isNullOrBlank()) return
        prefs.edit().putString(dateOfBirthKey, dateOfBirth.trim()).apply()
    }

    fun getStoredDateOfBirth(): String? = prefs.getString(dateOfBirthKey, null)?.takeIf { it.isNotBlank() }

    fun persistSummary(summary: StatsSummaryResponse) {
        appState.summary = summary
        appState.userId = summary.userId
        prefs.edit().putString(summaryKey, summaryToJson(summary)).apply()
    }

    suspend fun bootstrap(requireBackend: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        appState.userId = AuthManager.uid ?: ""
        loadPersistedState()

        setRetrofitProviders()

        val token = try {
            AuthManager.getIdToken()
        } catch (e: ApiException.MissingAuthToken) {
            return@withContext Result.success(Unit)
        } catch (e: Exception) {
            return@withContext Result.failure(e)
        }

        val summaryResult = withTimeoutOrNull(25_000L) {
            runCatching {
                try {
                    RetrofitModule.api.getSummary("Bearer $token", getStoredLanguage())
                } catch (e: HttpException) {
                    val resp = e.response()
                    val statusCode = resp?.code() ?: -1
                    val body = resp?.errorBody()?.string() ?: ""
                    throw ApiException.HttpError(statusCode, body)
                }
            }
        } ?: Result.failure(ApiException.NetworkError(Exception("Timeout")))

        summaryResult.fold(
            onSuccess = { summary ->
                appState.summary = summary
                appState.userId = summary.userId
                persistSummary(summary)
                appState.hasCompletedOnboarding = summary.onboardingStatus
                persistOnboarding(summary.onboardingStatus)
                checkSubscriptionStatus()
                Result.success(Unit)
            },
            onFailure = { e ->
                when (e) {
                    is ApiException.HttpError -> {
                        if (e.statusCode == 404 && (e.body.contains("onboarding", ignoreCase = true) || e.body.contains("user not found", ignoreCase = true))) {
                            appState.hasCompletedOnboarding = false
                            persistOnboarding(false)
                            Result.success(Unit)
                        } else if (e.statusCode == 403 && e.body.contains("subscription", ignoreCase = true)) {
                            Result.success(Unit)
                        } else Result.failure(e)
                    }
                    else -> if (requireBackend) Result.failure(e) else Result.success(Unit)
                }
            }
        )
    }

    /** §10.5: After bootstrap, check subscription — active/trial → skip paywall */
    private suspend fun checkSubscriptionStatus() {
        runCatching {
            val token = AuthManager.getIdToken()
            val resp = RetrofitModule.api.getSubscriptionStatus("Bearer $token", getStoredLanguage())
            val status = resp.subscription?.status?.lowercase()
            appState.isSubscriptionActive = status == "active" || status == "trial"
        }
    }

    fun setRetrofitProviders() {
        RetrofitModule.authTokenProvider = {
            runBlocking { kotlin.runCatching { AuthManager.getIdToken() }.getOrNull() }
        }
        RetrofitModule.languageProvider = { getStoredLanguage() }
    }

    private fun summaryToJson(s: StatsSummaryResponse): String = gson.toJson(s)

    private fun parseSummary(json: String): StatsSummaryResponse =
        gson.fromJson(json, StatsSummaryResponse::class.java)
}

private val gson = Gson()
