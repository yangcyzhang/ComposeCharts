package com.charts.compose.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.charts.compose.core.*
import com.charts.compose.data.PieDataSet
import com.charts.compose.data.PieEntry
import kotlin.math.*

@OptIn(ExperimentalTextApi::class)
@Composable
fun PieChart(
    dataSet: PieDataSet,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    var selectedSliceIndex by remember { mutableStateOf(-1) }
    var highlightedOffset by remember { mutableStateOf<Offset?>(null) }
    var highlightedEntry by remember { mutableStateOf<PieEntry?>(null) }

    val totalValue = remember(dataSet) { dataSet.entries.sumOf { it.value.toDouble() }.toFloat() }
    val defaultColors = listOf(
        Color(0xFF00ADB5), Color(0xFFFF5252), Color(0xFF4CAF50),
        Color(0xFFFFB300), Color(0xFF9C27B0), Color(0xFFFF9800)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(dataSet) {
                detectTapGestures { tapOffset ->
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val outerRadius = min(size.width, size.height) / 2.5f
                    val holeRadius = outerRadius * dataSet.holeRadiusRatio

                    val touchVector = tapOffset - center
                    val touchDist = touchVector.getDistance()

                    if (touchDist in holeRadius..outerRadius + dataSet.selectionShift) {
                        var rawAngle = (atan2(touchVector.y, touchVector.x) * 180f / PI.toFloat())
                        if (rawAngle < 0) rawAngle += 360f

                        val angleFromTop = (rawAngle + 90f) % 360f

                        var startAngle = 0f
                        dataSet.entries.forEachIndexed { index, entry ->
                            val sweepAngle = (entry.value / totalValue) * 360f
                            if (angleFromTop in startAngle..(startAngle + sweepAngle)) {
                                selectedSliceIndex = if (selectedSliceIndex == index) -1 else index
                                highlightedEntry = entry
                                highlightedOffset = tapOffset
                                return@detectTapGestures
                            }
                            startAngle += sweepAngle
                        }
                    } else {
                        selectedSliceIndex = -1
                        highlightedEntry = null
                        highlightedOffset = null
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = min(size.width, size.height) / 2.5f
            val holeRadius = outerRadius * dataSet.holeRadiusRatio

            var currentAngle = -90f

            dataSet.entries.forEachIndexed { index, entry ->
                val sweepAngle = (entry.value / totalValue) * 360f
                val isSelected = selectedSliceIndex == index
                val color = if (entry.color != Color.Unspecified) entry.color else defaultColors[index % defaultColors.size]

                val shift = if (isSelected) dataSet.selectionShift else 0f
                val midAngleRad = (currentAngle + sweepAngle / 2f) * PI.toFloat() / 180f
                val shiftDx = cos(midAngleRad) * shift
                val shiftDy = sin(midAngleRad) * shift
                val sliceCenter = Offset(center.x + shiftDx, center.y + shiftDy)

                drawArc(
                    color = color,
                    startAngle = currentAngle + dataSet.sliceSpace / 2f,
                    sweepAngle = sweepAngle - dataSet.sliceSpace,
                    useCenter = true,
                    topLeft = Offset(sliceCenter.x - outerRadius, sliceCenter.y - outerRadius),
                    size = Size(outerRadius * 2f, outerRadius * 2f)
                )

                if (dataSet.holeRadiusRatio > 0f) {
                    drawCircle(
                        color = Color(0xFF070B15),
                        radius = holeRadius,
                        center = center
                    )
                }

                currentAngle += sweepAngle
            }

            if (dataSet.holeRadiusRatio > 0f) {
                val layoutResult = textMeasurer.measure(
                    text = AnnotatedString("Total\n${totalValue.toInt()}"),
                    style = TextStyle(color = Color.White, fontSize = 14.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                )
                drawText(
                    textLayoutResult = layoutResult,
                    topLeft = Offset(center.x - layoutResult.size.width / 2f, center.y - layoutResult.size.height / 2f)
                )
            }

            // 绘制 MarkerView 触控框
            highlightedOffset?.let { offset ->
                highlightedEntry?.let { entry ->
                    val percent = (entry.value / totalValue * 100f).toInt()
                    drawMarkerView(
                        position = offset,
                        title = entry.label,
                        valueText = "${entry.value.toInt()} ($percent%)",
                        textMeasurer = textMeasurer,
                        badgeColor = entry.color
                    )
                }
            }
        }
    }
}
