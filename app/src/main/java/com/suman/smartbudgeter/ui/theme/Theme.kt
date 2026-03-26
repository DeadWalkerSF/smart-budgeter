package com.suman.smartbudgeter.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SmartColorScheme = darkColorScheme(
    primary = BudgetGold,
    onPrimary = DeepBlack,
    secondary = SoftGold,
    onSecondary = DeepBlack,
    tertiary = Graphite,
    background = DeepBlack,
    onBackground = Mist,
    surface = ElevatedBlack,
    onSurface = Mist,
    surfaceVariant = SurfaceBlack,
    onSurfaceVariant = MutedText,
    error = Danger,
    onError = DeepBlack,
)

@Composable
fun SmartBudgeterTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = SmartColorScheme,
        typography = SmartTypography,
        content = content,
    )
}
