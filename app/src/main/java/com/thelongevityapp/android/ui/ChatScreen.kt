package com.thelongevityapp.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thelongevityapp.android.data.ApiRepository
import com.thelongevityapp.android.data.SessionRepository
import com.thelongevityapp.android.ui.components.ChatBubbleView
import com.thelongevityapp.android.ui.components.InputBarView
import com.thelongevityapp.android.ui.theme.ContentGradientBottom
import com.thelongevityapp.android.ui.theme.DarkBgTop
import com.thelongevityapp.android.ui.theme.PrimaryGreen
import com.thelongevityapp.android.ui.theme.SubtitleMint
import com.thelongevityapp.android.ui.theme.TextSecondary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// §5.8 AICoachView — DailyCheckInPinnedCard, mesajlar (ChatBubbleView), InputBarView
@Composable
fun ChatScreen(
    apiRepo: ApiRepository,
    session: SessionRepository,
    modifier: Modifier = Modifier
) {
    var message by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<Pair<Boolean, String>>() }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    if (messages.isEmpty()) {
        messages.add(false to "Ask The Longevity App something to get started.")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkBgTop, ContentGradientBottom)))
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
            Text(
                "The Longevity App is ready.",
                color = androidx.compose.ui.graphics.Color.White,
                fontSize = 34.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Let's optimize your healthspan.",
                color = SubtitleMint,
                fontSize = 22.sp
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        DailyCheckInPinnedCard(session = session, apiRepo = apiRepo, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            messages.forEach { (isUser, text) ->
                ChatBubbleView(text = text, isUser = isUser)
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (loading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = PrimaryGreen, modifier = Modifier.size(24.dp))
                    Text("Thinking...", color = TextSecondary, fontSize = 16.sp)
                }
            }
            error?.let {
                Text(it, color = androidx.compose.ui.graphics.Color.Red, fontSize = 14.sp, modifier = Modifier.padding(18.dp))
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        InputBarView(
            value = message,
            onValueChange = { message = it },
            onSend = {
                if (message.isBlank() || loading) return@InputBarView
                val toSend = message.trim()
                message = ""
                messages.add(true to toSend)
                loading = true
                error = null
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val res = apiRepo.postChat(toSend)
                        messages.add(false to res.answer)
                    } catch (e: Exception) {
                        error = e.message
                    } finally {
                        loading = false
                    }
                }
            },
            loading = loading,
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }
}
