package com.thelongevityapp.android.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thelongevityapp.android.BuildConfig
import com.thelongevityapp.android.R
import com.thelongevityapp.android.auth.AuthManager
import com.thelongevityapp.android.data.ApiRepository
import com.thelongevityapp.android.data.SessionRepository
import com.thelongevityapp.android.ui.theme.PrimaryGreen
import com.thelongevityapp.android.ui.theme.TextSecondary
import com.thelongevityapp.android.ui.theme.ValidationYellow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class LanguageOption(val displayName: String, val code: String)

private val LANGUAGES = listOf(
    LanguageOption("English", "en"),
    LanguageOption("Türkçe", "tr"),
    LanguageOption("Español", "es"),
    LanguageOption("Français", "fr"),
    LanguageOption("Deutsch", "de")
)

private fun languageDisplayName(code: String): String = LANGUAGES.find { it.code == code }?.displayName ?: code.uppercase()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    session: SessionRepository,
    apiRepo: ApiRepository,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val appState = session.appState
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var deleteLoading by remember { mutableStateOf(false) }
    var isEmailVerificationBannerDismissed by remember { mutableStateOf(false) }
    var showEmailVerificationSheet by remember { mutableStateOf(false) }
    var showTerms by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }
    var showLanguageSheet by remember { mutableStateOf(false) }
    var subscriptionStatus by remember { mutableStateOf<String?>(null) }
    var selectedLanguage by remember { mutableStateOf(languageDisplayName(session.getStoredLanguage())) }
    var contentAlpha by remember { mutableStateOf(0f) }
    val scope = rememberCoroutineScope()

    val isEmailVerified = AuthManager.isEmailVerified
    val showEmailBanner = !isEmailVerified && !isEmailVerificationBannerDismissed
    val displayName = listOfNotNull(appState.userFirstName, appState.userLastName).joinToString(" ").ifBlank { null }

    LaunchedEffect(Unit) {
        AuthManager.reloadUser()
        if (AuthManager.isEmailVerified) isEmailVerificationBannerDismissed = true
        selectedLanguage = languageDisplayName(session.getStoredLanguage())
        contentAlpha = 1f
        subscriptionStatus = withContext(Dispatchers.IO) {
            kotlin.runCatching { apiRepo.getSubscriptionStatus() }.getOrNull()?.subscription?.status
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .alpha(contentAlpha)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 48.dp, bottom = 60.dp)
    ) {
        // Header
        Text(
            stringResource(R.string.profile_header),
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        displayName?.let { name ->
            Text(
                name,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.3).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(32.dp))

        // Email verification banner
        if (showEmailBanner) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showEmailVerificationSheet = true },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, PrimaryGreen.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.size(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.profile_verify_email), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text(stringResource(R.string.profile_verify_email_subtitle), color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
                    }
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { isEmailVerificationBannerDismissed = true }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Settings card (Membership, Language, Legal)
        MinimalCard(modifier = Modifier.fillMaxWidth()) {
            ProfileRow(
                icon = Icons.Default.Star,
                title = stringResource(R.string.profile_membership),
                subtitle = subscriptionStatus?.let { if (it.equals("active", true) || it.equals("trial", true)) stringResource(R.string.profile_membership_subtitle) else it } ?: stringResource(R.string.profile_membership_not_active),
                onClick = {
                    if (!AuthManager.isEmailVerified) showEmailVerificationSheet = true
                    else {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/account/subscriptions"))
                        try { context.startActivity(intent) } catch (_: Exception) { }
                    }
                }
            )
            DividerLine()
            LanguageRow(
                currentDisplayName = selectedLanguage,
                onOpenSheet = { showLanguageSheet = true }
            )
            DividerLine()
            ProfileRow(
                icon = Icons.Default.Description,
                title = stringResource(R.string.profile_terms),
                subtitle = null,
                onClick = { showTerms = true }
            )
            DividerLine()
            // Privacy
            ProfileRow(
                icon = Icons.Default.Lock,
                title = stringResource(R.string.profile_privacy),
                subtitle = null,
                onClick = { showPrivacy = true }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Account actions
        Text(
            stringResource(R.string.profile_logout),
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 15.sp,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showLogoutConfirm = true }
                .padding(vertical = 12.dp)
        )
        Text(
            stringResource(R.string.profile_delete_account),
            color = Color.White.copy(alpha = 0.35f),
            fontSize = 13.sp,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (!AuthManager.isEmailVerified) showEmailVerificationSheet = true
                    else showDeleteConfirm = true
                }
                .padding(vertical = 8.dp)
        )
        Text(
            stringResource(R.string.profile_delete_permanent),
            color = Color.White.copy(alpha = 0.3f),
            fontSize = 11.sp,
            modifier = Modifier.padding(top = 2.dp)
        )

        // DEBUG: Bypass Verify
        if (BuildConfig.DEBUG) {
            Spacer(modifier = Modifier.height(16.dp))
            var isBypassing by remember { mutableStateOf(false) }
            TextButton(
                onClick = {
                    isBypassing = true
                    kotlinx.coroutines.CoroutineScope(Dispatchers.Main).launch {
                        kotlin.runCatching {
                            withContext(Dispatchers.IO) { apiRepo.postBypassVerify() }
                            AuthManager.reloadUser()
                            showEmailVerificationSheet = false
                        }
                        isBypassing = false
                    }
                },
                enabled = !isBypassing
            ) {
                Text(stringResource(R.string.profile_bypass_verify_test), color = ValidationYellow.copy(alpha = 0.8f), fontSize = 13.sp)
            }
        }
    }

    // Logout alert
    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text(stringResource(R.string.profile_logout_alert_title)) },
            text = { Text(stringResource(R.string.profile_logout_alert_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutConfirm = false
                    scope.launch {
                        val token = kotlin.runCatching { withContext(Dispatchers.IO) { AuthManager.getIdToken() } }.getOrNull()
                        AuthManager.signOut()
                        onSignOut()
                        token?.let { kotlin.runCatching { apiRepo.postLogout(it) } }
                    }
                }) { Text(stringResource(R.string.profile_logout_confirm), color = PrimaryGreen) }
            },
            dismissButton = { TextButton(onClick = { showLogoutConfirm = false }) { Text(stringResource(R.string.profile_cancel), color = TextSecondary) } }
        )
    }

    // Delete alert
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { if (!deleteLoading) showDeleteConfirm = false },
            title = { Text(stringResource(R.string.profile_delete_alert_title)) },
            text = { Text(stringResource(R.string.profile_delete_alert_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteLoading = true
                        kotlinx.coroutines.CoroutineScope(Dispatchers.Main).launch {
                            kotlin.runCatching { withContext(Dispatchers.IO) { apiRepo.deleteAccount() } }
                                .onSuccess {
                                    AuthManager.signOut()
                                    showDeleteConfirm = false
                                    deleteLoading = false
                                    onSignOut()
                                }
                                .onFailure { deleteLoading = false }
                        }
                    },
                    enabled = !deleteLoading
                ) { Text(if (deleteLoading) "…" else stringResource(R.string.profile_delete_confirm), color = Color.Red) }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text(stringResource(R.string.profile_cancel), color = TextSecondary) } }
        )
    }

    if (showEmailVerificationSheet) {
        EmailVerificationRequiredSheet(
            apiRepo = apiRepo,
            onDismiss = { showEmailVerificationSheet = false }
        )
    }
    if (showTerms) {
        LegalSheet(
            title = stringResource(R.string.profile_terms),
            onDismiss = { showTerms = false },
            loadContent = { apiRepo.getTerms().content }
        )
    }
    if (showPrivacy) {
        LegalSheet(
            title = stringResource(R.string.profile_privacy),
            onDismiss = { showPrivacy = false },
            loadContent = { apiRepo.getPrivacy().content }
        )
    }
    if (showLanguageSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLanguageSheet = false },
            containerColor = Color.Black,
            contentColor = Color.White
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(stringResource(R.string.profile_language), fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(16.dp))
                LANGUAGES.forEach { opt ->
                    Text(
                        opt.displayName,
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                session.setLanguage(opt.code)
                                selectedLanguage = opt.displayName
                                showLanguageSheet = false
                                scope.launch {
                                    kotlin.runCatching { apiRepo.patchProfile(opt.code) }.onSuccess { appState.updateFromProfile(it.profile) }
                                    activity?.recreate()
                                }
                            }
                            .padding(vertical = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MinimalCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color.White.copy(alpha = 0.05f), Color.White.copy(alpha = 0.03f))))
        ) {
            Column(modifier = Modifier.padding(0.dp)) { content() }
        }
    }
}

