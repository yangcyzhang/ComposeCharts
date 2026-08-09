package com.charts.compose.core

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect

class ViewportHandler {
    var xMin: Float = 0f
    var xMax: Float = 1f
    var yMin: Float = 0f
    var yMax: Float = 1f

    var contentRect: Rect = Rect.Zero

    var scaleX by mutableFloatStateOf(1f)
    var scaleY by mutableFloatStateOf(1f)
    var translationX by mutableFloatStateOf(0f)
    var translationY by mutableFloatStateOf(0f)

    fun updateBounds(xMin: Float, xMax: Float, yMin: Float, yMax: Float) {
        this.xMin = xMin
        this.xMax = if (xMax == xMin) xMin + 1f else xMax
        this.yMin = yMin
        this.yMax = if (yMax == yMin) yMin + 1f else yMax
    }

    fun updateRect(rect: Rect) {
        this.contentRect = rect
    }

    // 边界平移约束：保证图表在任何放缩/拖拽倍率下都不会飞出 Viewport 内容区域
    fun constrainTranslation() {
        if (contentRect.width <= 0f) return
        val maxTx = 0f
        val minTx = contentRect.width * (1f - scaleX)
        translationX = translationX.coerceIn(minTx, maxTx)

        val maxTy = 0f
        val minTy = contentRect.height * (1f - scaleY)
        translationY = translationY.coerceIn(minTy, maxTy)
    }

    // 数据坐标 X -> Canvas 像素 X
    fun toPixelX(valueX: Float): Float {
        val width = contentRect.width
        if (width <= 0f) return 0f
        val normX = (valueX - xMin) / (xMax - xMin)
        return (contentRect.left + normX * width * scaleX) + translationX
    }

    // 数据坐标 Y -> Canvas 像素 Y (Y轴反转)
    fun toPixelY(valueY: Float): Float {
        val height = contentRect.height
        if (height <= 0f) return 0f
        val normY = (valueY - yMin) / (yMax - yMin)
        return (contentRect.bottom - normY * height * scaleY) + translationY
    }

    // Canvas 像素 X -> 数据坐标 X
    fun toValueX(pixelX: Float): Float {
        val width = contentRect.width
        if (width <= 0f) return 0f
        val normX = (pixelX - translationX - contentRect.left) / (width * scaleX)
        return xMin + normX * (xMax - xMin)
    }

    // Canvas 像素 Y -> 数据坐标 Y
    fun toValueY(pixelY: Float): Float {
        val height = contentRect.height
        if (height <= 0f) return 0f
        val normY = (contentRect.bottom - (pixelY - translationY)) / (height * scaleY)
        return yMin + normY * (yMax - yMin)
    }
}
