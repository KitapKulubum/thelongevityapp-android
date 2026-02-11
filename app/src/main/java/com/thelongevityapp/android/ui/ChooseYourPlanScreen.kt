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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thelongevityapp.android.R
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkBgTop, ContentGradientBottom)))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 48.dp, bottom = 32.dp)
    ) {
        Text(
            stringResource(R.string.plan_title),
            color = Color.White,
            fontSize = 28.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(R.string.plan_subtitle),
            color = TextSecondary,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        PlanCard(
            title = stringResource(R.string.plan_monthly),
            price = stringResource(R.string.plan_monthly_price),
            description = stringResource(R.string.plan_cancel_anytime),
            selected = selectedPlan == "monthly",
            onClick = { selectedPlan = "monthly" }
        )
        Spacer(modifier = Modifier.height(12.dp))
        PlanCard(
            title = stringResource(R.string.plan_yearly),
            price = stringResource(R.string.plan_yearly_price),
            description = stringResource(R.string.plan_save_yearly),
            badge = stringResource(R.string.plan_badge_best_value),
            selected = selectedPlan == "yearly",
            onClick = { selectedPlan = "yearly" }
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            stringResource(R.string.plan_included),
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 13.sp
        )
        listOf(
            R.string.plan_feature_age,
            R.string.plan_feature_daily,
            R.string.plan_feature_ai,
            R.string.plan_feature_trends
        ).forEach { resId ->
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("✓", color = PrimaryGreen, fontSize = 14.sp, modifier = Modifier.padding(end = 8.dp))
                Text(stringResource(resId), color = TextSecondary, fontSize = 14.sp)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        PrimaryPillButton(
            text = if (selectedPlan == "yearly") stringResource(R.string.plan_continue_yearly) else stringResource(R.string.plan_continue_monthly),
            onClick = {
                session.persistSeenChoosePlan(true)
                onContinue()
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            stringResource(R.string.plan_restore),
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
                .padding(12.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.not_you_log_out),
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 13.sp,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSignOut() }
                .padding(12.dp),
            textAlign = TextAlign.Center
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