@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color.White.copy(alpha = 0.1f))
    )
}

@Composable
private fun ProfileRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            if (subtitle != null) Text(subtitle, color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun LanguageRow(
    currentDisplayName: String,
    onOpenSheet: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenSheet)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Public, contentDescription = null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.profile_language), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(currentDisplayName, color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(20.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmailVerificationRequiredSheet(
    apiRepo: ApiRepository,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isVerified by remember { mutableStateOf(false) }
    var resendSuccessMessage by remember { mutableStateOf(false) }
    var resendCooldownSeconds by remember { mutableStateOf(0) }
    var isSending by remember { mutableStateOf(false) }
    var resendAttemptCount by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    var pollingJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(Unit) {
        val initiallyVerified = kotlin.runCatching {
            val token = AuthManager.getIdToken()
            apiRepo.postAuthMe(idToken = token).emailVerified == true
        }.getOrNull() ?: false
        if (initiallyVerified) {
            isVerified = true
            AuthManager.reloadUser()
            return@LaunchedEffect
        }
        pollingJob = scope.launch {
            while (true) {
                delay(3000L)
                if (isVerified) break
                val nowVerified = kotlin.runCatching {
                    val token = AuthManager.getIdToken()
                    apiRepo.postAuthMe(idToken = token).emailVerified == true
                }.getOrNull() ?: false
                if (nowVerified) {
                    isVerified = true
                    AuthManager.reloadUser()
                    break
                }
            }
            pollingJob = null
        }
    }
    LaunchedEffect(resendCooldownSeconds) {
        if (resendCooldownSeconds <= 0) return@LaunchedEffect
        delay(1000L)
        resendCooldownSeconds = (resendCooldownSeconds - 1).coerceAtLeast(0)
    }

    ModalBottomSheet(
        onDismissRequest = {
            pollingJob?.cancel()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = Color(red = 0.06f, green = 0.12f, blue = 0.1f),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            if (!isVerified) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.profile_email_verification_required),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    TextButton(onClick = {
                        pollingJob?.cancel()
                        onDismiss()
                    }) {
                        Text(stringResource(R.string.profile_later), color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Icon(
                    Icons.Default.Email,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    stringResource(R.string.profile_email_verification_required_body),
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 15.sp,
                    lineHeight = 22.sp
                )
                if (resendSuccessMessage) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(stringResource(R.string.profile_verification_sent), color = PrimaryGreen.copy(alpha = 0.9f), fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(24.dp))
                if (isSending) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        androidx.compose.material3.CircularProgressIndicator(color = PrimaryGreen, modifier = Modifier.size(24.dp))
                    }
                } else {
                    val cooldownMin = resendCooldownSeconds / 60
                    val cooldownSec = resendCooldownSeconds % 60
                    val enabled = resendCooldownSeconds == 0
                    Text(
                        if (resendCooldownSeconds > 0)
                            stringResource(R.string.profile_resend_cooldown, cooldownMin, cooldownSec)
                        else
                            stringResource(R.string.profile_resend_verification),
                        color = if (enabled) Color.Black else Color.White.copy(alpha = 0.48f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (enabled) PrimaryGreen else Color.White.copy(alpha = 0.2f))
                            .clickable(enabled = enabled) {
                                if (!enabled || isSending) return@clickable
                                isSending = true
                                scope.launch {
                                    kotlin.runCatching {
                                        AuthManager.sendEmailVerification()
                                        withContext(Dispatchers.Main) {
                                            resendSuccessMessage = true
                                            resendCooldownSeconds = 60
                                            resendAttemptCount++
                                        }
                                    }
                                    isSending = false
                                }
                            }
                            .padding(vertical = 16.dp),
                        textAlign = TextAlign.Center
                    )
                }
                if (resendAttemptCount >= 2) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        stringResource(R.string.profile_check_spam_hint),
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                }
            } else {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    stringResource(R.string.profile_email_verified_title),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    stringResource(R.string.profile_email_verified_body),
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    stringResource(R.string.profile_continue),
                    color = Color.Black,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(PrimaryGreen)
                        .clickable { onDismiss() }
                        .padding(vertical = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LegalSheet(
    title: String,
    onDismiss: () -> Unit,
    loadContent: suspend () -> String
) {
    var content by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        content = kotlin.runCatching { loadContent() }.getOrNull()
        loading = false
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Black,
        contentColor = Color.White
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(16.dp))
            if (loading) Text("Loading…", color = Color.White.copy(alpha = 0.6f))
            else content?.let { Text(it, color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp) }
        }
    }
}
