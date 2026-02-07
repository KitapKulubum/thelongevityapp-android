package com.thelongevityapp.android.data

import com.thelongevityapp.android.api.AuthMeRequest
import com.thelongevityapp.android.api.AuthProfileResponse
import com.thelongevityapp.android.api.ChatRequest
import com.thelongevityapp.android.api.ChatResponse
import com.thelongevityapp.android.api.DailyMetricsPayload
import com.thelongevityapp.android.api.DailyResultDTO
import com.thelongevityapp.android.api.DailySubmitRequest
import com.thelongevityapp.android.api.LongevityApi
import com.thelongevityapp.android.api.OnboardingResultResponse
import com.thelongevityapp.android.api.OnboardingSubmitRequest
import com.thelongevityapp.android.api.OnboardingResultDTO
import com.thelongevityapp.android.api.PasswordResetConfirmRequest
import com.thelongevityapp.android.api.PasswordResetRequestRequest
import com.thelongevityapp.android.api.PasswordResetVerifyRequest
import com.thelongevityapp.android.api.RecentImpactFactorsResponse
import com.thelongevityapp.android.api.BiologicalAgeChartResponse
import com.thelongevityapp.android.api.SubscriptionStatusResponse
import com.thelongevityapp.android.api.RetrofitModule
import com.thelongevityapp.android.api.StatsSummaryResponse
import com.thelongevityapp.android.auth.ApiException
import com.thelongevityapp.android.auth.AuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response

class ApiRepository(private val session: SessionRepository) {

    private fun language() = session.getStoredLanguage()

    suspend fun postLogout(token: String): Unit = withContext(Dispatchers.IO) {
        try {
            RetrofitModule.api.postLogout("Bearer $token", language())
        } catch (_: Exception) { }
    }

    suspend fun deleteAccount(): Unit = withContext(Dispatchers.IO) {
        val token = AuthManager.getIdToken()
        val r = RetrofitModule.api.deleteAccount("Bearer $token", language())
        if (!r.isSuccessful) throw ApiException.HttpError(r.code(), r.errorBody()?.string() ?: "")
    }

    suspend fun postAuthMe(
        idToken: String,
        firstName: String? = null,
        lastName: String? = null,
        dateOfBirth: String? = null,
        acceptedPrivacy: String? = null,
        acceptedTerms: String? = null
    ): AuthProfileResponse = withContext(Dispatchers.IO) {
        RetrofitModule.api.postAuthMe(
            "Bearer $idToken",
            language(),
            AuthMeRequest(idToken, firstName, lastName, dateOfBirth, acceptedPrivacy, acceptedTerms)
        )
    }

    suspend fun getSummary(): StatsSummaryResponse = withContext(Dispatchers.IO) {
        try {
            RetrofitModule.api.getSummary("Bearer ${com.thelongevityapp.android.auth.AuthManager.getIdToken()}", language())
        } catch (e: HttpException) {
            val resp = e.response()
            throw ApiException.HttpError(resp?.code() ?: -1, "")
        }
    }

    suspend fun postOnboardingSubmit(request: OnboardingSubmitRequest): OnboardingResultDTO = withContext(Dispatchers.IO) {
        try {
            RetrofitModule.api.postOnboardingSubmit(
                "Bearer ${com.thelongevityapp.android.auth.AuthManager.getIdToken()}",
                language(),
                request
            )
        } catch (e: HttpException) {
            val resp = e.response()
            throw ApiException.HttpError(resp?.code() ?: -1, "")
        }
    }

    suspend fun getOnboardingResult(): OnboardingResultResponse = withContext(Dispatchers.IO) {
        try {
            RetrofitModule.api.getOnboardingResult(
                "Bearer ${com.thelongevityapp.android.auth.AuthManager.getIdToken()}",
                language()
            )
        } catch (e: HttpException) {
            val resp = e.response()
            throw ApiException.HttpError(resp?.code() ?: -1, "")
        }
    }

    suspend fun postDailyUpdate(metrics: DailyMetricsPayload): DailyResultDTO = withContext(Dispatchers.IO) {
        try {
            RetrofitModule.api.postDailyUpdate(
                "Bearer ${com.thelongevityapp.android.auth.AuthManager.getIdToken()}",
                language(),
                DailySubmitRequest(metrics)
            )
        } catch (e: HttpException) {
            val resp = e.response()
            throw ApiException.HttpError(resp?.code() ?: -1, "")
        }
    }

    suspend fun postChat(message: String): ChatResponse = withContext(Dispatchers.IO) {
        try {
            RetrofitModule.api.postChat(
                "Bearer ${com.thelongevityapp.android.auth.AuthManager.getIdToken()}",
                language(),
                ChatRequest(message)
            )
        } catch (e: HttpException) {
            val resp = e.response()
            throw ApiException.HttpError(resp?.code() ?: -1, "")
        }
    }

    suspend fun getPrivacy(): com.thelongevityapp.android.api.PrivacyPolicyResponse = withContext(Dispatchers.IO) {
        RetrofitModule.api.getPrivacy(language())
    }

    suspend fun getTerms(): com.thelongevityapp.android.api.TermsOfServiceResponse = withContext(Dispatchers.IO) {
        RetrofitModule.api.getTerms(language())
    }

    suspend fun requestPasswordReset(email: String) = withContext(Dispatchers.IO) {
        val r: Response<Unit> = RetrofitModule.api.requestPasswordReset(language(), PasswordResetRequestRequest(email))
        if (!r.isSuccessful) throw ApiException.HttpError(r.code(), "")
    }

    suspend fun verifyPasswordReset(email: String, code: String): String = withContext(Dispatchers.IO) {
        val r: Response<com.thelongevityapp.android.api.PasswordResetVerifyResponse> = RetrofitModule.api.verifyPasswordReset(language(), PasswordResetVerifyRequest(email, code))
        if (!r.isSuccessful) throw ApiException.HttpError(r.code(), "")
        r.body()!!.resetToken
    }

    suspend fun confirmPasswordReset(resetToken: String, newPassword: String) = withContext(Dispatchers.IO) {
        val r: Response<Unit> = RetrofitModule.api.confirmPasswordReset(language(), PasswordResetConfirmRequest(resetToken, newPassword))
        if (!r.isSuccessful) throw ApiException.HttpError(r.code(), "")
    }

    suspend fun getBiologicalAgeChart(range: String): BiologicalAgeChartResponse = withContext(Dispatchers.IO) {
        try {
            RetrofitModule.api.getBiologicalAgeChart("Bearer ${AuthManager.getIdToken()}", language(), range)
        } catch (e: HttpException) {
            val resp = e.response()
            throw ApiException.HttpError(resp?.code() ?: -1, "")
        }
    }

    suspend fun getRecentImpactFactors(): RecentImpactFactorsResponse = withContext(Dispatchers.IO) {
        try {
            RetrofitModule.api.getRecentImpactFactors("Bearer ${AuthManager.getIdToken()}", language())
        } catch (e: HttpException) {
            val resp = e.response()
            throw ApiException.HttpError(resp?.code() ?: -1, "")
        }
    }

    suspend fun getSubscriptionStatus(): SubscriptionStatusResponse = withContext(Dispatchers.IO) {
        try {
            RetrofitModule.api.getSubscriptionStatus("Bearer ${AuthManager.getIdToken()}", language())
        } catch (e: HttpException) {
            val resp = e.response()
            throw ApiException.HttpError(resp?.code() ?: -1, "")
        }
    }
}
