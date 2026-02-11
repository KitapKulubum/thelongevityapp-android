package com.thelongevityapp.android.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume

object AuthManager {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    val uid: String?
        get() = currentUser?.uid

    val isEmailVerified: Boolean
        get() = currentUser?.isEmailVerified == true

    /** Reload current user from Firebase (e.g. to refresh isEmailVerified). */
    suspend fun reloadUser() {
        currentUser?.reload()?.let { task ->
            suspendCancellableCoroutine<Unit> { cont ->
                task.addOnCompleteListener {
                    cont.resume(Unit)
                }
            }
        }
    }

    suspend fun sendEmailVerification() {
        currentUser?.sendEmailVerification()?.let { task ->
            suspendCancellableCoroutine<Unit> { cont ->
                task.addOnCompleteListener { cont.resume(Unit) }
            }
        }
    }

    suspend fun signUp(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).await()
    }

    suspend fun signIn(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun sendPasswordResetEmail(email: String) {
        firebaseAuth.sendPasswordResetEmail(email).await()
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    /** §9.4: user-not-found / user-disabled → signOut + MissingAuthToken */
    suspend fun getIdToken(forceRefresh: Boolean = true): String {
        val user = currentUser ?: throw ApiException.MissingAuthToken
        return try {
            user.getIdToken(forceRefresh).await().token ?: throw ApiException.MissingAuthToken
        } catch (e: FirebaseAuthInvalidUserException) {
            signOut()
            throw ApiException.MissingAuthToken
        }
    }
}

sealed class ApiException(message: String) : Exception(message) {
    object MissingAuthToken : ApiException("Authorization token is missing. Please sign in again.")
    class HttpError(val statusCode: Int, val body: String) : ApiException("Server returned $statusCode")
    class NetworkError(cause: Throwable) : ApiException(cause.message ?: "Network error")
}
