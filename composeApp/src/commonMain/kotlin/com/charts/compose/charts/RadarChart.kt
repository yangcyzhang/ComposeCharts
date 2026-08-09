package com.charts.compose.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.charts.compose.core.*
import com.charts.compose.data.RadarDataSet
import com.charts.compose.data.RadarEntry
import kotlin.math.*

@OptIn(ExperimentalTextApi::class)
@Composable
fun RadarChart(
    dataSets: List<RadarDataSet>,
    modifier: Modifier = Modifier,
    webColor: Color = Color(0x2FFFFFF)
) {
    val textMeasurer = rememberTextMeasurer()
    var highlightedEntry by remember { mutableStateOf<Pair<RadarDataSet, RadarEntry>?>(null) }
    var highlightedOffset by remember { mutableStateOf<Offset?>(null) }

    val firstSet = dataSets.firstOrNull() ?: return
    val count = firstSet.entries.size
    if (count < 3) return

    val maxValue = remember(dataSets) {
        dataSets.flatMap { it.entries }.maxOfOrNull { it.value } ?: 100f
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(dataSets) {
                detectTapGestures { tapOffset ->
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = min(size.width, size.height) / 2.6f
                    val angleStep = (2 * PI / count).toFloat()

                    var closest: Pair<RadarDataSet, RadarEntry>? = null
                    var closestPt: Offset? = null
                    var minDistance = Float.MAX_VALUE

                    dataSets.forEach { ds ->
                        if (!ds.isVisible) return@forEach
                        ds.entries.forEachIndexed { i, entry ->
                            val angle = i * angleStep - (PI / 2f).toFloat()
                            val valRadius = radius * (entry.value / maxValue).coerceIn(0f, 1f)
                            val px = center.x + cos(angle) * valRadius
                            val py = center.y + sin(angle) * valRadius
                            val pt = Offset(px, py)
                            val dist = (pt - tapOffset).getDistance()

                            if (dist < 45f && dist < minDistance) {
                                minDistance = dist
                                closest = Pair(ds, entry)
                                closestPt = pt
                            }
                        }
                    }

                    highlightedEntry = closest
                    highlightedOffset = closestPt
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = min(size.width, size.height) / 2.6f
            val angleStep = (2 * PI / count).toFloat()

            // 1. 绘制蜘蛛网多边形底纹 (Spider Web)
            val webRings = 4
            for (ring in 1..webRings) {
                val ringRadius = radius * (ring / webRings.toFloat())
                val webPath = Path()

                for (i in 0 until count) {
                    val angle = i * angleStep - (PI / 2f).toFloat()
                    val px = center.x + cos(angle) * ringRadius
                    val py = center.y + sin(angle) * ringRadius

                    if (i == 0) webPath.moveTo(px, py) else webPath.lineTo(px, py)

                    if (ring == webRings) {
                        drawLine(
                            color = webColor,
                            start = center,
                            end = Offset(px, py),
                            strokeWidth = 1f
                        )

                        val label = firstSet.entries[i].label
                        val labelRadius = radius + 24f
                        val lx = center.x + cos(angle) * labelRadius
                        val ly = center.y + sin(angle) * labelRadius
                        val layoutResult = textMeasurer.measure(
                            text = AnnotatedString(label),
                            style = TextStyle(color = Color.White, fontSize = 11.sp)
                        )
                        drawText(
                            textLayoutResult = layoutResult,
                            topLeft = Offset(lx - layoutResult.size.width / 2f, ly - layoutResult.size.height / 2f)
                        )
                    }
                }
                webPath.close()
                drawPath(webPath, color = webColor, style = Stroke(width = 1f))
            }

            // 2. 绘制多组雷达图数据多边形
            dataSets.forEach { dataSet ->
                if (!dataSet.isVisible || dataSet.entries.size != count) return@forEach

                val polyPath = Path()
                dataSet.entries.forEachIndexed { i, entry ->
                    val angle = i * angleStep - (PI / 2f).toFloat()
                    val valRadius = radius * (entry.value / maxValue).coerceIn(0f, 1f)
                    val px = center.x + cos(angle) * valRadius
                    val py = center.y + sin(angle) * valRadius

                    if (i == 0) polyPath.moveTo(px, py) else polyPath.lineTo(px, py)
                }
                polyPath.close()

                drawPath(polyPath, color = dataSet.color.copy(alpha = dataSet.fillAlpha))
                drawPath(polyPath, color = dataSet.color, style = Stroke(width = dataSet.lineWidth))
            }

            // 3. 绘制图例
            drawLegend(dataSets = dataSets, textMeasurer = textMeasurer)

            // 4. 绘制 MarkerView 触控框
            highlightedOffset?.let { offset ->
                highlightedEntry?.let { (ds, entry) ->
                    drawMarkerView(
                        position = offset,
                        title = "${ds.label} - ${entry.label}",
                        valueText = "Score: ${entry.value.toInt()}",
                        textMeasurer = textMeasurer,
                        badgeColor = ds.color
                    )
                }
            }
        }
    }
}
