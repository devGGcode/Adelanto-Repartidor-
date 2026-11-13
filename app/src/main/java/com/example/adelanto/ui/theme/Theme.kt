package com.example.adelanto.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = AppColors.PrimaryLight,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = AppColors.PrimaryLightBg,
    onPrimaryContainer = AppColors.PrimaryLight,
    secondary = AppColors.PrimaryLight,
    onSecondary = Color(0xFFFFFFFF),
    tertiary = AppColors.SuccessLight,
    background = AppColors.BgLight,
    onBackground = AppColors.TextMainLight,
    surface = AppColors.SurfaceLight,
    onSurface = AppColors.TextMainLight,
    surfaceVariant = AppColors.SurfaceCardLight,
    onSurfaceVariant = AppColors.TextMutedLight,
    outline = AppColors.BorderLight,
    error = AppColors.DangerLight,
    onError = Color(0xFFFFFFFF)
)
private val DarkColorScheme = darkColorScheme(
    primary = AppColors.PrimaryDark,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = AppColors.PrimaryLightBgDark,
    onPrimaryContainer = AppColors.PrimaryDark,
    secondary = AppColors.PrimaryDark,
    onSecondary = Color(0xFFFFFFFF),
    tertiary = AppColors.SuccessDark,
    background = AppColors.BgDark,
    onBackground = AppColors.TextMainDark,
    surface = AppColors.SurfaceDark,
    onSurface = AppColors.TextMainDark,
    surfaceVariant = AppColors.SurfaceCardDark,
    onSurfaceVariant = AppColors.TextMutedDark,
    outline = AppColors.BorderDark,
    error = AppColors.DangerDark,
    onError = Color(0xFFFFFFFF)
)

@Composable
fun AdelantoTheme(
    darkTheme: Boolean = isDark(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
