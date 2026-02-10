package com.thelongevityapp.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thelongevityapp.android.R
import com.thelongevityapp.android.auth.ApiException
import com.thelongevityapp.android.data.ApiRepository
import com.thelongevityapp.android.ui.components.GlassTextField
import com.thelongevityapp.android.ui.components.PrimaryPillButton
import com.thelongevityapp.android.ui.theme.DarkBgBottom
import com.thelongevityapp.android.ui.theme.DarkBgTop
import com.thelongevityapp.android.ui.theme.PrimaryGreen
import com.thelongevityapp.android.ui.theme.ValidationYellow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

private enum class ForgotStep { Email, VerifyCode, NewPassword, Success }

@Composable
fun ForgotPasswordScreen(
    apiRepo: ApiRepository,
    onDismiss: () -> Unit,
    onComplete: (String) -> Unit
) {
    val context = LocalContext.current
    var step by remember { mutableStateOf(ForgotStep.Email) }
    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var resetToken by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showToast by remember { mutableStateOf(false) }
    var resendCountdown by remember { mutableIntStateOf(0) }

    fun parseErrorBody(body: String): String {
        return try {
            val json = JSONObject(body)
            json.optString("message").takeIf { it.isNotBlank() }
                ?: json.optString("error").takeIf { it.isNotBlank() }
                ?: context.getString(R.string.forgot_error_something_wrong)
        } catch (_: Exception) {
            context.getString(R.string.forgot_error_something_wrong)
        }
    }

    fun confirmErrorMessage(code: Int, body: String): String {
        if (code != 400) return parseErrorBody(body)
        return try {
            val json = JSONObject(body)
            when (json.optString("code")) {
                "PASSWORD_TOO_SHORT" -> context.getString(R.string.forgot_error_password_short)
                "PASSWORD_TOO_WEAK", "PASSWORD_POLICY_VIOLATION" -> context.getString(R.string.forgot_error_password_weak)
                else -> context.getString(R.string.forgot_error_update_failed)
            }
        } catch (_: Exception) {
            context.getString(R.string.forgot_error_update_failed)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkBgTop, DarkBgBottom)))
    ) {
        // Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (step) {
                ForgotStep.Email -> TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.forgot_cancel), color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                }
                ForgotStep.VerifyCode, ForgotStep.NewPassword -> IconButton(onClick = {
                    errorMessage = null
                    step = when (step) {
                        ForgotStep.VerifyCode -> ForgotStep.Email
                        ForgotStep.NewPassword -> ForgotStep.VerifyCode
                        else -> step
                    }
                }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = Color.White.copy(alpha = 0.7f))
                }
                ForgotStep.Success -> Spacer(Modifier.size(48.dp))
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            when (step) {
                ForgotStep.Email -> {
                    ForgotPasswordLogoBlock()
                    Spacer(Modifier.height(32.dp))
                    Text(
                        stringResource(R.string.forgot_reset_password),
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.forgot_send_code_subtitle),
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 15.sp,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(Modifier.height(32.dp))
                    GlassTextField(
                        value = email,
                        onValueChange = { email = it; errorMessage = null },
                        label = stringResource(R.string.forgot_email_address),
                        placeholder = "user@example.com",
                        modifier = Modifier.fillMaxWidth()
                    )
                    errorMessage?.let { msg ->
                        Spacer(Modifier.height(8.dp))
                        Text(msg, color = Color.Red.copy(alpha = 0.8f), fontSize = 13.sp, modifier = Modifier.fillMaxWidth())
                    }
                    Spacer(Modifier.height(28.dp))
                    PrimaryPillButton(
                        text = if (loading) "…" else stringResource(R.string.forgot_send_code),
                        onClick = {
                            val trimmed = email.trim()
                            if (trimmed.isEmpty()) return@PrimaryPillButton
                            loading = true
                            errorMessage = null
                            CoroutineScope(Dispatchers.Main).launch {
                                try {
                                    withContext(Dispatchers.IO) { apiRepo.requestPasswordReset(trimmed) }
                                    loading = false
                                    showToast = true
                                } catch (e: Exception) {
                                    loading = false
                                    errorMessage = when (e) {
                                        is ApiException.HttpError -> parseErrorBody(e.body)
                                        is ApiException.NetworkError -> context.getString(R.string.forgot_error_connection)
                                        is java.io.IOException -> context.getString(R.string.forgot_error_connection)
                                        else -> e.message ?: context.getString(R.string.forgot_error_something_wrong)
                                    }
                                }
                            }
                        },
                        enabled = !loading && email.trim().isNotBlank()
                    )
                }
                ForgotStep.VerifyCode -> {
                    ForgotPasswordLogoBlock()
                    Spacer(Modifier.height(32.dp))
                    Text(
                        stringResource(R.string.forgot_enter_code),
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.forgot_sent_to, email.trim()),
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 15.sp,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(Modifier.height(32.dp))
                    CodeInputView(
                        code = code,
                        onCodeChange = { code = it; errorMessage = null }
                    )
                    Spacer(Modifier.height(12.dp))
                    ResendCodeRow(
                        resendCountdown = resendCountdown,
                        onResend = {
                            CoroutineScope(Dispatchers.Main).launch {
                                try {
                                    withContext(Dispatchers.IO) { apiRepo.requestPasswordReset(email.trim()) }
                                    resendCountdown = 60
                                    errorMessage = null
                                } catch (e: Exception) {
                                    errorMessage = when (e) {
                                        is ApiException.HttpError -> if (e.statusCode == 429) context.getString(R.string.forgot_error_too_many_attempts) else parseErrorBody(e.body)
                                        is ApiException.NetworkError -> context.getString(R.string.forgot_error_connection)
                                        else -> e.message ?: context.getString(R.string.forgot_error_something_wrong)
                                    }
                                }
                            }
                        }
                    )
                    errorMessage?.let { msg ->
                        Spacer(Modifier.height(8.dp))
                        Text(msg, color = Color.Red.copy(alpha = 0.8f), fontSize = 13.sp, modifier = Modifier.fillMaxWidth())
                    }
                    Spacer(Modifier.height(28.dp))
                    PrimaryPillButton(
                        text = if (loading) "…" else stringResource(R.string.forgot_verify),
                        onClick = {
                            if (code.length != 6) return@PrimaryPillButton
                            loading = true
                            errorMessage = null
                            CoroutineScope(Dispatchers.Main).launch {
                                try {
                                    val token = withContext(Dispatchers.IO) { apiRepo.verifyPasswordReset(email.trim(), code) }
                                    resetToken = token
                                    loading = false
                                    step = ForgotStep.NewPassword
                                } catch (e: Exception) {
                                    loading = false
                                    errorMessage = when (e) {
                                        is ApiException.HttpError -> when (e.statusCode) {
                                            400 -> context.getString(R.string.forgot_error_invalid_code)
                                            429 -> context.getString(R.string.forgot_error_too_many_attempts)
                                            else -> parseErrorBody(e.body)
                                        }
                                        is ApiException.NetworkError -> context.getString(R.string.forgot_error_connection)
                                        else -> e.message ?: context.getString(R.string.forgot_error_something_wrong)
                                    }
                                }
                            }
                        },
                        enabled = !loading && code.length == 6
                    )
                }
                ForgotStep.NewPassword -> {
                    ForgotPasswordLogoBlock()
                    Spacer(Modifier.height(32.dp))
                    Text(
                        stringResource(R.string.forgot_new_password_title),
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(32.dp))
                    GlassSecureField(
                        value = newPassword,
                        onValueChange = { newPassword = it; errorMessage = null },
                        label = stringResource(R.string.forgot_new_password),
                        placeholder = "••••••••",
                        helper = stringResource(R.string.forgot_password_helper)
                    )
                    Spacer(Modifier.height(12.dp))
                    GlassSecureField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; errorMessage = null },
                        label = stringResource(R.string.forgot_confirm_password),
                        placeholder = "••••••••"
                    )
                    if (confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Check, null, tint = ValidationYellow.copy(alpha = 0.9f), modifier = Modifier.size(14.dp))
                            Spacer(Modifier.size(6.dp))
                            Text(
                                stringResource(R.string.forgot_passwords_dont_match),
                                color = ValidationYellow.copy(alpha = 0.9f),
                                fontSize = 12.sp
                            )
                        }
                    }
                    errorMessage?.let { msg ->
                        Spacer(Modifier.height(8.dp))
                        Text(msg, color = Color.Red.copy(alpha = 0.8f), fontSize = 13.sp, modifier = Modifier.fillMaxWidth())
                    }
                    Spacer(Modifier.height(28.dp))
                    val isValid = newPassword.length >= 8 && newPassword == confirmPassword
                    PrimaryPillButton(
                        text = if (loading) "…" else stringResource(R.string.forgot_update_password),
                        onClick = {
                            if (!isValid) return@PrimaryPillButton
                            loading = true
                            errorMessage = null
                            CoroutineScope(Dispatchers.Main).launch {
                                try {
                                    withContext(Dispatchers.IO) { apiRepo.confirmPasswordReset(resetToken, newPassword) }
                                    loading = false
                                    step = ForgotStep.Success
                                } catch (e: Exception) {
                                    loading = false
                                    errorMessage = when (e) {
                                        is ApiException.HttpError -> confirmErrorMessage(e.statusCode, e.body)
                                        is ApiException.NetworkError -> context.getString(R.string.forgot_error_connection)
                                        else -> e.message ?: context.getString(R.string.forgot_error_update_failed)
                                    }
                                }
                            }
                        },
                        enabled = !loading && isValid
                    )
                }
                ForgotStep.Success -> {
                    Spacer(Modifier.height(48.dp))
                    Box(
                        modifier = Modifier.size(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(PrimaryGreen.copy(alpha = 0.15f), CircleShape)
                        )
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = PrimaryGreen
                        )
                    }
                    Spacer(Modifier.height(32.dp))
                    Text(
                        stringResource(R.string.forgot_success_message),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                    Spacer(Modifier.height(32.dp))
                    PrimaryPillButton(
                        text = stringResource(R.string.forgot_back_to_sign_in),
                        onClick = { onComplete(email.trim()) }
                    )
                }
            }
        }

        // Toast (Step 1 success)
        if (showToast) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 40.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF262626))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Text(
                        stringResource(R.string.forgot_toast_sent),
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }

    // After toast 1.5s → step 2
    LaunchedEffect(showToast) {
        if (showToast) {
            delay(1500L)
            showToast = false
            step = ForgotStep.VerifyCode
            code = ""
            resendCountdown = 60
        }
    }

    // Resend countdown timer (step 2)
    LaunchedEffect(step) {
        if (step != ForgotStep.VerifyCode) return@LaunchedEffect
        while (true) {
            delay(1000L)
            resendCountdown = (resendCountdown - 1).coerceAtLeast(0)
        }
    }
}

