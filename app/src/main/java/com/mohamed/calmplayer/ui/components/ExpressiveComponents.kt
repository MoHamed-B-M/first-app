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

@Composable
fun WavyProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    waveHeight: Dp = 6.dp,
    waveLength: Dp = 24.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier.height(waveHeight * 3).fillMaxWidth()) {
        val width = size.width
        val height = size.height
        val centerY = height / 2

        // Draw Track
        drawLine(
            color = trackColor,
            start = Offset(0f, centerY),
            end = Offset(width, centerY),
            strokeWidth = 4.dp.toPx(),
            cap = Stroke.Cap.Round
        )

        // Draw Progress Wave
        val progressWidth = width * progress
        val path = Path()
        path.moveTo(0f, centerY)

        val waveLengthPx = waveLength.toPx()
        val waveHeightPx = waveHeight.toPx()
        
        var x = 0f
        while (x <= progressWidth) {
            val y = centerY + sin((x / waveLengthPx) * 2 * Math.PI + phase).toFloat() * waveHeightPx
            path.lineTo(x, y)
            x += 2f // High precision for smoothness
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = 6.dp.toPx(),
                cap = Stroke.Cap.Round,
                join = Stroke.Join.Round
            )
        )
        
        // Draw Thumb
        if (progress > 0) {
             val thumbX = progressWidth
             val thumbY = centerY + sin((thumbX / waveLengthPx) * 2 * Math.PI + phase).toFloat() * waveHeightPx
             drawCircle(
                 color = color,
                 radius = 10.dp.toPx(),
                 center = Offset(thumbX, thumbY)
             )
        }
    }
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
