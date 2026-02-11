package com.mohamed.calmplayer.ui.components

import android.graphics.Matrix
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun getSquircleShape() = MorphingShape(0f)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun getMorphingCircleShape() = MorphingShape(1f)

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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MorphingShape(
    morphProgress: Float
): Shape {
    val squircle = remember {
        RoundedPolygon(
            numVertices = 4,
            rounding = CornerRounding(0.4f)
        )
    }
    val circle = remember {
        // Create a circle using many vertices or high rounding
        RoundedPolygon(
            numVertices = 12,
            rounding = CornerRounding(radius = 1f)
        )
    }
    val morph = remember(squircle, circle) {
        Morph(squircle, circle)
    }
    
    return remember(morph, morphProgress) {
        object : Shape {
            private val androidPath = android.graphics.Path()
            private val matrix = Matrix()
            
            override fun createOutline(
                size: Size,
                layoutDirection: LayoutDirection,
                density: Density
            ): Outline {
                androidPath.reset()
                morph.toPath(morphProgress, androidPath)
                
                // Scale and center (RoundedPolygon is centered at 0,0 with radius 1)
                matrix.reset()
                matrix.postScale(size.width / 2f, size.height / 2f)
                matrix.postTranslate(size.width / 2f, size.height / 2f)
                androidPath.transform(matrix)
                
                return Outline.Generic(androidPath.asComposePath())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy, 
            stiffness = Spring.StiffnessLow
        ),
        label = "shapeMorph"
    )
    
    val shape = MorphingShape(morphProgress)
    
    Box(
        modifier = modifier
            .graphicsLayer {
                this.shape = shape
                clip = true
            }
            .background(color)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
