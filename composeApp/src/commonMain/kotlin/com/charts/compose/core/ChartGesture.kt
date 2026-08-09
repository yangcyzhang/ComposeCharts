package com.charts.compose.core

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput

fun Modifier.chartTransformGesture(
    onTransform: (zoomChange: Float, panChange: Offset, centroid: Offset) -> Unit
): Modifier = this.pointerInput(Unit) {
    detectTransformGestures { centroid, pan, zoom, rotation ->
        onTransform(zoom, pan, centroid)
    }
}

fun Modifier.edgeSwipeToBack(onBack: () -> Unit): Modifier = this.pointerInput(Unit) {
    detectDragGestures { change, dragAmount ->
        if (change.position.x - dragAmount.x < 120f && dragAmount.x > 25f) {
            onBack()
        }
    }
}
