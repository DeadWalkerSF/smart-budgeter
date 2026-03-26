package com.suman.smartbudgeter.ui.components

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.suman.smartbudgeter.domain.CategoryBreakdownSlice
import com.suman.smartbudgeter.domain.SpendingTrendPoint
import com.suman.smartbudgeter.domain.util.asCompactCurrency
import com.suman.smartbudgeter.domain.util.asCurrency
import com.suman.smartbudgeter.domain.util.asPercent
import com.suman.smartbudgeter.domain.util.toComposeColor
import com.suman.smartbudgeter.ui.theme.BudgetGold
import com.suman.smartbudgeter.ui.theme.Graphite
import com.suman.smartbudgeter.ui.theme.SoftGold
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

@Composable
fun SpendingTrendChart(
    points: List<SpendingTrendPoint>,
    modifier: Modifier = Modifier,
) {
    if (points.isEmpty()) {
        Text(
            text = "No spending trend yet.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }

    val maxValue = points.maxOfOrNull { it.amount }?.takeIf { it > 0.0 } ?: 1.0

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        points.forEach { point ->
            val ratio = (point.amount / maxValue).toFloat().coerceIn(0f, 1f)
            val barHeight = when {
                point.amount <= 0.0 -> 10.dp
                else -> max(30f, ratio * 146f).dp
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = point.amount.asCompactCurrency(),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (point.amount > 0.0) BudgetGold else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(168.dp),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Graphite.copy(alpha = 0.26f)),
                    )
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height(barHeight)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                brush = if (point.amount > 0.0) {
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            SoftGold.copy(alpha = 0.72f),
                                            BudgetGold,
                                        ),
                                    )
                                } else {
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Graphite.copy(alpha = 0.52f),
                                            Graphite.copy(alpha = 0.86f),
                                        ),
                                    )
                                },
                            ),
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = point.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryDonutChart(
    slices: List<CategoryBreakdownSlice>,
    modifier: Modifier = Modifier,
) {
    if (slices.isEmpty()) {
        Text(
            text = "Add a few transactions to unlock the donut chart.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        return
    }

    val total = slices.sumOf { it.amount }.takeIf { it > 0.0 } ?: 1.0
    val orderedSlices = slices.sortedByDescending { it.amount }
    var selectedSliceIndex by remember(orderedSlices) { mutableStateOf<Int?>(null) }
    var chartSize by remember { mutableStateOf(IntSize.Zero) }
    val selectedSlice = selectedSliceIndex?.let(orderedSlices::getOrNull)

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .onSizeChanged { chartSize = it }
                .pointerInput(orderedSlices) {
                    detectTapGestures { offset ->
                        selectedSliceIndex = findSliceIndex(
                            offset = offset,
                            chartSize = chartSize,
                            slices = orderedSlices,
                            total = total,
                        )
                    }
                }
                .pointerInteropFilter { event ->
                    when (event.actionMasked) {
                        MotionEvent.ACTION_HOVER_MOVE -> {
                            selectedSliceIndex = findSliceIndex(
                                offset = Offset(event.x, event.y),
                                chartSize = chartSize,
                                slices = orderedSlices,
                                total = total,
                            )
                            true
                        }

                        MotionEvent.ACTION_HOVER_EXIT,
                        MotionEvent.ACTION_CANCEL -> {
                            selectedSliceIndex = null
                            true
                        }

                        else -> false
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 24.dp.toPx()
                val selectedStrokeWidth = 28.dp.toPx()
                val arcSize = Size(size.width - selectedStrokeWidth, size.height - selectedStrokeWidth)
                val topLeft = Offset(selectedStrokeWidth / 2f, selectedStrokeWidth / 2f)
                val segmentGap = if (orderedSlices.size > 1) 2.5f else 0f

                drawArc(
                    color = Graphite.copy(alpha = 0.28f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                )

                var startAngle = -90f
                orderedSlices.forEachIndexed { index, slice ->
                    val rawSweep = ((slice.amount / total) * 360f).toFloat()
                    val visibleSweep = (rawSweep - segmentGap).coerceAtLeast(0.8f)
                    val isSelected = selectedSliceIndex == index
                    val alpha = if (selectedSliceIndex == null || isSelected) 1f else 0.38f

                    drawArc(
                        color = slice.colorHex.toComposeColor().copy(alpha = alpha),
                        startAngle = startAngle + (segmentGap / 2f),
                        sweepAngle = visibleSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(
                            width = if (isSelected) selectedStrokeWidth else strokeWidth,
                            cap = StrokeCap.Round,
                        ),
                    )
                    startAngle += rawSweep
                }
            }

            Box(
                modifier = Modifier
                    .size(118.dp)
                    .clip(CircleShape)
                    .background(Graphite.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 12.dp),
                ) {
                    Text(
                        text = selectedSlice?.label ?: "Total Spent",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = (selectedSlice?.amount ?: total).asCompactCurrency(),
                        style = MaterialTheme.typography.titleLarge,
                        color = BudgetGold,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = if (selectedSlice != null) {
                            ((selectedSlice.amount / total) * 100).asPercent()
                        } else {
                            "${orderedSlices.size} categories"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

private fun findSliceIndex(
    offset: Offset,
    chartSize: IntSize,
    slices: List<CategoryBreakdownSlice>,
    total: Double,
): Int? {
    if (chartSize == IntSize.Zero || slices.isEmpty() || total <= 0.0) return null

    val minDimension = min(chartSize.width.toFloat(), chartSize.height.toFloat())
    val outerStrokeWidth = minDimension * (28f / 220f)
    val baseStrokeWidth = minDimension * (24f / 220f)
    val width = chartSize.width.toFloat()
    val height = chartSize.height.toFloat()
    val centerX = width / 2f
    val centerY = height / 2f
    val outerRadius = (min(width, height) - outerStrokeWidth) / 2f
    val innerRadius = outerRadius - baseStrokeWidth
    val dx = offset.x - centerX
    val dy = offset.y - centerY
    val distance = hypot(dx.toDouble(), dy.toDouble()).toFloat()

    if (distance < innerRadius || distance > outerRadius + (outerStrokeWidth / 2f)) {
        return null
    }

    val angle = ((Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())) + 450.0) % 360.0).toFloat()
    var currentSweep = 0f

    slices.forEachIndexed { index, slice ->
        val sliceSweep = ((slice.amount / total) * 360f).toFloat()
        if (angle >= currentSweep && angle < currentSweep + sliceSweep) {
            return index
        }
        currentSweep += sliceSweep
    }

    return null
}
