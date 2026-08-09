package com.charts.compose.screens

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
import com.charts.compose.charts.PieChart
import com.charts.compose.core.edgeSwipeToBack
import com.charts.compose.data.PieDataSet
import com.charts.compose.data.PieEntry
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PieChartDemo(onBack: () -> Unit) {
    var seed by remember { mutableStateOf(0) }
    var holeRatioState by remember { mutableStateOf(0.55f) }

    val pieEntries = remember(seed) {
        val r = Random(seed + 1)
        listOf(
            PieEntry(r.nextInt(20, 50).toFloat(), "Kotlin", Color(0xFF00ADB5)),
            PieEntry(r.nextInt(15, 40).toFloat(), "Swift", Color(0xFFFF5252)),
            PieEntry(r.nextInt(10, 30).toFloat(), "Dart", Color(0xFF4CAF50)),
            PieEntry(r.nextInt(10, 25).toFloat(), "Rust", Color(0xFFFFB300)),
            PieEntry(r.nextInt(5, 15).toFloat(), "Other", Color(0xFF9C27B0))
        )
    }

    val dataSet = remember(pieEntries, holeRatioState) {
        PieDataSet("Language Share", pieEntries).apply {
            holeRadiusRatio = holeRatioState
            sliceSpace = 0f
            selectionShift = 16f
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("环形/饼图 (PieChart)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White) },
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
                    onClick = { holeRatioState = if (holeRatioState > 0f) 0f else 0.55f },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2838), contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(if (holeRatioState > 0f) "🍩 切换实心饼图" else "🍩 切换环形饼图", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("💡 交互提示：点击扇区触发抽离动画与占比 MarkerView 弹窗，已修正 12 点钟起始角度定位。", fontSize = 11.sp, color = Color.LightGray.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(16.dp))

            PieChart(
                dataSet = dataSet,
                modifier = Modifier.fillMaxWidth().height(420.dp)
            )
        }
    }
}
