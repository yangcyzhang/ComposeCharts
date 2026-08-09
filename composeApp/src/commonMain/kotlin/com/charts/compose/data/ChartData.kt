package com.charts.compose.data

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// --- 基础数据点定义 ---
open class Entry(
    open val x: Float,
    open val y: Float,
    val data: Any? = null
)

// 柱状图数据点（支持单柱与多重堆叠柱）
class BarEntry(
    override val x: Float,
    val yValues: FloatArray,
    val label: String = ""
) : Entry(x, yValues.sum()) {
    constructor(x: Float, y: Float, label: String = "") : this(x, floatArrayOf(y), label)
}

// 饼图数据点
data class PieEntry(
    val value: Float,
    val label: String,
    val color: Color = Color.Unspecified
)

// K线图/蜡烛图数据点
data class CandleEntry(
    val x: Float,
    val high: Float,
    val low: Float,
    val open: Float,
    val close: Float
)

// 雷达图数据点
data class RadarEntry(
    val value: Float,
    val label: String
)

// --- 数据集（DataSet）定义 ---
abstract class DataSet<T>(
    val label: String,
    val entries: List<T>
) {
    var isVisible: Boolean = true
    var color: Color = Color(0xFF00ADB5)
    var colors: List<Color> = emptyList()
    var drawValues: Boolean = false
    var animateProgress: Float = 1f
}

// 折线图数据集
class LineDataSet(
    label: String,
    entries: List<Entry>
) : DataSet<Entry>(label, entries) {
    var lineWidth: Float = 3f
    var circleRadius: Float = 5f
    var drawCircles: Boolean = true
    var isCubic: Boolean = true // 是否贝塞尔平滑
    var fillBrush: Brush? = null // 渐变色块填充
    var drawFilled: Boolean = true
    var circleColor: Color = Color(0xFF00ADB5)
}

// 柱状图数据集
class BarDataSet(
    label: String,
    entries: List<BarEntry>
) : DataSet<BarEntry>(label, entries) {
    var barBorderWidth: Float = 0f
    var barBorderColor: Color = Color.Transparent
    var stackLabels: List<String> = emptyList()
}

// 饼图数据集
class PieDataSet(
    label: String,
    entries: List<PieEntry>
) : DataSet<PieEntry>(label, entries) {
    var sliceSpace: Float = 0f // 扇区边缘缝隙，默认为 0 无空隙
    var selectionShift: Float = 12f // 选中抽离偏移距离
    var holeRadiusRatio: Float = 0.5f // 环形内孔比例
}

// K线图数据集
class CandleDataSet(
    label: String,
    entries: List<CandleEntry>
) : DataSet<CandleEntry>(label, entries) {
    var shadowWidth: Float = 1.5f // 影线粗细
    var increasingColor: Color = Color(0xFF00C853) // 涨（绿/红可调）
    var decreasingColor: Color = Color(0xFFFF3D00) // 跌
    var neutralColor: Color = Color.Gray
}

// 雷达图数据集
class RadarDataSet(
    label: String,
    entries: List<RadarEntry>
) : DataSet<RadarEntry>(label, entries) {
    var lineWidth: Float = 2.5f
    var fillAlpha: Float = 0.3f
}
