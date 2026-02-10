package com.thelongevityapp.android.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import com.thelongevityapp.android.ui.BreathingGreenBackground
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
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
import com.thelongevityapp.android.ui.theme.MeaningBackground
import com.thelongevityapp.android.ui.theme.PrimaryGreen

private data class MeaningPage(val titleRes: Int, val subtitleRes: Int)

private val meaningPages = listOf(
    MeaningPage(R.string.meaning_page0_title, R.string.meaning_page0_subtitle),
    MeaningPage(R.string.meaning_page1_title, R.string.meaning_page1_subtitle),
    MeaningPage(R.string.meaning_page2_title, R.string.meaning_page2_subtitle)
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MeaningOnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val reduceMotion = remember { isReduceMotionEnabled(context) }
    val pagerState = rememberPagerState(pageCount = { meaningPages.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MeaningBackground)
    ) {
        BreathingGreenBackground(reduceMotion = reduceMotion)

        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, end = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                TextButton(onClick = onComplete) {
                    Text(
                        stringResource(R.string.meaning_skip),
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 15.sp
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                userScrollEnabled = true
            ) { page ->
                val pageData = meaningPages[page]
                val ctx = LocalContext.current
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = ctx.getString(pageData.titleRes),
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = ctx.getString(pageData.subtitleRes),
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 5
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(0.dp, 0.dp, 0.dp, 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                meaningPages.forEachIndexed { index, _ ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(if (isSelected) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) PrimaryGreen else Color.White.copy(alpha = 0.4f)
                            )
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 24.dp)
            ) {
                Button(
                    onClick = {
                        if (pagerState.currentPage < meaningPages.size - 1) {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        } else {
                            onComplete()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen, contentColor = Color.Black)
                ) {
                    Text(
                        text = if (pagerState.currentPage < meaningPages.size - 1) stringResource(R.string.meaning_continue) else stringResource(R.string.meaning_start),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    stringResource(R.string.meaning_trust_line),
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
