package com.charts.compose

import androidx.compose.runtime.Composable

@Composable
actual fun LocalBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS back gestures are handled via edge swipe and UI back buttons
}
