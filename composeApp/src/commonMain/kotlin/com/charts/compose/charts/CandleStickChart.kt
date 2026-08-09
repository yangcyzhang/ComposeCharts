package com.charts.compose.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.charts.compose.core.*
import com.charts.compose.data.CandleDataSet
import com.charts.compose.data.CandleEntry
import kotlin.math.abs

@OptIn(ExperimentalTextApi::class)
@Composable
fun CandleStickChart(
    dataSet: CandleDataSet,
    modifier: Modifier = Modifier,
    xAxisConfig: AxisConfig = AxisConfig(),
    yAxisConfig: AxisConfig = AxisConfig()
) {
    val textMeasurer = rememberTextMeasurer()
    val viewport = remember { ViewportHandler() }

    var highlightedEntry by remember { mutableStateOf<CandleEntry?>(null) }
    var highlightedOffset by remember { mutableStateOf<Offset?>(null) }

    val entries = dataSet.entries
    val xMin = (entries.minOfOrNull { it.x } ?: 0f) - 0.5f
    val xMax = (entries.maxOfOrNull { it.x } ?: 1f) + 0.5f
    val yMin = (entries.minOfOrNull { it.low } ?: 0f) * 0.98f
    val yMax = (entries.maxOfOrNull { it.high } ?: 10f) * 1.02f

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
            .pointerInput(dataSet) {
                detectTapGestures { tapOffset ->
                    val candleWidth = 24f * viewport.scaleX
                    var closest: CandleEntry? = null
                    var closestPt: Offset? = null

                    dataSet.entries.forEach { candle ->
                        val px = viewport.toPixelX(candle.x)
                        val left = px - candleWidth / 2f
                        val right = px + candleWidth / 2f
                        if (tapOffset.x in left..right) {
                            val highY = viewport.toPixelY(candle.high)
                            closest = candle
                            closestPt = Offset(px, highY)
                        }
                    }

                    highlightedEntry = closest
                    highlightedOffset = closestPt
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

            val candleWidth = 24f * viewport.scaleX

            entries.forEach { candle ->
                val px = viewport.toPixelX(candle.x)

                if (px in (viewport.contentRect.left - candleWidth)..(viewport.contentRect.right + candleWidth)) {
                    val highY = viewport.toPixelY(candle.high * dataSet.animateProgress)
                    val lowY = viewport.toPixelY(candle.low * dataSet.animateProgress)
                    val openY = viewport.toPixelY(candle.open * dataSet.animateProgress)
                    val closeY = viewport.toPixelY(candle.close * dataSet.animateProgress)

                    val isIncreasing = candle.close >= candle.open
                    val color = if (isIncreasing) dataSet.increasingColor else dataSet.decreasingColor

                    // 1. 绘制高低影线 (High/Low Shadow Line)
                    drawLine(
                        color = color,
                        start = Offset(px, highY),
                        end = Offset(px, lowY),
                        strokeWidth = dataSet.shadowWidth
                    )

                    // 2. 绘制开盘价/收盘价实体柱 (Candle Body)
                    val topY = minOf(openY, closeY)
                    val bodyHeight = abs(openY - closeY).coerceAtLeast(2f)

                    drawRect(
                        color = color,
                        topLeft = Offset(px - candleWidth / 2f, topY),
                        size = Size(candleWidth, bodyHeight)
                    )
                }
            }

            // 绘制图例
            drawLegend(dataSets = listOf(dataSet), textMeasurer = textMeasurer)

            // 绘制 MarkerView 触控框
            highlightedOffset?.let { offset ->
                highlightedEntry?.let { candle ->
                    drawMarkerView(
                        position = offset,
                        title = dataSet.label,
                        valueText = "O:${candle.open.toInt()} C:${candle.close.toInt()} H:${candle.high.toInt()} L:${candle.low.toInt()}",
                        textMeasurer = textMeasurer,
                        badgeColor = if (candle.close >= candle.open) dataSet.increasingColor else dataSet.decreasingColor
                    )
                }
            }
        }
    }
}
