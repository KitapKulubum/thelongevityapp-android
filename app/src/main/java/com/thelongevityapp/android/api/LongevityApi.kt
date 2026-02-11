package com.thelongevityapp.android.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Query

interface LongevityApi {

    @POST("api/auth/me")
    suspend fun postAuthMe(
        @Header("Authorization") authorization: String,
        @Header("X-Language") language: String?,
        @Body body: AuthMeRequest
    ): AuthProfileResponse

    @POST("api/auth/logout")
    suspend fun postLogout(
        @Header("Authorization") authorization: String,
        @Header("X-Language") language: String?
    ): Response<Unit>

    @DELETE("api/auth/account")
    suspend fun deleteAccount(
        @Header("Authorization") authorization: String,
        @Header("X-Language") language: String?
    ): Response<Unit>

    @POST("api/auth/bypassverify")
    suspend fun postBypassVerify(
        @Header("Authorization") authorization: String,
        @Header("X-Language") language: String?
    ): Response<Unit>

    @PATCH("api/auth/profile")
    suspend fun patchProfile(
        @Header("Authorization") authorization: String,
        @Header("X-Language") language: String?,
        @Body body: ProfileUpdateRequest
    ): AuthProfileResponse

    @GET("api/stats/summary")
    suspend fun getSummary(
        @Header("Authorization") authorization: String,
        @Header("X-Language") language: String?
    ): StatsSummaryResponse

    @POST("api/onboarding/submit")
    suspend fun postOnboardingSubmit(
        @Header("Authorization") authorization: String,
        @Header("X-Language") language: String?,
        @Body body: OnboardingSubmitRequest
    ): OnboardingResultDTO

    @GET("api/onboarding/result")
    suspend fun getOnboardingResult(
        @Header("Authorization") authorization: String,
        @Header("X-Language") language: String?
    ): OnboardingResultResponse

    @POST("api/age/daily-update")
    suspend fun postDailyUpdate(
        @Header("Authorization") authorization: String,
        @Header("X-Language") language: String?,
        @Body body: DailySubmitRequest
    ): DailyResultDTO

    @POST("api/chat")
    suspend fun postChat(
        @Header("Authorization") authorization: String,
        @Header("X-Language") language: String?,
        @Body body: ChatRequest
    ): ChatResponse

    @GET("api/legal/privacy")
    suspend fun getPrivacy(
        @Header("X-Language") language: String?
    ): PrivacyPolicyResponse

    @GET("api/legal/terms")
    suspend fun getTerms(
        @Header("X-Language") language: String?
    ): TermsOfServiceResponse

    @POST("api/auth/password-reset/request")
    suspend fun requestPasswordReset(
        @Header("X-Language") language: String?,
        @Body body: PasswordResetRequestRequest
    ): Response<Unit>

    @POST("api/auth/password-reset/verify")
    suspend fun verifyPasswordReset(
        @Header("X-Language") language: String?,
        @Body body: PasswordResetVerifyRequest
    ): Response<PasswordResetVerifyResponse>

    @POST("api/auth/password-reset/confirm")
    suspend fun confirmPasswordReset(
        @Header("X-Language") language: String?,
        @Body body: PasswordResetConfirmRequest
    ): Response<Unit>

    @GET("api/analytics/biological-age-chart")
    suspend fun getBiologicalAgeChart(
        @Header("Authorization") authorization: String,
        @Header("X-Language") language: String?,
        @Query("range") range: String
    ): BiologicalAgeChartResponse

    @GET("api/age/recent-impact-factors")
    suspend fun getRecentImpactFactors(
        @Header("Authorization") authorization: String,
        @Header("X-Language") language: String?
    ): RecentImpactFactorsResponse

    @GET("api/subscription/status")
    suspend fun getSubscriptionStatus(
        @Header("Authorization") authorization: String,
        @Header("X-Language") language: String?
    ): SubscriptionStatusResponse

    @POST("api/subscription/verify")
    suspend fun postSubscriptionVerify(
        @Header("Authorization") authorization: String,
        @Header("X-Language") language: String?,
        @Body body: SubscriptionVerifyRequest
    ): Response<Unit>
}
