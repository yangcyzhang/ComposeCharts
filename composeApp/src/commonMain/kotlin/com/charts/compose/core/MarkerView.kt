package com.charts.compose.core

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalTextApi::class)
fun DrawScope.drawMarkerView(
    position: Offset,
    title: String,
    valueText: String,
    textMeasurer: TextMeasurer,
    badgeColor: Color = Color(0xFF00ADB5)
) {
    val titleResult = textMeasurer.measure(
        text = AnnotatedString(title),
        style = TextStyle(color = Color.LightGray, fontSize = 10.sp)
    )
    val valueResult = textMeasurer.measure(
        text = AnnotatedString(valueText),
        style = TextStyle(color = Color.White, fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
    )

    val cardWidth = maxOf(titleResult.size.width, valueResult.size.width) + 32f
    val cardHeight = titleResult.size.height + valueResult.size.height + 20f

    // 智能边界避让：计算 Card 卡片在屏幕之内的坐标
    val rawLeft = position.x - cardWidth / 2f
    val cardLeft = rawLeft.coerceIn(20f, size.width - cardWidth - 20f)
    val cardTop = (position.y - cardHeight - 20f).coerceAtLeast(10f)

    // 1. 绘制 MarkerView 阴影与玻璃态背景框
    drawRoundRect(
        color = Color(0xDD1E2638),
        topLeft = Offset(cardLeft, cardTop),
        size = Size(cardWidth, cardHeight),
        cornerRadius = CornerRadius(10f, 10f)
    )
    drawRoundRect(
        color = badgeColor,
        topLeft = Offset(cardLeft, cardTop),
        size = Size(6f, cardHeight),
        cornerRadius = CornerRadius(3f, 3f)
    )

    // 2. 绘制标题与具体数值
    drawText(
        textLayoutResult = titleResult,
        topLeft = Offset(cardLeft + 16f, cardTop + 8f)
    )
    drawText(
        textLayoutResult = valueResult,
        topLeft = Offset(cardLeft + 16f, cardTop + 10f + titleResult.size.height)
    )
}
