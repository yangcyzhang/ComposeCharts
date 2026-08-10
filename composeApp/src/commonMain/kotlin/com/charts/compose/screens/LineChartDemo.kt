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
import com.charts.compose.core.MarkerViewConfig
import com.charts.compose.core.edgeSwipeToBack
import com.charts.compose.data.AnimationType
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
    var animationType by remember { mutableStateOf(AnimationType.VERTICAL) }
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(seed, animationType) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(1200))
    }

    val entries1 = remember(seed) {
        (0..6).map { i -> Entry(i.toFloat(), Random(seed + i * 3).nextInt(15, 85).toFloat()) }
    }
    val entries2 = remember(seed) {
        (0..6).map { i -> Entry(i.toFloat(), Random(seed + i * 7 + 1).nextInt(10, 90).toFloat()) }
    }

    val dataSet1 = remember(entries1, drawValuesState, drawFilledState, animProgress.value, animationType) {
        LineDataSet("Pro Growth", entries1).apply {
            color = Color(0xFF00ADB5)
            drawValues = drawValuesState
            drawFilled = drawFilledState
            animateProgress = animProgress.value
            this.animationType = animationType
        }
    }
    val dataSet2 = remember(entries2, drawValuesState, drawFilledState, animProgress.value, animationType) {
        LineDataSet("Lite Growth", entries2).apply {
            color = Color(0xFFE854FF)
            drawValues = drawValuesState
            drawFilled = drawFilledState
            animateProgress = animProgress.value
            this.animationType = animationType
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
                    Text("🎲 随机", fontSize = 10.sp, color = Color.White)
                }
                
                // 切换动画类型
                Button(
                    onClick = {
                        animationType = when(animationType) {
                            AnimationType.VERTICAL -> AnimationType.HORIZONTAL
                            AnimationType.HORIZONTAL -> AnimationType.REVEAL
                            AnimationType.REVEAL -> AnimationType.VERTICAL
                            else -> AnimationType.VERTICAL
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2838), contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("🎭 ${animationType.name}", fontSize = 10.sp, color = Color.White)
                }

                Button(
                    onClick = { drawValuesState = !drawValuesState },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2838), contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(if (drawValuesState) "👁️ 隐藏" else "👁️ 显示", fontSize = 10.sp, color = Color.White)
                }
                
                Button(
                    onClick = {
                        scope.launch {
                            animProgress.snapTo(0f)
                            animProgress.animateTo(1f, animationSpec = tween(1200))
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0), contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("⚡ 动画", fontSize = 10.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            LineChart(
                dataSets = listOf(dataSet1, dataSet2),
                modifier = Modifier.fillMaxWidth().height(420.dp),
                markerConfig = MarkerViewConfig(
                    backgroundColor = Color(0xEE222222),
                    badgeWidth = 10f,
                    cornerRadius = 16f
                )
            )
        }
    }
}
