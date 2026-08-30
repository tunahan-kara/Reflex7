package com.tunahankara.reflex7.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object ReflexColors {
    val Page = Color(0xFF000000)
    val PageSoft = Color(0xFF050805)
    val Primary = Color(0xFF4CAF50)
    val PrimaryBright = Color(0xFF8CFF91)
    val PrimaryDim = Color(0xFF2E7D32)
    val Choice = Color(0xFF102313)
    val ChoiceShadow = Color(0xFF1B5E20)
    val PanelTop = Color(0xF5121A13)
    val PanelBottom = Color(0xF7040805)
    val Ink = Color(0xFFF2FFF3)
    val Muted = Color(0xFF91A493)
    val Warning = Color(0xFFFFEB3B)
    val Danger = Color(0xFFFF6259)
    val DangerSoft = Color(0xFFFF8A80)
    val Cyan = Color(0xFF80CBC4)
    val Fast = Color(0xFFFF9800)
    val FastShadow = Color(0xFFE68A00)
    val Blue = Color(0xFF2196F3)
    val Border = Color(0x7A5DFF70)
    val BorderMuted = Color(0x4076C77B)
    val Overlay = Color(0xF0000401)
    val TimerTrack = Color(0xFF222222)
}

object ReflexSpacing {
    val Xs = 4.dp
    val Sm = 8.dp
    val Md = 12.dp
    val Lg = 18.dp
    val Xl = 26.dp
    val Touch = 44.dp
    val PanelRadius = 6.dp
    val ControlRadius = 5.dp
    val TaskRadius = 11.dp
}

object ReflexMotion {
    const val FastMs = 120
    const val NormalMs = 220
}

val TerminalFamily = FontFamily.Monospace
val InterfaceFamily = FontFamily.SansSerif

private val scheme = darkColorScheme(
    primary = ReflexColors.Primary,
    onPrimary = ReflexColors.Ink,
    secondary = ReflexColors.Cyan,
    background = ReflexColors.Page,
    onBackground = ReflexColors.Ink,
    surface = ReflexColors.PageSoft,
    onSurface = ReflexColors.Ink,
    error = ReflexColors.Danger
)

@Composable
fun Reflex7Theme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = scheme,
        typography = MaterialTheme.typography.copy(
            displayLarge = TextStyle(fontFamily = TerminalFamily, fontWeight = FontWeight.Normal, fontSize = 64.sp, letterSpacing = 3.sp),
            headlineLarge = TextStyle(fontFamily = TerminalFamily, fontWeight = FontWeight.Normal, fontSize = 42.sp, letterSpacing = 3.sp),
            headlineMedium = TextStyle(fontFamily = TerminalFamily, fontWeight = FontWeight.Normal, fontSize = 30.sp, letterSpacing = 2.sp),
            titleLarge = TextStyle(fontFamily = TerminalFamily, fontWeight = FontWeight.Normal, fontSize = 24.sp, letterSpacing = 1.2.sp),
            titleMedium = TextStyle(fontFamily = TerminalFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, letterSpacing = .8.sp),
            bodyLarge = TextStyle(fontFamily = InterfaceFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 23.sp),
            bodyMedium = TextStyle(fontFamily = InterfaceFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
            labelLarge = TextStyle(fontFamily = InterfaceFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = .2.sp),
            labelMedium = TextStyle(fontFamily = TerminalFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, letterSpacing = 1.sp)
        ),
        content = content
    )
}

val TerminalGreen = ReflexColors.Primary
val TerminalDark = ReflexColors.PageSoft
val PanelDark = ReflexColors.Choice
val DangerRed = ReflexColors.Danger
