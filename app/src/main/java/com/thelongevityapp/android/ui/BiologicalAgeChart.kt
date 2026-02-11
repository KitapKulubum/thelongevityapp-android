package com.thelongevityapp.android.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thelongevityapp.android.api.BiologicalAgeChartPoint
import com.thelongevityapp.android.ui.theme.PrimaryGreen
import kotlin.math.max
import kotlin.math.min

private val CHART_HEIGHT_DP = 200
private val CHART_PADDING_H = 16
private val CHART_PADDING_V = 14
private val Y_PADDING_FACTOR = 0.1f
private val MIN_Y_PADDING = 0.5f
private val LINE_STROKE_DP = 3f
private val GRID_ALPHA = 0.12f

/** Aggregate daily series to one point per month (last day's value). */
fun aggregateYearlyToMonthly(series: List<BiologicalAgeChartPoint>): List<BiologicalAgeChartPoint> {
    if (series.isEmpty()) return emptyList()
    val byMonth = series
        .sortedBy { it.date }
        .groupBy { it.date.take(7) } // "YYYY-MM"
    return byMonth.map { (_, points) ->
        points.maxByOrNull { it.date }!! // last day of month
    }.sortedBy { it.date }
}

@Composable
fun BiologicalAgeChart(
    series: List<BiologicalAgeChartPoint>,
    @Suppress("UNUSED_PARAMETER") rangeDeltaYears: Double?,
    isLoading: Boolean,
    error: String?,
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") range: String = "weekly"
) {
    val strokeWidthPx = with(LocalDensity.current) { LINE_STROKE_DP.dp.toPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height((CHART_HEIGHT_DP + CHART_PADDING_V * 2).dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Black,
                        Color.Black.copy(alpha = 0.9f),
                        PrimaryGreen.copy(alpha = 0.15f)
                    )
                )
            )
            .padding(CHART_PADDING_H.dp, CHART_PADDING_V.dp)
    ) {
        when {
            isLoading -> Box(
                modifier = Modifier.fillMaxWidth().height(CHART_HEIGHT_DP.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
            error != null -> Box(
                modifier = Modifier.fillMaxWidth().height(CHART_HEIGHT_DP.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(error, color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
            }
            series.size < 2 -> Box(
                modifier = Modifier.fillMaxWidth().height(CHART_HEIGHT_DP.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Not enough data to show a trend yet.",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp
                )
            }
            else -> {
                val ages = series.map { it.biologicalAgeYears }
                val minAge = ages.minOrNull()!!.toFloat()
                val maxAge = ages.maxOrNull()!!.toFloat()
                val padding = max(MIN_Y_PADDING, (maxAge - minAge) * Y_PADDING_FACTOR)
                val yMin = minAge - padding
                val yMax = maxAge + padding
                val yRange = (yMax - yMin).coerceAtLeast(0.1f)

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(CHART_HEIGHT_DP.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val left = 8f
                    val right = w - 8f
                    val top = 12f
                    val bottom = h - 20f
                    val chartW = right - left
                    val chartH = bottom - top

                    fun yToPy(y: Double): Float = bottom - ((y.toFloat() - yMin) / yRange * chartH)
                    fun xToPx(index: Int): Float = left + (index.toFloat() / (series.size - 1).coerceAtLeast(1)) * chartW

                    // Grid (horizontal)
                    val step = when {
                        yRange <= 1f -> 0.2f
                        yRange <= 3f -> 0.5f
                        else -> 1f
                    }
                    var yTick = (yMin / step).toInt() * step
                    if (yTick < yMin) yTick += step
                    while (yTick <= yMax) {
                        val py = yToPy(yTick.toDouble())
                        drawLine(
                            color = Color.White.copy(alpha = GRID_ALPHA),
                            start = Offset(left, py),
                            end = Offset(right, py),
                            strokeWidth = 1f
                        )
                        yTick += step
                    }

                    // Line: monotone (smooth polyline through points)
                    val path = Path()
                    path.moveTo(xToPx(0), yToPy(series[0].biologicalAgeYears))
                    for (i in 1 until series.size) {
                        path.lineTo(xToPx(i), yToPy(series[i].biologicalAgeYears))
                    }
                    drawPath(
                        path = path,
                        color = PrimaryGreen,
                        style = Stroke(
                            width = strokeWidthPx,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }
    }
}
