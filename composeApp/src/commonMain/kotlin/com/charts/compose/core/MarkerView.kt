package com.charts.compose.core

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.sp

data class MarkerViewConfig(
    val backgroundColor: Color = Color(0xDD1E2638),
    val titleColor: Color = Color.LightGray,
    val valueColor: Color = Color.White,
    val titleFontSize: Float = 10f,
    val valueFontSize: Float = 12f,
    val padding: Float = 16f,
    val cornerRadius: Float = 10f,
    val badgeWidth: Float = 6f
)

@OptIn(ExperimentalTextApi::class)
fun DrawScope.drawMarkerView(
    position: Offset,
    title: String,
    valueText: String,
    textMeasurer: TextMeasurer,
    badgeColor: Color = Color(0xFF00ADB5),
    config: MarkerViewConfig = MarkerViewConfig()
) {
    val titleResult = textMeasurer.measure(
        text = AnnotatedString(title),
        style = TextStyle(color = config.titleColor, fontSize = config.titleFontSize.sp)
    )
    val valueResult = textMeasurer.measure(
        text = AnnotatedString(valueText),
        style = TextStyle(
            color = config.valueColor,
            fontSize = config.valueFontSize.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
    )

    val cardWidth = maxOf(titleResult.size.width, valueResult.size.width) + config.padding * 2f
    val cardHeight = titleResult.size.height + valueResult.size.height + 20f

    // 智能边界避让
    val rawLeft = position.x - cardWidth / 2f
    val cardLeft = rawLeft.coerceIn(20f, size.width - cardWidth - 20f)
    val cardTop = (position.y - cardHeight - 20f).coerceAtLeast(10f)

    // 1. 绘制背景与装饰
    drawRoundRect(
        color = config.backgroundColor,
        topLeft = Offset(cardLeft, cardTop),
        size = Size(cardWidth, cardHeight),
        cornerRadius = CornerRadius(config.cornerRadius, config.cornerRadius)
    )
    drawRoundRect(
        color = badgeColor,
        topLeft = Offset(cardLeft, cardTop),
        size = Size(config.badgeWidth, cardHeight),
        cornerRadius = CornerRadius(config.badgeWidth / 2f, config.badgeWidth / 2f)
    )

    // 2. 绘制文本
    drawText(
        textLayoutResult = titleResult,
        topLeft = Offset(cardLeft + config.padding, cardTop + 8f)
    )
    drawText(
        textLayoutResult = valueResult,
        topLeft = Offset(cardLeft + config.padding, cardTop + 10f + titleResult.size.height)
    )
}
