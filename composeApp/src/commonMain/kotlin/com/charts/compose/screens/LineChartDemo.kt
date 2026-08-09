package com.charts.compose.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.charts.compose.charts.LineChart
import com.charts.compose.core.edgeSwipeToBack
import com.charts.compose.data.Entry
import com.charts.compose.data.LineDataSet
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LineChartDemo(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var seed by remember { mutableStateOf(0) }
    var drawValuesState by remember { mutableStateOf(false) }
    var drawFilledState by remember { mutableStateOf(true) }
    val animProgress = remember { Animatable(1f) }

    val entries1 = remember(seed) {
        (0..6).map { i -> Entry(i.toFloat(), Random(seed + i * 3).nextInt(15, 85).toFloat()) }
    }
    val entries2 = remember(seed) {
        (0..6).map { i -> Entry(i.toFloat(), Random(seed + i * 7 + 1).nextInt(10, 90).toFloat()) }
    }

    val dataSet1 = remember(entries1, drawValuesState, drawFilledState, animProgress.value) {
        LineDataSet("Pro Growth", entries1).apply {
            color = Color(0xFF00ADB5)
            drawValues = drawValuesState
            drawFilled = drawFilledState
            animateProgress = animProgress.value
        }
    }
    val dataSet2 = remember(entries2, drawValuesState, drawFilledState, animProgress.value) {
        LineDataSet("Lite Growth", entries2).apply {
            color = Color(0xFFE854FF)
            drawValues = drawValuesState
            drawFilled = drawFilledState
            animateProgress = animProgress.value
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("多折线平滑走势图 (LineChart)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF070B15))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF070B15))
                .edgeSwipeToBack(onBack)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = { seed += 1 },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00ADB5), contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("🎲 随机数据", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { drawValuesState = !drawValuesState },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2838), contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(if (drawValuesState) "👁️ 隐藏数值" else "👁️ 显示数值", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { drawFilledState = !drawFilledState },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2838), contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(if (drawFilledState) "🎨 关渐变" else "🎨 开渐变", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        scope.launch {
                            animProgress.snapTo(0f)
                            animProgress.animateTo(1f, animationSpec = tween(1000))
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0), contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("⚡ 入场动画", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("💡 交互提示：捏合放缩已锁定边界，双指移动拉扯不会飞出屏幕。点击点位可弹窗查看 MarkerView。", fontSize = 11.sp, color = Color.LightGray.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(16.dp))

            LineChart(
                dataSets = listOf(dataSet1, dataSet2),
                modifier = Modifier.fillMaxWidth().height(420.dp)
            )
        }
    }
}
