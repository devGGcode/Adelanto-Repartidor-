package com.example.adelanto.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object AppColors {
    // Light
    val BgLight = Color(0xFFF1F5F9)
    val SurfaceLight = Color(0xFFFFFFFF)
    val SurfaceCardLight = Color(0xFFFFFFFF)
    val TextMainLight = Color(0xFF0F172A)
    val TextMutedLight = Color(0xFF64748B)
    val BorderLight = Color(0xFFE2E8F0)
    val PrimaryLight = Color(0xFF4F46E5)
    val PrimaryHoverLight = Color(0xFF4338CA)
    val PrimaryLightBg = Color(0xFFE0E7FF)
    val SuccessLight = Color(0xFF10B981)
    val SuccessBgLight = Color(0xFFD1FAE5)
    val DangerLight = Color(0xFFEF4444)
    val DangerBgLight = Color(0xFFFEE2E2)
    val WarningBgLight = Color(0xFFFEF3C7)
    val WarningTextLight = Color(0xFFD97706)
    val HeroGradientLight = Brush.linearGradient(listOf(Color(0xFF4F46E5), Color(0xFF3B82F6)))

    // Dark
    val BgDark = Color(0xFF0B0F19)
    val SurfaceDark = Color(0xFF111827)
    val SurfaceCardDark = Color(0xFF1F2937)
    val TextMainDark = Color(0xFFF9FAFB)
    val TextMutedDark = Color(0xFF9CA3AF)
    val BorderDark = Color(0xFF374151)
    val PrimaryDark = Color(0xFF6366F1)
    val PrimaryHoverDark = Color(0xFF4F46E5)
    val PrimaryLightBgDark = Color(0xFF312E81)
    val SuccessDark = Color(0xFF34D399)
    val SuccessBgDark = Color(0x2634D399)
    val DangerDark = Color(0xFFF87171)
    val DangerBgDark = Color(0x26F87171)
    val WarningBgDark = Color(0x26FBBF24)
    val WarningTextDark = Color(0xFFFBBF24)
    val HeroGradientDark = Brush.linearGradient(listOf(Color(0xFF4338CA), Color(0xFF2563EB)))
}

@Composable
fun isDark(): Boolean = isSystemInDarkTheme()

@Composable
fun appBg() = if (isDark()) AppColors.BgDark else AppColors.BgLight

@Composable
fun appSurface() = if (isDark()) AppColors.SurfaceDark else AppColors.SurfaceLight

@Composable
fun appCard() = if (isDark()) AppColors.SurfaceCardDark else AppColors.SurfaceCardLight

@Composable
fun appText() = if (isDark()) AppColors.TextMainDark else AppColors.TextMainLight

@Composable
fun appMuted() = if (isDark()) AppColors.TextMutedDark else AppColors.TextMutedLight

@Composable
fun appBorder() = if (isDark()) AppColors.BorderDark else AppColors.BorderLight

@Composable
fun appPrimary() = if (isDark()) AppColors.PrimaryDark else AppColors.PrimaryLight

@Composable
fun appPrimaryHover() = if (isDark()) AppColors.PrimaryHoverDark else AppColors.PrimaryHoverLight

@Composable
fun appPrimaryLight() = if (isDark()) AppColors.PrimaryLightBgDark else AppColors.PrimaryLightBg

@Composable
fun appSuccess() = if (isDark()) AppColors.SuccessDark else AppColors.SuccessLight

@Composable
fun appSuccessBg() = if (isDark()) AppColors.SuccessBgDark else AppColors.SuccessBgLight

@Composable
fun appDanger() = if (isDark()) AppColors.DangerDark else AppColors.DangerLight

@Composable
fun appDangerBg() = if (isDark()) AppColors.DangerBgDark else AppColors.DangerBgLight

@Composable
fun appWarningBg() = if (isDark()) AppColors.WarningBgDark else AppColors.WarningBgLight

@Composable
fun appWarningText() = if (isDark()) AppColors.WarningTextDark else AppColors.WarningTextLight

@Composable
fun appHeroGradient() = if (isDark()) AppColors.HeroGradientDark else AppColors.HeroGradientLight

@Composable
fun appShadow() = if (isDark()) Color(0x80000000) else Color(0x1A000000)
