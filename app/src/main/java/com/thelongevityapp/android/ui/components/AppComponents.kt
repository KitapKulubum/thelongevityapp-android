package com.thelongevityapp.android.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thelongevityapp.android.ui.theme.InputSurfaceDark
import com.thelongevityapp.android.ui.theme.InputSurfaceDarkBottom
import com.thelongevityapp.android.ui.theme.PrimaryGreen
import com.thelongevityapp.android.ui.theme.TextMuted
import java.util.Calendar

private val AuthInputShape = RoundedCornerShape(30.dp)
private val AuthPillShape = RoundedCornerShape(28.dp)

@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    helper: String? = null,
    supportingText: String? = null,
    supportingTextColor: Color = PrimaryGreen.copy(alpha = 0.75f)
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp) },
            placeholder = { Text(placeholder, color = TextMuted.copy(alpha = 0.7f)) },
            modifier = Modifier
                .fillMaxWidth()
                .clip(AuthInputShape)
                .background(
                    Brush.verticalGradient(
                        listOf(InputSurfaceDark, InputSurfaceDarkBottom)
                    )
                ),
            shape = AuthInputShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
                focusedLabelColor = PrimaryGreen,
                unfocusedLabelColor = TextMuted,
                cursorColor = PrimaryGreen,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent
            ),
            singleLine = true,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None
        )
        if (helper != null) {
            Text(
                helper,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
        if (supportingText != null) {
            Text(
                supportingText,
                color = supportingTextColor,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}

@Composable
fun GlassDatePicker(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    helper: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(AuthPillShape)
                .background(
                    Brush.verticalGradient(
                        listOf(InputSurfaceDark, InputSurfaceDarkBottom)
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.08f), AuthPillShape)
                .clickable {
                    val cal = Calendar.getInstance()
                    try {
                        value.takeIf { it.length >= 10 }?.let { s ->
                            val parts = s.split("-")
                            if (parts.size == 3) {
                                cal.set(parts[0].toIntOrNull() ?: cal.get(Calendar.YEAR),
                                    (parts[1].toIntOrNull() ?: 1) - 1,
                                    parts[2].toIntOrNull() ?: 1)
                            }
                        }
                    } catch (_: Exception) { }
                    android.app.DatePickerDialog(
                        context,
                        { _, y, m, d ->
                            onValueChange("%04d-%02d-%02d".format(y, m + 1, d))
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Column {
                Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                Text(
                    text = value.ifBlank { placeholder },
                    color = if (value.isBlank()) TextMuted.copy(alpha = 0.7f) else Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        if (helper != null) {
            Text(
                helper,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 4.dp, top = 2.dp)
            )
        }
    }
}

@Composable
fun PrimaryPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    showTrailingIcon: Boolean = true
) {
    val gradientColors = if (enabled && !loading) {
        listOf(
            com.thelongevityapp.android.ui.theme.PrimaryGreenDark,
            PrimaryGreen
        )
    } else {
        listOf(
            PrimaryGreen.copy(alpha = 0.45f),
            PrimaryGreen.copy(alpha = 0.45f)
        )
    }
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(AuthInputShape)
            .background(
                brush = Brush.verticalGradient(gradientColors),
                shape = AuthInputShape
            ),
        enabled = enabled && !loading,
        shape = AuthInputShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.Black,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Color.Black.copy(alpha = 0.6f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 24.dp)
    ) {
        Text(
            text,
            fontSize = 16.sp,
            color = if (enabled && !loading) Color.Black else Color.Black.copy(alpha = 0.6f)
        )
        if (showTrailingIcon) {
            Spacer(modifier = Modifier.weight(1f))
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.Black,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color.Black
                )
            }
        }
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

// §4.8 OnboardingProgressBar — track 6dp corner 4dp, fill primary green, "Question X / Y" + "Z%"
@Composable
fun OnboardingProgressBar(
    progress: Float,
    currentQuestion: Int,
    totalQuestions: Int,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "progress"
    )
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .background(PrimaryGreen)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Question $currentQuestion / $totalQuestions",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                "${(progress * 100).toInt()}%",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
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
