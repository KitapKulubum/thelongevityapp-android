package com.thelongevityapp.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thelongevityapp.android.data.ApiRepository
import com.thelongevityapp.android.data.SessionRepository
import com.thelongevityapp.android.ui.theme.ContentGradientBottom
import com.thelongevityapp.android.ui.theme.PrimaryGreen
import com.thelongevityapp.android.ui.theme.SendButtonGreen

@Composable
private fun RowScope.TabItem(
    index: Int,
    label: String,
    icon: ImageVector,
    selectedTab: Int,
    onSelect: (Int) -> Unit
) {
    val selected = selectedTab == index
    val tint = if (selected) SendButtonGreen else Color.White.copy(alpha = 0.6f)
    Row(
        modifier = Modifier
            .weight(1f)
            .clickable { onSelect(index) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.size(8.dp))
        Text(text = label, color = tint, fontSize = 14.sp)
    }
}

@Composable
fun MainTabScreen(
    session: SessionRepository,
    apiRepo: ApiRepository,
    onSignOut: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    Scaffold(
        modifier = Modifier.background(ContentGradientBottom),
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TabItem(0, "AI", Icons.Default.AutoAwesome, selectedTab) { selectedTab = it }
                TabItem(1, "Age", Icons.Default.ShowChart, selectedTab) { selectedTab = it }
                TabItem(2, "Profile", Icons.Default.Person, selectedTab) { selectedTab = it }
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> ChatScreen(apiRepo = apiRepo, session = session, modifier = Modifier.padding(padding))
            1 -> ScoreScreen(session = session, apiRepo = apiRepo, modifier = Modifier.padding(padding))
            2 -> ProfileScreen(session = session, apiRepo = apiRepo, onSignOut = onSignOut, modifier = Modifier.padding(padding))
        }
    }
}
