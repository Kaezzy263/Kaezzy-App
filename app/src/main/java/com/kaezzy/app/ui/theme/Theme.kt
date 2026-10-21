package com.kaezzy.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val KaezzyDarkColorScheme = darkColorScheme(
    primary = PrimaryAccent,
    onPrimary = BackgroundPrimary,
    primaryContainer = SurfaceLevel2,
    onPrimaryContainer = TextPrimary,
    secondary = SecondaryAccent,
    onSecondary = BackgroundPrimary,
    background = BackgroundPrimary,
    onBackground = TextPrimary,
    surface = SurfaceLevel1,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceLevel2,
    onSurfaceVariant = TextSecondary,
    error = SemanticDanger,
    onError = TextPrimary
)

@Composable
fun KaezzyTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BackgroundPrimary.toArgb()
            window.navigationBarColor = BackgroundPrimary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = KaezzyDarkColorScheme,
        typography = Typography,
        content = content
    )
}
