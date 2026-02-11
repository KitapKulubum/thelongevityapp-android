package com.thelongevityapp.android.auth

import retrofit2.HttpException

/**
 * §2: Central place for user-facing API/auth error messages.
 * Prefer backend body message/error when present; otherwise status + context.
 */
object ErrorMessageHelper {

    enum class Context { Login, Signup, Bootstrap, DailyCheckIn, Onboarding, General, DeleteAccount, Subscription }

    /** Extract message from HTTP error body (message or error field). */
    fun messageFromBody(body: String): String? {
        if (body.isBlank()) return null
        return try {
            val json = org.json.JSONObject(body)
            json.optString("message").takeIf { it.isNotBlank() }
                ?: json.optString("error").takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }

    /** Returns true if body indicates subscription_required. */
    fun isSubscriptionRequired(statusCode: Int, body: String): Boolean =
        statusCode == 403 && (
            body.contains("subscription_required", ignoreCase = true) ||
            body.contains("subscription required", ignoreCase = true) ||
            body.contains("\"code\":\"subscription_required\"", ignoreCase = true)
        )

    /** Returns true if body indicates email verification required (e.g. delete account). */
    fun isEmailVerificationRequired(statusCode: Int, body: String): Boolean =
        statusCode == 403 && (
            body.contains("email_verification_required", ignoreCase = true) ||
            body.contains("email verification", ignoreCase = true)
        )

    fun getMessage(e: Throwable, context: Context): String {
        val (statusCode, body) = when (e) {
            is ApiException.HttpError -> e.statusCode to e.body
            is HttpException -> (e.response()?.code() ?: -1) to (e.response()?.errorBody()?.string() ?: "")
            else -> -1 to ""
        }
        val bodyMessage = messageFromBody(body)
        if (!bodyMessage.isNullOrBlank()) return bodyMessage

        return when (e) {
            is ApiException.MissingAuthToken -> "Your session has expired. Please sign in again."
            is ApiException.HttpError, is HttpException -> getMessageForStatus(statusCode, body, context)
            is ApiException.NetworkError -> "Connection Issue"
            is java.io.IOException -> "Connection Issue"
            else -> e.message ?: "Something went wrong"
        }
    }

    private fun getMessageForStatus(statusCode: Int, body: String, context: Context): String {
        when (statusCode) {
            400 -> return "Invalid request. Please check your input and try again."
            401 -> return when (context) {
                Context.Login -> "Invalid email or password. Please check your credentials and try again."
                else -> "Your session has expired. Please sign in again."
            }
            403 -> return when {
                isSubscriptionRequired(statusCode, body) -> "An active subscription is required to access this feature."
                context == Context.DeleteAccount && isEmailVerificationRequired(statusCode, body) ->
                    "Email verification is required to delete your account."
                else -> "You don't have permission to perform this action."
            }
            404 -> return "The requested resource was not found."
            409 -> return when (context) {
                Context.DailyCheckIn -> "You've already completed today's check-in. Come back tomorrow!"
                else -> "This action conflicts with your current data. Please refresh and try again."
            }
            422 -> return "Invalid data provided. Please check your input and try again."
            429 -> return "Too many requests. Please wait a moment and try again."
        }
        if (statusCode in 500..599) return "Our servers are experiencing issues. Please try again in a few moments."
        return "Something went wrong."
    }
}
