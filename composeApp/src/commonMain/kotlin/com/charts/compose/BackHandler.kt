package com.charts.compose

import androidx.compose.runtime.Composable

@Composable
expect fun LocalBackHandler(enabled: Boolean = true, onBack: () -> Unit)
