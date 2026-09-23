package com.organistaapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = VioletaPrimario,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = VioletaClaro,
    onPrimaryContainer = androidx.compose.ui.graphics.Color.White,
    secondary = DouradoAcento,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = DouradoClaro,
    background = CinzaFundo,
    surface = CinzaCard,
    onBackground = CinzaTexto,
    onSurface = CinzaTexto,
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFFEDE8F5),
    outline = androidx.compose.ui.graphics.Color(0xFFCCBBDD)
)

private val DarkColorScheme = darkColorScheme(
    primary = VioletaClaro,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = VioletaEscuro,
    onPrimaryContainer = androidx.compose.ui.graphics.Color.White,
    secondary = DouradoAcento,
    onSecondary = FundoEscuro,
    secondaryContainer = DouradoClaro,
    background = FundoEscuro,
    surface = CardEscuro,
    onBackground = androidx.compose.ui.graphics.Color.White,
    onSurface = androidx.compose.ui.graphics.Color.White,
    surfaceVariant = SuperficieEscura,
    outline = androidx.compose.ui.graphics.Color(0xFF6B5588)
)

@Composable
fun OrganistaAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
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