@Composable
private fun ForgotPasswordLogoBlock() {
    Box(
        modifier = Modifier.size(88.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(PrimaryGreen.copy(alpha = 0.18f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(Color.White.copy(alpha = 0.05f), CircleShape)
                .border(1.dp, PrimaryGreen.copy(alpha = 0.135f), CircleShape)
        )
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(R.mipmap.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape),
            contentScale = androidx.compose.ui.layout.ContentScale.Fit
        )
    }
}

@Composable
private fun CodeInputView(
    code: String,
    onCodeChange: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val digitBoxShape = RoundedCornerShape(12.dp)
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            stringResource(R.string.forgot_verification_code),
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp
        )
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(6) { index ->
                val digit = code.getOrNull(index)
                val isFocused = code.length == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(digitBoxShape)
                        .background(Color.White.copy(alpha = 0.06f))
                        .border(
                            if (isFocused) 2.dp else 1.dp,
                            if (isFocused) PrimaryGreen.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f),
                            digitBoxShape
                        )
                        .clickable { focusRequester.requestFocus() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = digit?.toString() ?: "•",
                        color = if (digit != null) Color.White else Color.White.copy(alpha = 0.2f),
                        fontSize = 24.sp,
                        fontWeight = if (digit != null) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
        androidx.compose.foundation.text.BasicTextField(
            value = code,
            onValueChange = { new ->
                val digits = new.filter { it.isDigit() }.take(6)
                onCodeChange(digits)
            },
            modifier = Modifier
                .size(1.dp)
                .focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
    }
}

@Composable
private fun ResendCodeRow(
    resendCountdown: Int,
    onResend: () -> Unit
) {
    if (resendCountdown > 0) {
        Text(
            stringResource(R.string.forgot_resend_in, resendCountdown),
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 13.sp
        )
    } else {
        TextButton(onClick = onResend) {
            Text(
                stringResource(R.string.forgot_resend_code),
                color = PrimaryGreen,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun GlassSecureField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    helper: String? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        GlassTextField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            placeholder = placeholder,
            modifier = Modifier.fillMaxWidth(),
            isPassword = true
        )
        if (helper != null) {
            Spacer(Modifier.height(6.dp))
            Text(helper, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
        }
    }
}
