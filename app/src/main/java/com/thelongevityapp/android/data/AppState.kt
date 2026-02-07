package com.thelongevityapp.android.data

import com.thelongevityapp.android.api.AuthProfileResponse
import com.thelongevityapp.android.api.StatsSummaryResponse

data class AppState(
    var userId: String = "",
    var hasCompletedOnboarding: Boolean = false,
    var summary: StatsSummaryResponse? = null,
    var userFirstName: String? = null,
    var userLastName: String? = null,
    var userChronologicalAge: Double? = null,
    var userTimezone: String? = null,
    var userPreferredLanguage: String? = null,
    var initialChronologicalAgeYears: Double? = null,
    var initialBiologicalAgeYears: Double? = null,
    var hasSeenWhyThisAppScreen: Boolean = false,
    var hasSeenInitialAgeInsightScreen: Boolean = false,
    var hasSeenChoosePlanScreen: Boolean = false,
    var hasSeenAIWelcomeAfterOnboarding: Boolean = false,
    var isSubscriptionActive: Boolean = false
) {
    val isTodaySubmitted: Boolean
        get() = summary?.today != null

    fun updateFromProfile(profile: AuthProfileResponse.ProfileInfo?) {
        if (profile == null) return
        userFirstName = profile.firstName ?: userFirstName
        userLastName = profile.lastName ?: userLastName
        profile.chronologicalAgeYears?.let { userChronologicalAge = it }
        profile.timezone?.let { userTimezone = it }
        profile.preferredLanguage?.let { userPreferredLanguage = it }
    }

    fun setInitialAges(chronological: Double, biological: Double) {
        initialChronologicalAgeYears = chronological
        initialBiologicalAgeYears = biological
    }
}
