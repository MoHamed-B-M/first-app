package com.mohamed.calmplayer.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    surface = Color(0xFF121212),
    onSurface = Color(0xFFE1E1E1)
)

private val LightColorScheme = lightColorScheme(
    primary = CalmGreenPrimary,
    secondary = CalmGreenSecondary,
    tertiary = CalmGreenTertiary,
    surface = Color(0xFFFDFDFD),
    onSurface = Color(0xFF1B1B1B)
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CalmMusicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    seedColor: Color? = null,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        seedColor != null -> {
            // In M3 Expressive, we can use a seed color for "vibrant" shifts
            if (darkTheme) darkColorScheme(primary = seedColor) 
            else lightColorScheme(primary = seedColor)
            // Note: Ideally use a proper SchemeExpressive builder here
        }
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = Typography,
        motionScheme = MotionScheme.expressive(),
        shapes = Shapes,
        content = content
    )
}
