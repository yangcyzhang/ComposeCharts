package com.charts.compose.core

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.sp
import com.charts.compose.data.DataSet

@OptIn(ExperimentalTextApi::class)
fun DrawScope.drawLegend(
    dataSets: List<DataSet<*>>,
    textMeasurer: TextMeasurer,
    textColor: Color = Color.LightGray
) {
    if (dataSets.isEmpty()) return

    val itemSpacing = 32f
    val boxSize = 16f
    var startX = 60f
    val startY = size.height - 30f

    dataSets.forEach { dataSet ->
        if (!dataSet.isVisible) return@forEach

        // 1. 绘制图例颜色块 (Color Box)
        drawRoundRect(
            color = dataSet.color,
            topLeft = Offset(startX, startY - boxSize / 2f),
            size = Size(boxSize, boxSize),
            cornerRadius = CornerRadius(3f, 3f)
        )

        // 2. 绘制数据集名称 (DataSet Label)
        val layoutResult = textMeasurer.measure(
            text = AnnotatedString(dataSet.label),
            style = TextStyle(color = textColor, fontSize = 11.sp)
        )
        drawText(
            textLayoutResult = layoutResult,
            topLeft = Offset(startX + boxSize + 10f, startY - layoutResult.size.height / 2f)
        )

        startX += boxSize + 10f + layoutResult.size.width + itemSpacing
    }
}
