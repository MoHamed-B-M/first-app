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

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.ui.graphics.lerp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MorphingShape(
    morphProgress: Float,
    isCircle: Boolean = false
): GenericShape {
    return remember(morphProgress, isCircle) {
        GenericShape { size, _ ->
            val w = size.width
            val h = size.height
            
            // Progress from Squircle (0f) to Circle (1f)
            // For a circle, control points are roughly 0.55228f * radius
            val k = 0.55228f
            
            // Squircle-ish control points (very rounded)
            val sK = 0.2f
            
            val currentK = lerp(sK, k, morphProgress)
            
            moveTo(0f, h / 2)
            cubicTo(0f, h / 2 - h / 2 * currentK, w / 2 - w / 2 * currentK, 0f, w / 2, 0f)
            cubicTo(w / 2 + w / 2 * currentK, 0f, w, h / 2 - h / 2 * currentK, w, h / 2)
            cubicTo(w, h / 2 + h / 2 * currentK, w / 2 + w / 2 * currentK, h, w / 2, h)
            cubicTo(w / 2 - w / 2 * currentK, h, 0f, h / 2 + h / 2 * currentK, 0f, h / 2)
            close()
        }
    }
}

@Composable
fun SquircleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    color: Color = MaterialTheme.colorScheme.primaryContainer,
    content: @Composable () -> Unit
) {
    val morphProgress by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "shapeMorph"
    )
    
    val shape = MorphingShape(morphProgress)
    
    Box(
        modifier = modifier
            .background(color, shape)
            .clickable(onClick = onClick),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        content()
    }
}
