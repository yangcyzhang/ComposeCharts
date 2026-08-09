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
import com.charts.compose.charts.BarChart
import com.charts.compose.core.edgeSwipeToBack
import com.charts.compose.data.BarDataSet
import com.charts.compose.data.BarEntry
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarChartDemo(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var seed by remember { mutableStateOf(0) }
    var drawValuesState by remember { mutableStateOf(false) }
    val animProgress = remember { Animatable(1f) }

    val barEntries = remember(seed) {
        (0..4).map { i ->
            val r = Random(seed + i * 5)
            BarEntry(i.toFloat(), floatArrayOf(r.nextInt(10, 35).toFloat(), r.nextInt(10, 30).toFloat(), r.nextInt(10, 40).toFloat()))
        }
    }

    val dataSet = remember(barEntries, drawValuesState, animProgress.value) {
        BarDataSet("Stacked Revenue", barEntries).apply {
            colors = listOf(Color(0xFF00ADB5), Color(0xFFFF5252), Color(0xFF9C27B0))
            drawValues = drawValuesState
            animateProgress = animProgress.value
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("堆叠柱状图 (BarChart)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White) },
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
            Text("💡 特性：支持多阶段堆叠柱与分组对比，点击柱体显示堆叠总额 MarkerView 弹窗。", fontSize = 11.sp, color = Color.LightGray.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(16.dp))

            BarChart(
                dataSets = listOf(dataSet),
                modifier = Modifier.fillMaxWidth().height(420.dp)
            )
        }
    }
}
