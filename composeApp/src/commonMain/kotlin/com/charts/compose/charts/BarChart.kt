package com.charts.compose.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.charts.compose.core.*
import com.charts.compose.data.BarDataSet
import com.charts.compose.data.BarEntry

@OptIn(ExperimentalTextApi::class)
@Composable
fun BarChart(
    dataSets: List<BarDataSet>,
    modifier: Modifier = Modifier,
    xAxisConfig: AxisConfig = AxisConfig(),
    yAxisConfig: AxisConfig = AxisConfig()
) {
    val textMeasurer = rememberTextMeasurer()
    val viewport = remember { ViewportHandler() }

    var highlightedPoint by remember { mutableStateOf<Pair<BarDataSet, BarEntry>?>(null) }
    var highlightedOffset by remember { mutableStateOf<Offset?>(null) }

    val allEntries = dataSets.flatMap { it.entries }
    val xMin = (allEntries.minOfOrNull { it.x } ?: 0f) - 0.5f
    val xMax = (allEntries.maxOfOrNull { it.x } ?: 1f) + 0.5f
    val yMin = 0f
    val yMax = (allEntries.maxOfOrNull { it.y } ?: 10f) * 1.15f

    viewport.updateBounds(xMin, xMax, yMin, yMax)

    Box(
        modifier = modifier
            .fillMaxSize()
            .chartTransformGesture { zoom, pan, centroid ->
                val oldScale = viewport.scaleX
                val newScale = (oldScale * zoom).coerceIn(1f, 8f)
                val scaleFactor = newScale / oldScale

                viewport.scaleX = newScale
                viewport.translationX = (viewport.translationX - centroid.x) * scaleFactor + centroid.x + pan.x
                
                viewport.constrainTranslation()
            }
            .pointerInput(dataSets) {
                detectTapGestures { tapOffset ->
                    var closest: Pair<BarDataSet, BarEntry>? = null
                    var closestPt: Offset? = null

                    val barWidth = 35f
                    val dataSetCount = dataSets.size

                    dataSets.forEachIndexed { setIndex, ds ->
                        if (!ds.isVisible) return@forEachIndexed
                        ds.entries.forEach { entry ->
                            val baseX = viewport.toPixelX(entry.x)
                            val offsetShift = (setIndex - (dataSetCount - 1) / 2f) * (barWidth + 6f)
                            val left = baseX + offsetShift - barWidth / 2f
                            val right = left + barWidth

                            if (tapOffset.x in left..right) {
                                val topY = viewport.toPixelY(entry.y * ds.animateProgress)
                                closest = Pair(ds, entry)
                                closestPt = Offset((left + right) / 2f, topY)
                            }
                        }
                    }

                    if (closest != null) {
                        highlightedPoint = closest
                        highlightedOffset = closestPt
                    } else {
                        highlightedPoint = null
                        highlightedOffset = null
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val paddingLeft = 100f
            val paddingBottom = 90f
            val paddingTop = 40f
            val paddingRight = 40f

            viewport.updateRect(
                Rect(
                    left = paddingLeft,
                    top = paddingTop,
                    right = size.width - paddingRight,
                    bottom = size.height - paddingBottom
                )
            )

            viewport.constrainTranslation()

            drawAxisAndGrid(
                viewport = viewport,
                xAxisConfig = xAxisConfig,
                yAxisConfig = yAxisConfig,
                textMeasurer = textMeasurer
            )

            val dataSetCount = dataSets.size
            val barWidth = 35f

            dataSets.forEachIndexed { setIndex, dataSet ->
                if (!dataSet.isVisible) return@forEachIndexed

                dataSet.entries.forEach { entry ->
                    val baseX = viewport.toPixelX(entry.x)
                    val offsetShift = (setIndex - (dataSetCount - 1) / 2f) * (barWidth + 6f)
                    val left = baseX + offsetShift - barWidth / 2f

                    if (left in (viewport.contentRect.left - barWidth)..(viewport.contentRect.right)) {
                        var runningY = 0f
                        val colorsList = if (dataSet.colors.isNotEmpty()) dataSet.colors else listOf(dataSet.color)

                        entry.yValues.forEachIndexed { valIndex, valY ->
                            val bottomY = viewport.toPixelY(runningY * dataSet.animateProgress)
                            runningY += valY
                            val topY = viewport.toPixelY(runningY * dataSet.animateProgress)
                            val barHeight = bottomY - topY
                            val color = colorsList[valIndex % colorsList.size]

                            drawRoundRect(
                                color = color,
                                topLeft = Offset(left, topY),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(4f, 4f)
                            )
                        }

                        // 绘制数值 Value Text Label，精准紧贴柱体顶端上方 4px 位置
                        if (dataSet.drawValues) {
                            val totalTopY = viewport.toPixelY(runningY * dataSet.animateProgress)
                            val labelResult = textMeasurer.measure(
                                text = AnnotatedString(entry.y.toInt().toString()),
                                style = TextStyle(color = Color.White, fontSize = 11.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                            )
                            drawText(
                                textLayoutResult = labelResult,
                                topLeft = Offset(left + barWidth / 2f - labelResult.size.width / 2f, totalTopY - labelResult.size.height - 4f)
                            )
                        }
                    }
                }
            }

            // 绘制图例
            drawLegend(dataSets = dataSets, textMeasurer = textMeasurer)

            // 绘制 MarkerView 触控框
            highlightedOffset?.let { offset ->
                highlightedPoint?.let { (ds, entry) ->
                    drawMarkerView(
                        position = offset,
                        title = ds.label,
                        valueText = "Total Y: ${entry.y.toInt()}",
                        textMeasurer = textMeasurer,
                        badgeColor = ds.color
                    )
                }
            }
        }
    }
}
