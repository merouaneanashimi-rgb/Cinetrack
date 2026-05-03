package com.cinetrack.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.cinetrack.domain.model.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF01B4E4),
    onPrimary = Color(0xFF003544),
    primaryContainer = Color(0xFF014D61),
    onPrimaryContainer = Color(0xFFB8EAFF),
    secondary = Color(0xFF90CEA1),
    onSecondary = Color(0xFF00391A),
    secondaryContainer = Color(0xFF1B4332),
    onSecondaryContainer = Color(0xFFACF4C2),
    tertiary = Color(0xFFB983FF),
    onTertiary = Color(0xFF3A0080),
    tertiaryContainer = Color(0xFF5317A6),
    onTertiaryContainer = Color(0xFFE9DDFF),
    error = Color(0xFFF44336),
    onError = Color.White,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0D1B2A),
    onBackground = Color(0xFFE0E2E8),
    surface = Color(0xFF1A2A3A),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF243447),
    onSurfaceVariant = Color(0xFF8899AA),
    outline = Color(0xFF3A5068),
    outlineVariant = Color(0xFF2A4055),
    inverseOnSurface = Color(0xFF1A1C1E),
    inverseSurface = Color(0xFFE2E2E5),
    scrim = Color(0xFF000000)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF007EA8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB8EAFF),
    onPrimaryContainer = Color(0xFF001F2A),
    secondary = Color(0xFF2D6A4F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFACF4C2),
    onSecondaryContainer = Color(0xFF002110),
    tertiary = Color(0xFF6B4EA0),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE9DDFF),
    onTertiaryContainer = Color(0xFF24005A),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF8F9FF),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE0E2E8),
    onSurfaceVariant = Color(0xFF43474E),
    outline = Color(0xFF74777F),
    outlineVariant = Color(0xFFC3C6CF),
    inverseOnSurface = Color(0xFFF1F0F4),
    inverseSurface = Color(0xFF2F3033),
    scrim = Color(0xFF000000)
)

@Composable
fun CineTrackTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val colorScheme = when {
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
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CineTrackTypography,
        content = content
    )
}
