package com.thelongevityapp.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.thelongevityapp.android.auth.ApiException
import com.thelongevityapp.android.auth.AuthManager
import retrofit2.HttpException
import com.thelongevityapp.android.data.ApiRepository
import com.thelongevityapp.android.data.SessionRepository
import com.thelongevityapp.android.ui.components.CheckCircle
import com.thelongevityapp.android.ui.components.GlassTextField
import com.thelongevityapp.android.ui.components.PrimaryPillButton
import com.thelongevityapp.android.ui.theme.DarkBgBottom
import com.thelongevityapp.android.ui.theme.DarkBgTop
import com.thelongevityapp.android.ui.theme.PrimaryGreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// §5.2 AuthLandingView — gradient, logo block, form, primary button, footer. Signup: CheckCircle Terms.
@Composable
fun AuthScreen(
    session: SessionRepository,
    apiRepo: ApiRepository,
    onAuthSuccess: () -> Unit,
    initialError: String? = null
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var dateOfBirthStr by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var agreeTerms by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var error by remember(initialError) { mutableStateOf(initialError) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkBgTop, DarkBgBottom)))
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(PrimaryGreen, CircleShape)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text("The Longevity App", color = Color.White, fontSize = 28.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                if (isSignUp) "Create your longevity profile" else "Continue your longevity journey.",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        if (isSignUp) {
            GlassTextField(value = name, onValueChange = { name = it }, label = "First name", placeholder = "Your name")
            Spacer(modifier = Modifier.height(12.dp))
            GlassTextField(value = dateOfBirthStr, onValueChange = { dateOfBirthStr = it }, label = "Date of birth", placeholder = "YYYY-MM-DD")
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CheckCircle(checked = agreeTerms, onCheckedChange = { agreeTerms = it })
                Spacer(modifier = Modifier.size(10.dp))
                Text(
                    "I agree to the Privacy Policy & Terms of Service",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        GlassTextField(value = email, onValueChange = { email = it }, label = "Email", placeholder = "user@example.com")
        Spacer(modifier = Modifier.height(12.dp))
        GlassTextField(value = password, onValueChange = { password = it }, label = if (isSignUp) "Create password" else "Password", placeholder = "••••••••", isPassword = true)
        if (!isSignUp) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { }) {
                Text("Forgot password?", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            }
        }
        error?.let { Text(it, color = Color.Red, modifier = Modifier.padding(top = 8.dp), fontSize = 14.sp) }
        Spacer(modifier = Modifier.height(24.dp))
        PrimaryPillButton(
            text = if (loading) "…" else if (isSignUp) "Create account" else "Continue",
            onClick = {
                if (isSignUp && !agreeTerms) return@PrimaryPillButton
                loading = true
                error = null
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        if (isSignUp) AuthManager.signUp(email.trim(), password)
                        else AuthManager.signIn(email.trim(), password)
                        val token = AuthManager.getIdToken()
                        withContext(Dispatchers.Main) {
                            loading = false
                            onAuthSuccess()
                        }
                        session.setRetrofitProviders()
                        val profile = apiRepo.postAuthMe(
                            idToken = token,
                            firstName = if (isSignUp) name.ifBlank { null } else null,
                            dateOfBirth = if (isSignUp) dateOfBirthStr.trim().ifBlank { null } else null,
                            acceptedPrivacy = if (isSignUp) "1.0" else null,
                            acceptedTerms = if (isSignUp) "1.0" else null
                        )
                        withContext(Dispatchers.Main) {
                            session.appState.userId = profile.uid ?: AuthManager.uid ?: ""
                            session.appState.updateFromProfile(profile.profile)
                            session.appState.hasCompletedOnboarding = profile.hasCompletedOnboarding ?: false
                            session.persistOnboarding(session.appState.hasCompletedOnboarding)
                            profile.locale?.let { session.setLanguage(it) }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            loading = false
                            fun isBackendOrNetworkError(): Boolean = when (e) {
                                is ApiException.HttpError -> true
                                is HttpException -> true
                                is ApiException.NetworkError -> true
                                is java.io.IOException -> true
                                else -> false
                            }
                            if (isBackendOrNetworkError()) {
                                AuthManager.signOut()
                                error = when (e) {
                                    is ApiException.NetworkError -> "Connection Issue"
                                    is ApiException.HttpError -> if (e.statusCode >= 500 || e.statusCode == 401) "Connection Issue" else (e.body.ifBlank { null } ?: e.message ?: "Sign in failed")
                                    is HttpException -> {
                                        val code = e.response()?.code() ?: -1
                                        if (code >= 500 || code == 401) "Connection Issue" else (e.response()?.errorBody()?.string()?.takeIf { it.isNotBlank() } ?: e.message ?: "Sign in failed")
                                    }
                                    is java.io.IOException -> "Connection Issue"
                                    else -> e.message ?: "Sign in failed"
                                }
                            } else {
                                error = when {
                                    e.message?.contains("already in use", ignoreCase = true) == true -> "This email is already registered. Try signing in."
                                    e.message?.contains("invalid", ignoreCase = true) == true -> "Invalid email or password."
                                    else -> e.message ?: "Sign in failed"
                                }
                                if (e.message?.contains("already in use", ignoreCase = true) == true) isSignUp = false
                            }
                        }
                    }
                }
            },
            enabled = !loading && email.isNotBlank() && password.isNotBlank() && (!isSignUp || agreeTerms)
        )
        Spacer(modifier = Modifier.height(10.dp))
        TextButton(onClick = { isSignUp = !isSignUp }) {
            Row {
                Text(if (isSignUp) "Already have an account? " else "Don't have an account? ", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp)
                Text(if (isSignUp) "Sign In" else "Sign Up", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
