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

private val CyberColorScheme = darkColorScheme(
  primary = NeonBlue,
  secondary = NeonPurple,
  tertiary = AccentCyan,
  background = CyberBackground,
  surface = CyberCard,
  onPrimary = Color(0xFF050505),
  onSecondary = Color.White,
  onTertiary = Color(0xFF050505),
  onBackground = Color.White,
  onSurface = Color.White
)

private val DarkColorScheme =
  darkColorScheme(primary = Purple80, secondary = PurpleGrey80, tertiary = Pink80)

private val LightColorScheme =
  lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme for Kartik Labs
  dynamicColor: Boolean = false, // Force custom neon colors
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) CyberColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
