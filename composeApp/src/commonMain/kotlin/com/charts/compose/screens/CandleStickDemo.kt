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
import com.charts.compose.charts.CandleStickChart
import com.charts.compose.core.edgeSwipeToBack
import com.charts.compose.data.CandleDataSet
import com.charts.compose.data.CandleEntry
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CandleStickDemo(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var seed by remember { mutableStateOf(0) }
    val animProgress = remember { Animatable(1f) }

    val candleEntries = remember(seed) {
        var basePrice = 100f
        (0..7).map { i ->
            val r = Random(seed + i * 9)
            val open = basePrice + r.nextInt(-5, 6)
            val close = open + r.nextInt(-10, 12)
            val high = maxOf(open, close) + r.nextInt(2, 10)
            val low = minOf(open, close) - r.nextInt(2, 10)
            basePrice = close
            CandleEntry(i.toFloat(), high = high, low = low, open = open, close = close)
        }
    }

    val dataSet = remember(candleEntries, animProgress.value) {
        CandleDataSet("Crypto K-Line", candleEntries).apply {
            animateProgress = animProgress.value
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("K线/蜡烛图 (CandleStick)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White) },
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
                    Text("🎲 模拟大盘走势", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
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
            Text("💡 商业金融特性：点击 K 线显示 OHCL（开高低收）MarkerView 弹窗，放缩边界锁定无空白。", fontSize = 11.sp, color = Color.LightGray.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(16.dp))

            CandleStickChart(
                dataSet = dataSet,
                modifier = Modifier.fillMaxWidth().height(420.dp)
            )
        }
    }
}
