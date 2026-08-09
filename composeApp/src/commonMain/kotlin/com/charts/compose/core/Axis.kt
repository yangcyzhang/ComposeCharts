package com.charts.compose.core

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

// 轴线与刻度配置
data class AxisConfig(
    val isEnabled: Boolean = true,
    val showGridLines: Boolean = true,
    val gridColor: Color = Color(0x1FFFFFFF),
    val textColor: Color = Color.LightGray,
    val labelCount: Int = 5,
    val isDashedGrid: Boolean = true
)

@OptIn(ExperimentalTextApi::class)
fun DrawScope.drawAxisAndGrid(
    viewport: ViewportHandler,
    xAxisConfig: AxisConfig = AxisConfig(),
    yAxisConfig: AxisConfig = AxisConfig(),
    textMeasurer: TextMeasurer,
    xLabelFormatter: (Float) -> String = { it.roundToInt().toString() },
    yLabelFormatter: (Float) -> String = { ((it * 10f).roundToInt() / 10f).toString() }
) {
    val rect = viewport.contentRect

    // 1. 绘制 Y 轴水平网格线与 Y 轴刻度标签
    if (yAxisConfig.isEnabled) {
        val count = yAxisConfig.labelCount.coerceAtLeast(2)
        val step = (viewport.yMax - viewport.yMin) / (count - 1)
        val pathEffect = if (yAxisConfig.isDashedGrid) PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f) else null

        for (i in 0 until count) {
            val valY = viewport.yMin + i * step
            val py = viewport.toPixelY(valY)

            if (py in rect.top..rect.bottom) {
                // 水平网格线
                if (yAxisConfig.showGridLines) {
                    drawLine(
                        color = yAxisConfig.gridColor,
                        start = Offset(rect.left, py),
                        end = Offset(rect.right, py),
                        strokeWidth = 1f,
                        pathEffect = pathEffect
                    )
                }

                // Y 轴文字标签
                val label = yLabelFormatter(valY)
                val layoutResult = textMeasurer.measure(
                    text = AnnotatedString(label),
                    style = TextStyle(color = yAxisConfig.textColor, fontSize = 10.sp)
                )
                drawText(
                    textLayoutResult = layoutResult,
                    topLeft = Offset(rect.left - layoutResult.size.width - 12f, py - layoutResult.size.height / 2f)
                )
            }
        }
    }

    // 2. 绘制 X 轴垂直网格线与 X 轴刻度标签
    if (xAxisConfig.isEnabled) {
        val count = xAxisConfig.labelCount.coerceAtLeast(2)
        val step = (viewport.xMax - viewport.xMin) / (count - 1)

        for (i in 0 until count) {
            val valX = viewport.xMin + i * step
            val px = viewport.toPixelX(valX)

            if (px in rect.left..rect.right) {
                // X 轴文字标签
                val label = xLabelFormatter(valX)
                val layoutResult = textMeasurer.measure(
                    text = AnnotatedString(label),
                    style = TextStyle(color = xAxisConfig.textColor, fontSize = 10.sp)
                )
                drawText(
                    textLayoutResult = layoutResult,
                    topLeft = Offset(px - layoutResult.size.width / 2f, rect.bottom + 8f)
                )
            }
        }
    }
}
