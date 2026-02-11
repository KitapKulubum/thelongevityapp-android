package com.thelongevityapp.android.ui

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
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thelongevityapp.android.R
import com.thelongevityapp.android.data.ApiRepository
import com.thelongevityapp.android.data.SessionRepository
import com.thelongevityapp.android.ui.components.ChatBubbleView
import com.thelongevityapp.android.ui.components.InputBarView
import com.thelongevityapp.android.ui.theme.PrimaryGreen
import com.thelongevityapp.android.ui.theme.SubtitleMint
import com.thelongevityapp.android.ui.theme.TextSecondary
import com.thelongevityapp.android.ui.theme.WarningOrange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// §5.8 AI tab: breathing background, welcome overlay, daily check-in card, warning band, messages, input bar
@Composable
fun ChatScreen(
    apiRepo: ApiRepository,
    session: SessionRepository,
    onSubscriptionRequired: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var message by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<Pair<Boolean, String>>() }
    var loading by remember { mutableStateOf(false) }
    var chatError by remember { mutableStateOf<String?>(null) }
    var checkInExpanded by remember { mutableStateOf(false) }

    val isTodaySubmitted = session.appState.isTodaySubmitted
    var showWelcomeOverlay by remember { mutableStateOf(!session.appState.hasSeenAIWelcomeAfterOnboarding) }

    // Resolve strings at composition time so they can be used in callbacks
    val strDailySuccess = stringResource(R.string.ai_daily_submit_success)
    val strTrendNoChange = stringResource(R.string.ai_daily_trend_no_change)
    val strTrendYounger = stringResource(R.string.ai_daily_trend_younger)
    val strTrendOlder = stringResource(R.string.ai_daily_trend_older)
    val strFollowUp = stringResource(R.string.ai_daily_follow_up)
    val strChatError = stringResource(R.string.ai_chat_error)
    val strInputPlaceholder = stringResource(R.string.ai_input_placeholder)

    fun dismissWelcome() {
        showWelcomeOverlay = false
        session.persistSeenAIWelcome(true)
        if (!session.appState.isTodaySubmitted) checkInExpanded = true
    }

    // Fetch summary on open so isTodaySubmitted is up to date
    LaunchedEffect(Unit) {
        try {
            val summary = apiRepo.getSummary()
            session.persistSummary(summary)
        } catch (_: Exception) { }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Breathing background (AIBreathingPresenceView)
        BreathingGreenBackground(reduceMotion = remember { isReduceMotionEnabled(context) })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Daily Check-in card
            DailyCheckInPinnedCard(
                session = session,
                apiRepo = apiRepo,
                expanded = checkInExpanded,
                onToggle = { checkInExpanded = !checkInExpanded },
                modifier = Modifier.fillMaxWidth(),
                onSubscriptionRequired = onSubscriptionRequired,
                subscriptionRequiredMessage = stringResource(R.string.subscription_required_message),
                onDailyComplete = { deltaYears ->
                    checkInExpanded = false
                    val trendLine = when {
                        deltaYears == null -> strTrendNoChange
                        deltaYears < 0 -> strTrendYounger.format(-deltaYears)
                        else -> strTrendOlder.format(deltaYears)
                    }
                    messages.add(false to "$strDailySuccess\n\n$trendLine\n\n$strFollowUp")
                }
            )

            // "Bugün check-in yapılmadı" warning band — only when daily mode and !isTodaySubmitted
            if (!isTodaySubmitted) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(WarningOrange.copy(alpha = 0.1f))
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "!",
                            color = WarningOrange,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        stringResource(R.string.ai_daily_warning),
                        color = WarningOrange.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            } else {
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Messages area: ChatBubbleView list (no hero when empty)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                messages.forEach { (isUser, text) ->
                    ChatBubbleView(text = text, isUser = isUser, modifier = Modifier.padding(vertical = 6.dp))
                }
                // §4.10: Infinity (∞) when waiting for AI and last message is from user
                if (loading && messages.isNotEmpty() && messages.last().first) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Spacer(modifier = Modifier.padding(horizontal = 18.dp))
                        InfinityWaitingIndicator(reduceMotion = remember { isReduceMotionEnabled(context) })
                    }
                }
                chatError?.let { err ->
                    Text(
                        err,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            InputBarView(
                value = message,
                onValueChange = { message = it },
                onSend = {
                    if (message.isBlank() || loading || checkInExpanded) return@InputBarView
                    val toSend = message.trim()
                    message = ""
                    messages.add(true to toSend)
                    loading = true
                    chatError = null
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            val res = apiRepo.postChat(toSend)
                            messages.add(false to res.answer)
                        } catch (_: Exception) {
                            messages.add(false to strChatError)
                        } finally {
                            loading = false
                        }
                    }
                },
                loading = loading,
                enabled = !checkInExpanded,
                placeholder = strInputPlaceholder,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        // Welcome overlay
        if (showWelcomeOverlay) {
            val welcomeCardBg = Color(red = 0.06f, green = 0.08f, blue = 0.10f)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
                    .clickable(enabled = false) { }
            ) {
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 44.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = welcomeCardBg),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(28.dp)
                                    .clickable { dismissWelcome() }
                            )
                        }
                        val firstName = session.appState.userFirstName?.trim()?.takeIf { it.isNotBlank() }
                        Text(
                            if (firstName != null) stringResource(R.string.ai_welcome_title, firstName) else stringResource(R.string.ai_welcome_title_fallback),
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            stringResource(R.string.ai_welcome_intro),
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 15.sp,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        // Daily check-in row
                        Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryGreen.copy(alpha = 0.9f), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.size(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.ai_welcome_daily_title), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                Text(stringResource(R.string.ai_welcome_daily_desc), color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp, lineHeight = 20.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        // Your biological age row
                        Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.size(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.ai_welcome_age_title), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                Text(stringResource(R.string.ai_welcome_age_desc), color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp, lineHeight = 20.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        // Ask the AI row
                        Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Chat, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.size(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(stringResource(R.string.ai_welcome_ask_title), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                Text(stringResource(R.string.ai_welcome_ask_desc), color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp, lineHeight = 20.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            stringResource(R.string.ai_welcome_cta),
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(PrimaryGreen)
                                .clickable { dismissWelcome() }
                                .padding(vertical = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
