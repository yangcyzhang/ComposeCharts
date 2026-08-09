package com.charts.compose

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@Composable
actual fun LocalBackHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled = enabled, onBack = onBack)
}
