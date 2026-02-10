package com.thelongevityapp.android.ui

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Image
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thelongevityapp.android.R
import com.thelongevityapp.android.auth.ApiException
import com.thelongevityapp.android.auth.AuthManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.thelongevityapp.android.data.ApiRepository
import com.thelongevityapp.android.data.SessionRepository
import com.thelongevityapp.android.ui.components.CheckCircle
import com.thelongevityapp.android.ui.components.GlassDatePicker
import com.thelongevityapp.android.ui.components.GlassTextField
import com.thelongevityapp.android.ui.components.PrimaryPillButton
import com.thelongevityapp.android.ui.theme.DarkBgBottom
import com.thelongevityapp.android.ui.theme.DarkBgTop
import com.thelongevityapp.android.ui.theme.PrimaryGreen
import retrofit2.HttpException

private fun isValidEmail(email: String): Boolean =
    email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()

@Composable
fun AuthScreen(
    session: SessionRepository,
    apiRepo: ApiRepository,
    onAuthSuccess: () -> Unit,
    initialError: String? = null
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var dateOfBirthStr by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var showForgotPasswordFlow by remember { mutableStateOf(false) }
    var agreeTerms by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var error by remember(initialError) { mutableStateOf(initialError) }
    var emailHasBlurred by remember { mutableStateOf(false) }
    var emailValidationMessage by remember { mutableStateOf<String?>(null) }
    var showLegal by remember { mutableStateOf(false) }
    var legalTitle by remember { mutableStateOf("") }
    var legalContent by remember { mutableStateOf<String?>(null) }

    fun computeEmailValidationMessage(): String? {
        val trimmed = email.trim()
        if (trimmed.isEmpty()) return null
        return if (isValidEmail(trimmed)) null else context.getString(R.string.auth_email_invalid)
    }

    if (showForgotPasswordFlow) {
        ForgotPasswordScreen(
            apiRepo = apiRepo,
            onDismiss = { showForgotPasswordFlow = false },
            onComplete = { completedEmail ->
                email = completedEmail
                showForgotPasswordFlow = false
            }
        )
        return
    }

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
            val density = LocalDensity.current
            val haloRadiusPx = with(density) { 40.dp.toPx() }
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            Brush.radialGradient(
                                center = Offset(haloRadiusPx, haloRadiusPx),
                                radius = haloRadiusPx,
                                colors = listOf(
                                    PrimaryGreen.copy(alpha = 0.09f),
                                    PrimaryGreen.copy(alpha = 0f)
                                )
                            ),
                            CircleShape
                        )
                )
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                        .border(1.dp, PrimaryGreen.copy(alpha = 0.135f), CircleShape)
                )
                Image(
                    painter = painterResource(R.mipmap.ic_launcher_foreground),
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                "The Longevity App",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                if (isSignUp) "Create your longevity profile" else "Continue your longevity journey.",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp
            )
        }
        Spacer(modifier = Modifier.height(32.dp))

        if (isSignUp) {
            GlassTextField(
                value = name,
                onValueChange = { name = it },
                label = "First name",
                placeholder = "Your Name",
                helper = context.getString(R.string.auth_first_name_helper)
            )
            Spacer(modifier = Modifier.height(24.dp))
            GlassDatePicker(
                value = dateOfBirthStr,
                onValueChange = { dateOfBirthStr = it },
                label = "Date of birth",
                placeholder = "YYYY-MM-DD",
                helper = context.getString(R.string.auth_dob_helper)
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        GlassTextField(
            value = email,
            onValueChange = {
                email = it
                if (emailHasBlurred && isValidEmail(it.trim())) emailValidationMessage = null
            },
            label = "Email address",
            placeholder = "user@example.com",
            supportingText = emailValidationMessage
        )
        Spacer(modifier = Modifier.height(24.dp))
        GlassTextField(
            value = password,
            onValueChange = { password = it },
            label = if (isSignUp) "Create password" else "Password",
            placeholder = "••••••••",
            isPassword = true,
            helper = if (isSignUp) context.getString(R.string.auth_password_helper) else null
        )
        if (!isSignUp) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { error = null; showForgotPasswordFlow = true }) {
                Text("Forgot password?", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            }
        }

        if (isSignUp) {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CheckCircle(checked = agreeTerms, onCheckedChange = { agreeTerms = it })
                Spacer(modifier = Modifier.size(10.dp))
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(context.getString(R.string.auth_agree_prefix), fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f))
                    Text(
                        context.getString(R.string.auth_privacy_policy),
                        color = PrimaryGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable(
                            onClick = {
                                legalTitle = context.getString(R.string.auth_privacy_policy)
                                showLegal = true
                                legalContent = null
                            },
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        )
                    )
                    Text(context.getString(R.string.auth_and), fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f))
                    Text(
                        context.getString(R.string.auth_terms_of_service),
                        color = PrimaryGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textDecoration = TextDecoration.Underline,
                        modifier = Modifier.clickable(
                            onClick = {
                                legalTitle = context.getString(R.string.auth_terms_of_service)
                                showLegal = true
                                legalContent = null
                            },
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        error?.let { Text(it, color = Color.Red, modifier = Modifier.padding(top = 8.dp), fontSize = 14.sp) }
        Spacer(modifier = Modifier.height(28.dp))

        val signupEnabled = name.trim().isNotBlank() && email.trim().isNotBlank() && password.isNotBlank() && agreeTerms && !loading
        val loginEnabled = email.trim().isNotBlank() && password.isNotBlank() && !loading
        PrimaryPillButton(
            text = if (loading) "…" else if (isSignUp) "Create account" else "Continue",
            onClick = {
                if (isSignUp && !agreeTerms) return@PrimaryPillButton
                emailHasBlurred = true
                emailValidationMessage = computeEmailValidationMessage()
                if (emailValidationMessage != null) return@PrimaryPillButton
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
                            firstName = if (isSignUp) name.trim().ifBlank { null } else null,
                            dateOfBirth = if (isSignUp) dateOfBirthStr.trim().ifBlank { null } else null,
                            acceptedPrivacy = if (isSignUp) "1.0" else null,
                            acceptedTerms = if (isSignUp) "1.0" else null
                        )
                        withContext(Dispatchers.Main) {
                            session.appState.userId = profile.uid ?: AuthManager.uid ?: ""
                            session.appState.updateFromProfile(profile.profile)
                            session.appState.hasCompletedOnboarding = profile.hasCompletedOnboarding ?: false
                            session.persistOnboarding(session.appState.hasCompletedOnboarding)
                            if (isSignUp && dateOfBirthStr.trim().isNotBlank()) {
                                session.persistDateOfBirth(dateOfBirthStr.trim())
                            }
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
            enabled = if (isSignUp) signupEnabled else loginEnabled,
            loading = loading,
            showTrailingIcon = true
        )
        Spacer(modifier = Modifier.height(10.dp))
        TextButton(onClick = { isSignUp = !isSignUp }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (isSignUp) context.getString(R.string.auth_already_have_account) else context.getString(R.string.auth_dont_have_account),
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )
                Text(
                    if (isSignUp) context.getString(R.string.auth_log_in) else context.getString(R.string.auth_sign_up),
                    color = PrimaryGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }

    LaunchedEffect(showLegal, legalTitle) {
        if (!showLegal || legalTitle.isEmpty()) return@LaunchedEffect
        legalContent = kotlin.runCatching {
            withContext(Dispatchers.IO) {
                if (legalTitle == context.getString(R.string.auth_privacy_policy))
                    apiRepo.getPrivacy().content
                else
                    apiRepo.getTerms().content
            }
        }.getOrNull() ?: ""
    }

    if (showLegal) {
        AlertDialog(
            onDismissRequest = { showLegal = false; legalTitle = ""; legalContent = null },
            title = { Text(legalTitle, color = Color.White) },
            text = {
                if (legalContent != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(legalContent!!, color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                    }
                } else {
                    Text("Loading…", color = Color.White.copy(alpha = 0.6f))
                }
            },
            confirmButton = {
                TextButton(onClick = { showLegal = false; legalTitle = ""; legalContent = null }) {
                    Text("Close", color = PrimaryGreen)
                }
            },
            containerColor = Color(0xFF1A1A1A)
        )
    }
}
