package com.thelongevityapp.android.api

import com.google.gson.annotations.SerializedName

// --- Auth ---
data class AuthMeRequest(
    val idToken: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val dateOfBirth: String? = null, // yyyy-MM-dd; sent on signup, backend returns profile.chronologicalAgeYears
    val acceptedPrivacyPolicyVersion: String? = null,
    val acceptedTermsVersion: String? = null
)

data class AuthProfileResponse(
    val uid: String? = null,
    val email: String? = null,
    val emailVerified: Boolean? = null,
    val hasCompletedOnboarding: Boolean? = null,
    val profile: ProfileInfo? = null,
    val locale: String? = null
) {
    data class ProfileInfo(
        val firstName: String? = null,
        val lastName: String? = null,
        val chronologicalAgeYears: Double? = null,
        val timezone: String? = null,
        val preferredLanguage: String? = null
    )
}

data class ProfileUpdateRequest(
    val timezone: String? = null,
    val preferredLanguage: String? = null
)

// --- Onboarding ---
data class OnboardingAnswersPayload(
    val sleep: Double,
    val activity: Double,
    val muscle: Double,
    val visceralFat: Double,
    val nutritionPattern: Double,
    val sugar: Double,
    val stress: Double,
    val smokingAlcohol: Double,
    val metabolicHealth: Double,
    val energyFocus: Double
)

data class OnboardingSubmitRequest(
    val chronologicalAgeYears: Double,
    val answers: OnboardingAnswersPayload
)

data class OnboardingResultDTO(
    val userId: String,
    val chronologicalAgeYears: Double,
    val baselineBiologicalAgeYears: Double,
    val currentBiologicalAgeYears: Double,
    val BAOYears: Double,
    val totalScore: Double
)

data class OnboardingResultResponse(
    val chronologicalAgeYears: Double,
    val biologicalAgeYears: Double,
    val deltaYears: Double,
    val totalScore: Double,
    val BAOYears: Double
)

// --- Daily check-in ---
data class DailyMetricsPayload(
    @SerializedName("sleep_quality") val sleepQuality: Double,
    @SerializedName("movement_level") val movementLevel: Double,
    @SerializedName("nutrition_quality") val nutritionQuality: Double,
    @SerializedName("added_sugar_control") val addedSugarControl: Double,
    @SerializedName("stress_balance") val stressBalance: Double,
    @SerializedName("mental_load") val mentalLoad: Double,
    @SerializedName("mood_social") val moodSocial: Double,
    @SerializedName("physical_wellbeing") val physicalWellbeing: Double,
    @SerializedName("recovery_status") val recoveryStatus: Double,
    @SerializedName("self_care") val selfCare: Double
) {
    companion object {
        fun scaleTo04(score: Double): Double = ((score + 1) * 2).coerceIn(0.0, 4.0)
    }
}

data class DailySubmitRequest(val metrics: DailyMetricsPayload)

data class DailyResultDTO(
    val state: BiologicalAgeState,
    val today: TodayEntry?
)

// --- Stats summary ---
data class BiologicalAgeState(
    val chronologicalAgeYears: Double,
    val baselineBiologicalAgeYears: Double? = null,
    val currentBiologicalAgeYears: Double? = null,
    val agingDebtYears: Double,
    val rejuvenationStreakDays: Int,
    val totalRejuvenationDays: Int
)

data class TodayEntry(
    val date: String,
    val score: Double,
    val deltaYears: Double,
    val reasons: List<String>
)

data class HistoryPoint(
    val date: String,
    val biologicalAgeYears: Double,
    val deltaYears: Double,
    val score: Double
)

data class StatsSummaryResponse(
    val userId: String,
    val state: BiologicalAgeState,
    val today: TodayEntry? = null,
    val weeklyHistory: List<HistoryPoint>,
    val monthlyHistory: List<HistoryPoint>,
    val yearlyHistory: List<HistoryPoint>,
    val hasCompletedOnboarding: Boolean? = null
) {
    val onboardingStatus: Boolean
        get() = hasCompletedOnboarding ?: (state.baselineBiologicalAgeYears != null)
}

// --- Chat ---
data class ChatRequest(val message: String)
data class ChatResponse(val answer: String)

// --- Legal ---
data class PrivacyPolicyResponse(
    val version: String,
    val lastUpdated: String,
    val content: String
)

data class TermsOfServiceResponse(
    val version: String,
    val lastUpdated: String,
    val content: String
)

// --- Password reset ---
data class PasswordResetRequestRequest(val email: String)
data class PasswordResetVerifyRequest(val email: String, val code: String)
data class PasswordResetVerifyResponse(val resetToken: String)
data class PasswordResetConfirmRequest(val resetToken: String, val newPassword: String)

// --- §6.6 Analytics / Age ---
data class BiologicalAgeChartResponse(
    val range: String? = null,
    val timezone: String? = null,
    val start: String? = null,
    val end: String? = null,
    val series: List<BiologicalAgeChartPoint>? = null,
    val rangeDeltaYears: Double? = null
)
data class BiologicalAgeChartPoint(
    val date: String,
    val biologicalAgeYears: Double
)

data class RecentImpactFactorsResponse(
    val date: String? = null,
    val deltaYears: Double? = null,
    val factors: List<ImpactFactor>? = null,
    val message: String? = null
)
data class ImpactFactor(
    val label: String? = null,
    val sign: String? = null,  // "positive" / "negative"
    val score: Double? = null
)

// --- §6.8 Subscription ---
data class SubscriptionStatusResponse(
    val subscription: SubscriptionInfo? = null
)
data class SubscriptionInfo(
    val status: String? = null,
    val plan: String? = null,
    val renewalDate: String? = null
)
