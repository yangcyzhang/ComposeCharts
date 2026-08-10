package com.charts.compose.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.charts.compose.core.*
import com.charts.compose.data.AnimationType
import com.charts.compose.data.Entry
import com.charts.compose.data.LineDataSet
import kotlin.math.abs

@OptIn(ExperimentalTextApi::class)
@Composable
fun LineChart(
    dataSets: List<LineDataSet>,
    modifier: Modifier = Modifier,
    xAxisConfig: AxisConfig = AxisConfig(),
    yAxisConfig: AxisConfig = AxisConfig(),
    markerConfig: MarkerViewConfig = MarkerViewConfig(),
    customMarkerView: (DrawScope.(position: Offset, dataset: LineDataSet, entry: Entry) -> Unit)? = null
) {
    val textMeasurer = rememberTextMeasurer()
    val viewport = remember { ViewportHandler() }

    var highlightedPoint by remember { mutableStateOf<Pair<LineDataSet, Entry>?>(null) }
    var highlightedOffset by remember { mutableStateOf<Offset?>(null) }

    // 计算全局极值包围盒
    val allEntries = dataSets.flatMap { it.entries }
    val xMin = allEntries.minOfOrNull { it.x } ?: 0f
    val xMax = allEntries.maxOfOrNull { it.x } ?: 1f
    val yMin = (allEntries.minOfOrNull { it.y } ?: 0f).coerceAtMost(0f)
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
                viewport.scaleY = (viewport.scaleY * zoom).coerceIn(1f, 8f)

                viewport.translationX = (viewport.translationX - centroid.x) * scaleFactor + centroid.x + pan.x
                viewport.translationY = (viewport.translationY - centroid.y) * scaleFactor + centroid.y + pan.y
                
                // 约束平移：确保图表放缩平移不会跑出屏幕之外
                viewport.constrainTranslation()
            }
            .pointerInput(dataSets) {
                detectTapGestures { tapOffset ->
                    var minDistance = Float.MAX_VALUE
                    var closest: Pair<LineDataSet, Entry>? = null
                    var closestPt: Offset? = null

                    dataSets.forEach { ds ->
                        if (!ds.isVisible) return@forEach
                        ds.entries.forEach { entry ->
                            val px = viewport.toPixelX(entry.x)
                            val py = viewport.toPixelY(entry.y * ds.animateProgress)
                            val dist = abs(px - tapOffset.x)
                            if (dist < 40f && dist < minDistance) {
                                minDistance = dist
                                closest = Pair(ds, entry)
                                closestPt = Offset(px, py)
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

            // 保持平移约束生效
            viewport.constrainTranslation()

            // 1. 绘制网格与坐标轴
            drawAxisAndGrid(
                viewport = viewport,
                xAxisConfig = xAxisConfig,
                yAxisConfig = yAxisConfig,
                textMeasurer = textMeasurer
            )

            // 2. 绘制多组折线与平滑渐变填充
            dataSets.forEach { dataSet ->
                if (!dataSet.isVisible || dataSet.entries.isEmpty()) return@forEach

                val linePath = Path()
                val fillPath = Path()
                val pixelPoints = mutableListOf<Offset>()

                dataSet.entries.forEachIndexed { index, entry ->
                    val animatedY = if (dataSet.animationType == AnimationType.VERTICAL) {
                        entry.y * dataSet.animateProgress
                    } else {
                        entry.y
                    }
                    
                    val px = viewport.toPixelX(entry.x)
                    val py = viewport.toPixelY(animatedY)
                    val pt = Offset(px, py)
                    pixelPoints.add(pt)

                    if (index == 0) {
                        linePath.moveTo(px, py)
                        fillPath.moveTo(px, viewport.contentRect.bottom)
                        fillPath.lineTo(px, py)
                    } else {
                        val prevPt = pixelPoints[index - 1]
                        if (dataSet.isCubic) {
                            val cx1 = (prevPt.x + pt.x) / 2f
                            val cy1 = prevPt.y
                            val cx2 = (prevPt.x + pt.x) / 2f
                            val cy2 = pt.y
                            linePath.cubicTo(cx1, cy1, cx2, cy2, pt.x, pt.y)
                            fillPath.cubicTo(cx1, cy1, cx2, cy2, pt.x, pt.y)
                        } else {
                            linePath.lineTo(pt.x, pt.y)
                            fillPath.lineTo(pt.x, pt.y)
                        }
                    }

                    if (index == dataSet.entries.lastIndex) {
                        fillPath.lineTo(pt.x, viewport.contentRect.bottom)
                        fillPath.close()
                    }
                }

                // 计算裁剪区域（用于 HORIZONTAL 动画）
                val chartAlpha = if (dataSet.animationType == AnimationType.REVEAL) dataSet.animateProgress else 1f
                
                drawContext.canvas.save()
                if (dataSet.animationType == AnimationType.HORIZONTAL) {
                    val clipWidth = viewport.contentRect.width * dataSet.animateProgress
                    drawContext.canvas.clipRect(
                        left = viewport.contentRect.left,
                        top = viewport.contentRect.top,
                        right = viewport.contentRect.left + clipWidth,
                        bottom = viewport.contentRect.bottom,
                        clipOp = ClipOp.Intersect
                    )
                }

                // 绘制渐变色填充
                if (dataSet.drawFilled) {
                    val brush = dataSet.fillBrush ?: Brush.verticalGradient(
                        colors = listOf(dataSet.color.copy(alpha = 0.4f * chartAlpha), Color.Transparent),
                        startY = viewport.contentRect.top,
                        endY = viewport.contentRect.bottom
                    )
                    drawPath(fillPath, brush = brush, alpha = chartAlpha)
                }

                // 绘制折线
                drawPath(
                    path = linePath,
                    color = dataSet.color,
                    style = Stroke(width = dataSet.lineWidth, cap = StrokeCap.Round, join = StrokeJoin.Round),
                    alpha = chartAlpha
                )

                // 绘制数据圆点与数值 Label
                pixelPoints.forEachIndexed { idx, pt ->
                    if (pt.x in viewport.contentRect.left..viewport.contentRect.right) {
                        if (dataSet.drawCircles) {
                            drawCircle(color = Color.White.copy(alpha = chartAlpha), radius = dataSet.circleRadius + 2f, center = pt)
                            drawCircle(color = dataSet.color, radius = dataSet.circleRadius, center = pt, alpha = chartAlpha)
                        }

                        if (dataSet.drawValues) {
                            val entry = dataSet.entries[idx]
                            val labelResult = textMeasurer.measure(
                                text = AnnotatedString(entry.y.toInt().toString()),
                                style = TextStyle(color = Color.White.copy(alpha = chartAlpha), fontSize = 10.sp)
                            )
                            drawText(
                                textLayoutResult = labelResult,
                                topLeft = Offset(pt.x - labelResult.size.width / 2f, pt.y - labelResult.size.height - 8f),
                                alpha = chartAlpha
                            )
                        }
                    }
                }
                drawContext.canvas.restore()
            }

            // 3. 绘制底层图例 (Legend)
            drawLegend(dataSets = dataSets, textMeasurer = textMeasurer)

            // 4. 绘制点选 MarkerView 弹窗与高亮准星线
            highlightedOffset?.let { offset ->
                highlightedPoint?.let { (ds, entry) ->
                    if (offset.x in viewport.contentRect.left..viewport.contentRect.right) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.6f),
                            start = Offset(offset.x, viewport.contentRect.top),
                            end = Offset(offset.x, viewport.contentRect.bottom),
                            strokeWidth = 1.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )

                        if (customMarkerView != null) {
                            customMarkerView(offset, ds, entry)
                        } else {
                            drawMarkerView(
                                position = offset,
                                title = ds.label,
                                valueText = "X: ${entry.x.toInt()}, Y: ${entry.y}",
                                textMeasurer = textMeasurer,
                                badgeColor = ds.color,
                                config = markerConfig
                            )
                        }
                    }
                }
            }
        }
    }
}
