package com.thelongevityapp.android.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thelongevityapp.android.ui.theme.GlassBorder
import com.thelongevityapp.android.ui.theme.GlassFill
import com.thelongevityapp.android.ui.theme.PrimaryGreen
import com.thelongevityapp.android.ui.theme.TextMuted

@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false
) {
    val shape = RoundedCornerShape(28.dp)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp) },
        placeholder = { Text(placeholder, color = TextMuted.copy(alpha = 0.7f)) },
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(GlassFill)
            .border(1.dp, GlassBorder, shape),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedLabelColor = PrimaryGreen,
            unfocusedLabelColor = TextMuted,
            cursorColor = PrimaryGreen,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None
    )
}

@Composable
fun PrimaryPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        enabled = enabled,
        shape = RoundedCornerShape(percent = 50),
        colors = ButtonDefaults.buttonColors(
            containerColor = PrimaryGreen,
            contentColor = Color.Black,
            disabledContainerColor = PrimaryGreen.copy(alpha = 0.6f),
            disabledContentColor = Color.Black.copy(alpha = 0.6f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 12.dp)
    ) {
        Text(text, fontSize = 16.sp, color = Color.Black)
    }
}

// §4.7 OptionButton — Onboarding / Daily seçenekleri
@Composable
fun OptionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false
) {
    androidx.compose.material3.Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = if (selected) PrimaryGreen.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)
        ),
        border = BorderStroke(
            1.dp,
            if (selected) PrimaryGreen.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.1f)
        )
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 15.sp,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)
        )
    }
}

// §4.8 OnboardingProgressBar — track 6dp, fill primary green, "Question X / Y"
@Composable
fun OnboardingProgressBar(
    progress: Float,
    currentQuestion: Int,
    totalQuestions: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        androidx.compose.material3.LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth().height(6.dp).padding(horizontal = 4.dp),
            color = PrimaryGreen,
            trackColor = Color.White.copy(alpha = 0.1f)
        )
        Text(
            "Question $currentQuestion / $totalQuestions • ${(progress * 100).toInt()}%",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

// §4.4 CheckCircle — Terms onay kutusu
@Composable
fun CheckCircle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(22.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (checked) PrimaryGreen else Color.Transparent)
            .border(1.dp, if (checked) PrimaryGreen else Color.White.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .clickable { onCheckedChange(!checked) },
            contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
        }
    }
}

// §4.9 ChatBubbleView — kullanıcı sağa, AI sola; corner 18dp, padding 18/14
@Composable
fun ChatBubbleView(
    text: String,
    isUser: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .padding(horizontal = 18.dp, vertical = 14.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    if (isUser) PrimaryGreen.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)
                )
                .border(
                    1.dp,
                    if (isUser) PrimaryGreen.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f),
                    RoundedCornerShape(18.dp)
                )
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 15.sp,
                lineHeight = 20.sp
            )
        }
    }
}

// §4.10 InputBarView — 54dp, capsule black 0.87, stroke primaryGreen 0.1/0.22, placeholder, send
@Composable
fun InputBarView(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    placeholder: String = "How did today affect my biological age?"
) {
    val shape = RoundedCornerShape(percent = 50)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.White.copy(alpha = 0.62f), fontSize = 16.sp) },
            modifier = Modifier
                .weight(1f)
                .height(54.dp)
                .clip(shape)
                .background(Color.Black.copy(alpha = 0.87f)),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryGreen.copy(alpha = 0.22f),
                unfocusedBorderColor = PrimaryGreen.copy(alpha = 0.1f),
                cursorColor = PrimaryGreen,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = shape
        )
        Button(
            onClick = onSend,
            modifier = Modifier.size(52.dp),
            shape = RoundedCornerShape(percent = 50),
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryGreen,
                contentColor = Color.White,
                disabledContainerColor = Color.White.copy(alpha = 0.15f),
                disabledContentColor = Color.White.copy(alpha = 0.35f)
            ),
            enabled = !loading && value.isNotBlank()
        ) {
            Icon(Icons.Default.Send, contentDescription = "Send")
        }
    }
}
