package com.charts.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.charts.compose.screens.*

@Composable
fun App() {
    var currentScreen by remember { mutableStateOf(0) } // 0: Home, 1: Line, 2: Bar, 3: Pie, 4: Candle, 5: Radar

    MaterialTheme(colorScheme = darkColorScheme(background = Color(0xFF070B15))) {
        if (currentScreen != 0) {
            LocalBackHandler(enabled = true) {
                currentScreen = 0
            }
        }

        when (currentScreen) {
            0 -> ChartDashboardScreen(onSelectScreen = { currentScreen = it })
            1 -> LineChartDemo(onBack = { currentScreen = 0 })
            2 -> BarChartDemo(onBack = { currentScreen = 0 })
            3 -> PieChartDemo(onBack = { currentScreen = 0 })
            4 -> CandleStickDemo(onBack = { currentScreen = 0 })
            5 -> RadarDemo(onBack = { currentScreen = 0 })
        }
    }
}

@Composable
fun ChartDashboardScreen(onSelectScreen: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070B15))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "ComposeCharts",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF00ADB5)
        )
        Text(
            text = "MPAndroidChart 全功能 Compose 高性能重构",
            fontSize = 13.sp,
            color = Color.LightGray.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        ChartMenuCard(
            title = "多折线平滑走势图 (LineChart)",
            subtitle = "Smooth Bezier, Gradient Fills & Gestures",
            description = "支持贝塞尔曲线拟合、渐变面积填充、双坐标轴、双指手势缩放平移及数据准星点高亮。",
            icon = Icons.Default.ShowChart,
            gradientColors = listOf(Color(0xFF00F2FE), Color(0xFF4FACFE)),
            onClick = { onSelectScreen(1) }
        )

        ChartMenuCard(
            title = "多维柱状图 (BarChart)",
            subtitle = "Grouped & Multi-Stacked Bar Charts",
            description = "支持单柱、分组对比柱状图、多重堆叠柱，圆角柱体绘制与边框定制。",
            icon = Icons.Default.BarChart,
            gradientColors = listOf(Color(0xFFF9D423), Color(0xFFFF4E50)),
            onClick = { onSelectScreen(2) }
        )

        ChartMenuCard(
            title = "环形/饼图 (PieChart)",
            subtitle = "Donut Hole & Selection Shift",
            description = "支持实心/Donut 环形饼图，点击扇区可平滑触发 Selection Shift 抽离动画与缝隙间隔。",
            icon = Icons.Default.PieChart,
            gradientColors = listOf(Color(0xFFB188FF), Color(0xFFE854FF)),
            onClick = { onSelectScreen(3) }
        )

        ChartMenuCard(
            title = "K线/蜡烛图 (CandleStickChart)",
            subtitle = "Stock Trading High/Low Shadows",
            description = "金融专业 K 线图，高低影线与开收盘实体柱精准绘制，大批量数据平移缩放无延迟。",
            icon = Icons.Default.CandlestickChart,
            gradientColors = listOf(Color(0xFF38F9D7), Color(0xFF43E97B)),
            onClick = { onSelectScreen(4) }
        )

        ChartMenuCard(
            title = "蜘蛛网雷达图 (RadarChart)",
            subtitle = "Polar Web Polygons & Attributes",
            description = "极坐标多边形蛛网底纹与多数据集能力对比重叠绘制，精准计算角度与维度刻度。",
            icon = Icons.Default.Radar,
            gradientColors = listOf(Color(0xFFFF9A9E), Color(0xFFFECFEF)),
            onClick = { onSelectScreen(5) }
        )
        
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun ChartMenuCard(
    title: String,
    subtitle: String,
    description: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0x0FFFFFFF), Color(0x03FFFFFF))
                )
            )
            .border(
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x1AFFFFFF)),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.radialGradient(colors = gradientColors)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF00ADB5),
                    modifier = Modifier.padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = Color.LightGray.copy(alpha = 0.8f),
                    lineHeight = 14.sp
                )
            }
        }
    }
}
