package com.thelongevityapp.android.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thelongevityapp.android.auth.AuthManager
import com.thelongevityapp.android.data.ApiRepository
import com.thelongevityapp.android.data.SessionRepository
import com.thelongevityapp.android.ui.components.PrimaryPillButton
import com.thelongevityapp.android.ui.theme.CardStroke
import com.thelongevityapp.android.ui.theme.ContentGradientBottom
import com.thelongevityapp.android.ui.theme.DarkBgTop
import com.thelongevityapp.android.ui.theme.GlassFill
import com.thelongevityapp.android.ui.theme.PrimaryGreen
import com.thelongevityapp.android.ui.theme.TextSecondary

@Composable
fun ProfileScreen(
    session: SessionRepository,
    apiRepo: ApiRepository,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appState = session.appState
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var deleteLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var subscriptionStatus by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        subscriptionStatus = withContext(Dispatchers.IO) {
            kotlin.runCatching { apiRepo.getSubscriptionStatus() }.getOrNull()?.subscription?.status ?: "—"
        }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkBgTop, ContentGradientBottom)))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp)
            .padding(top = 40.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Profile", color = Color.White, fontSize = 28.sp)
        Spacer(modifier = Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = GlassFill),
            border = BorderStroke(1.dp, CardStroke)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                appState.userFirstName?.let { Text("$it ${appState.userLastName ?: ""}", color = Color.White, fontSize = 18.sp) }
                appState.userChronologicalAge?.let { Text("Age: %.0f".format(it), color = TextSecondary, fontSize = 16.sp) }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = GlassFill),
            border = BorderStroke(1.dp, CardStroke)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Language", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                Text(session.getStoredLanguage().uppercase(), color = PrimaryGreen, fontSize = 16.sp)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = GlassFill),
            border = BorderStroke(1.dp, CardStroke)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Membership", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                Text(subscriptionStatus ?: "Loading…", color = TextSecondary, fontSize = 14.sp)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = GlassFill),
            border = BorderStroke(1.dp, CardStroke)
        ) {
            Column {
                Text(
                    "Privacy Policy",
                    color = PrimaryGreen,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { }
                        .padding(16.dp)
                )
                Text(
                    "Terms of Service",
                    color = PrimaryGreen,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { }
                        .padding(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        PrimaryPillButton(
            text = "Log out",
            onClick = { showLogoutConfirm = true },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Delete account",
            color = Color.White.copy(alpha = 0.35f),
            fontSize = 13.sp,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDeleteConfirm = true }
                .padding(8.dp)
        )
        Text(
            "This action is permanent",
            color = Color.White.copy(alpha = 0.3f),
            fontSize = 11.sp,
            modifier = Modifier.padding(8.dp)
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
                    scope.launch {
                        val token = kotlin.runCatching { withContext(Dispatchers.IO) { AuthManager.getIdToken() } }.getOrNull()
                        AuthManager.signOut()
                        token?.let { kotlin.runCatching { apiRepo.postLogout(it) } }
                        onSignOut()
                    }
                }) { Text("Log out", color = PrimaryGreen) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) { Text("Cancel", color = TextSecondary) }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { if (!deleteLoading) showDeleteConfirm = false },
            title = { Text("Delete account") },
            text = { Text("This action is permanent. All your data will be deleted. Are you sure?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteLoading = true
                        scope.launch {
                            kotlin.runCatching {
                                withContext(Dispatchers.IO) { apiRepo.deleteAccount() }
                            }.onSuccess {
                                AuthManager.signOut()
                                showDeleteConfirm = false
                                deleteLoading = false
                                onSignOut()
                            }.onFailure {
                                deleteLoading = false
                            }
                        }
                    },
                    enabled = !deleteLoading
                ) { Text(if (deleteLoading) "…" else "Delete", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel", color = TextSecondary) }
            }
        )
    }
}
