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
import com.charts.compose.charts.RadarChart
import com.charts.compose.core.edgeSwipeToBack
import com.charts.compose.data.RadarDataSet
import com.charts.compose.data.RadarEntry
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadarDemo(onBack: () -> Unit) {
    var seed by remember { mutableStateOf(0) }

    val radarEntries1 = remember(seed) {
        val r = Random(seed + 1)
        listOf(
            RadarEntry(r.nextInt(50, 99).toFloat(), "Attack"), RadarEntry(r.nextInt(40, 95).toFloat(), "Defense"),
            RadarEntry(r.nextInt(60, 99).toFloat(), "Speed"), RadarEntry(r.nextInt(50, 90).toFloat(), "Stamina"),
            RadarEntry(r.nextInt(70, 99).toFloat(), "Magic"), RadarEntry(r.nextInt(40, 90).toFloat(), "Luck")
        )
    }
    val radarEntries2 = remember(seed) {
        val r = Random(seed + 2)
        listOf(
            RadarEntry(r.nextInt(40, 90).toFloat(), "Attack"), RadarEntry(r.nextInt(60, 99).toFloat(), "Defense"),
            RadarEntry(r.nextInt(30, 80).toFloat(), "Speed"), RadarEntry(r.nextInt(70, 99).toFloat(), "Stamina"),
            RadarEntry(r.nextInt(30, 75).toFloat(), "Magic"), RadarEntry(r.nextInt(50, 85).toFloat(), "Luck")
        )
    }

    val dataSet1 = remember(radarEntries1) {
        RadarDataSet("Mage Class", radarEntries1).apply { color = Color(0xFF00ADB5) }
    }
    val dataSet2 = remember(radarEntries2) {
        RadarDataSet("Tank Class", radarEntries2).apply { color = Color(0xFFFF5252) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("蜘蛛网雷达图 (RadarChart)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White) },
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
                    Text("🎲 随机英雄属性", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("💡 极坐标多边形：点击多边形节点可触发属性维度 Score 弹窗，底部显示多数据集 Legend。", fontSize = 11.sp, color = Color.LightGray.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(16.dp))

            RadarChart(
                dataSets = listOf(dataSet1, dataSet2),
                modifier = Modifier.fillMaxWidth().height(420.dp)
            )
        }
    }
}
