package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Blue80,
    onPrimary = CompanyNavyDark,
    primaryContainer = CompanyBlueDark,
    onPrimaryContainer = Blue80,
    secondary = BlueGrey80,
    onSecondary = CompanyNavyDark,
    tertiary = LightBlue80,
    background = CompanyNavyDark,
    onBackground = Color.White,
    surface = CompanyNavy,
    onSurface = Color.White,
    surfaceVariant = CompanyNavySurface,
    onSurfaceVariant = BlueGrey80,
    error = DangerRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = CompanyBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE8FE),
    onPrimaryContainer = Color(0xFF0C2B6B),
    secondary = BlueGrey40,
    onSecondary = Color.White,
    tertiary = LightBlue40,
    background = Slate50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate700,
    error = DangerRed,
    onError = Color.White
)

@Composable
fun CompanyCallTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep crisp branded corporate look by default
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    CompanyCallTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
