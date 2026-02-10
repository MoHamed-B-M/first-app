package com.mohamed.calmplayer.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sin
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin

@Composable
fun ExpressiveSlider(
    progress: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    Slider(
        value = progress,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        colors = SliderDefaults.colors(
            thumbColor = color,
            activeTrackColor = color,
            inactiveTrackColor = trackColor
        )
    )
}

val SquircleShape = GenericShape { size, _ ->
    val radius = size.width / 4 // Adjust for squircle-ness (hyperellipse approximation)
    
    // Simple approach: Rounded rect with large corners, or a true superellipse path
    // For "Expressive" look, let's use a very rounded rect which is close enough for standard UI
    // Or we can use a cubic bezier approximation for a smoother squircle
    
    val w = size.width
    val h = size.height
    
    moveTo(0f, h / 2)
    cubicTo(0f, 0f, 0f, 0f, w / 2, 0f) // Top-Left
    cubicTo(w, 0f, w, 0f, w, h / 2) // Top-Right
    cubicTo(w, h, w, h, w / 2, h) // Bottom-Right
    cubicTo(0f, h, 0f, h, 0f, h / 2) // Bottom-Left
    close()
}

@Composable
fun SquircleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primaryContainer,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(color, SquircleShape)
            .clickable(onClick = onClick),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        content()
    }
}
