package com.thelongevityapp.android.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.thelongevityapp.android.auth.AuthManager
import com.thelongevityapp.android.data.SessionRepository
import com.thelongevityapp.android.ui.components.PrimaryPillButton
import com.thelongevityapp.android.ui.theme.ContentGradientBottom
import com.thelongevityapp.android.ui.theme.DarkBgTop
import com.thelongevityapp.android.ui.theme.GlassFill
import com.thelongevityapp.android.ui.theme.PrimaryGreen
import com.thelongevityapp.android.ui.theme.TextSecondary

// §5.6 ChooseYourPlanScreen — plan kartları Monthly/Yearly, CTA, Restore
@Composable
fun ChooseYourPlanScreen(
    session: SessionRepository,
    onContinue: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPlan by remember { mutableStateOf("yearly") }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkBgTop, ContentGradientBottom)))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp, bottom = 32.dp)
    ) {
        Text(
            "Choose how you'd like to continue",
            color = Color.White,
            fontSize = 28.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Both plans include full access to biological age tracking and AI insights.",
            color = TextSecondary,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        PlanCard(
            title = "Monthly",
            price = "Monthly price",
            description = "Cancel anytime",
            selected = selectedPlan == "monthly",
            onClick = { selectedPlan = "monthly" }
        )
        Spacer(modifier = Modifier.height(12.dp))
        PlanCard(
            title = "Yearly",
            price = "Best value — billed yearly",
            description = "Save compared to monthly",
            badge = "Best value",
            selected = selectedPlan == "yearly",
            onClick = { selectedPlan = "yearly" }
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "Included in both plans",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 13.sp
        )
        listOf(
            "Biological age tracking",
            "Daily check-in insights",
            "AI coach and recommendations",
            "Trend charts and impact factors"
        ).forEach { item ->
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("✓", color = PrimaryGreen, fontSize = 14.sp, modifier = Modifier.padding(end = 8.dp))
                Text(item, color = TextSecondary, fontSize = 14.sp)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        PrimaryPillButton(
            text = if (selectedPlan == "yearly") "Continue with Yearly" else "Continue with Monthly",
            onClick = {
                session.persistSeenChoosePlan(true)
                onContinue()
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Restore purchases",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
                .padding(12.dp)
        )
        Text(
            "Log out",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 14.sp,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showLogoutConfirm = true }
                .padding(12.dp)
        )
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Log out") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutConfirm = false
                    AuthManager.signOut()
                    onSignOut()
                }) { Text("Log out", color = PrimaryGreen) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) { Text("Cancel", color = TextSecondary) }
            }
        )
    }
}

@Composable
private fun PlanCard(
    title: String,
    price: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    badge: String? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.04f)
        ),
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) PrimaryGreen.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.08f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            badge?.let {
                Text(
                    it,
                    color = PrimaryGreen,
                    fontSize = 11.sp,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .background(PrimaryGreen.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Text(title, color = Color.White, fontSize = 18.sp)
            Text(price, color = TextSecondary, fontSize = 14.sp)
            Text(description, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
        }
    }
}
