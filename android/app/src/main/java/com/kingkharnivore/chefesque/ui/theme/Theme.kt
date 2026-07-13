package com.kingkharnivore.chefesque.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = SoftSage,
    onPrimary = ForestShadow,
    primaryContainer = DeepMossGreen,
    onPrimaryContainer = MistySage,
    secondary = WarmClay,
    onSecondary = RoastedClay,
    secondaryContainer = EmberClay,
    onSecondaryContainer = SoftClay,
    tertiary = WarmClay,
    onTertiary = RoastedClay,
    tertiaryContainer = EmberClay,
    onTertiaryContainer = SoftClay,
    background = NightMoss,
    onBackground = OatLinen,
    surface = MossCharcoal,
    onSurface = WarmPorcelainDark,
    surfaceDim = SurfaceContainerLowestDark,
    surfaceBright = SurfaceContainerHighestDark,
    surfaceTint = SoftSage,
    surfaceVariant = DeepSageCharcoal,
    onSurfaceVariant = WarmStone,
    outline = SoftPewter,
    outlineVariant = OutlineVariantDark,
    error = SoftCoralRed,
    onError = DeepWine,
    errorContainer = DarkError,
    onErrorContainer = PaleError,
    inverseSurface = InverseSurfaceDark,
    inverseOnSurface = InverseOnSurfaceDark,
    inversePrimary = InversePrimaryDark,
    scrim = Color(0xFF000000),
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
)

private val LightColorScheme = lightColorScheme(
    primary = DeepForest,
    onPrimary = WarmPorcelain,
    primaryContainer = MistySage,
    onPrimaryContainer = ForestShadow,
    secondary = FiredTerracotta,
    onSecondary = White,
    secondaryContainer = SoftClay,
    onSecondaryContainer = DarkClay,
    tertiary = FiredTerracotta,
    onTertiary = White,
    tertiaryContainer = SoftClay,
    onTertiaryContainer = DarkClay,
    background = OatCream,
    onBackground = WarmCharcoal,
    surface = Porcelain,
    onSurface = WarmCharcoal,
    surfaceDim = SurfaceContainerHighLight,
    surfaceBright = SurfaceContainerLowestLight,
    surfaceTint = DeepForest,
    surfaceVariant = StoneBeige,
    onSurfaceVariant = MutedCharcoal,
    outline = WarmGrey,
    outlineVariant = OutlineVariantLight,
    error = DeepRed,
    onError = White,
    errorContainer = LightErrorContainer,
    onErrorContainer = OnLightErrorContainer,
    inverseSurface = InverseSurfaceLight,
    inverseOnSurface = InverseOnSurfaceLight,
    inversePrimary = InversePrimaryLight,
    scrim = Color(0xFF000000),
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight,
)

@Composable
fun ChefesqueTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ChefesqueTypography,
        content = content,
    )
}
