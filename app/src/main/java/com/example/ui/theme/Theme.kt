package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TacticalColorScheme = darkColorScheme(
    primary = AttackTeamCyan,
    onPrimary = Color(0xFF00363F),
    primaryContainer = Color(0xFF004E5B),
    onPrimaryContainer = Color(0xFF9CF0FF),
    secondary = DefenseTeamOrange,
    onSecondary = Color(0xFF4C1B00),
    secondaryContainer = Color(0xFF6E2800),
    onSecondaryContainer = Color(0xFFFFDBCF),
    tertiary = GoalkeeperGold,
    onTertiary = Color(0xFF423100),
    background = PitchDarkBg,
    onBackground = Color(0xFFE5F5EC),
    surface = PitchSurface,
    onSurface = Color(0xFFE5F5EC),
    surfaceVariant = PitchSurfaceVariant,
    onSurfaceVariant = Color(0xFFC0D8CB),
    outline = PitchBorder,
    outlineVariant = Color(0xFF1B3D2C)
)

@Composable
fun CoachTacticsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Tactical pitch board looks best in deep high-contrast dark green
    MaterialTheme(
        colorScheme = TacticalColorScheme,
        typography = Typography,
        content = content
    )
}

// Keep backward compatibility
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    CoachTacticsTheme(content = content)
}
