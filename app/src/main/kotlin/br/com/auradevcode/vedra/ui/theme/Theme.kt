package br.com.auradevcode.vedra.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.auradevcode.vedra.R

/** Paleta da marca (specs/08). */
object VedraColors {
    val AuraPurple = Color(0xFF8A2CE2)
    val AuraPurpleLight = Color(0xFFB07CFF)
    val DigitalCyan = Color(0xFF00E5FF)
    val IconStart = Color(0xFFA13BFF)
    val IconMid = Color(0xFF6A3DF0)
    val IconEnd = Color(0xFF00C8E0)

    val Background = Color(0xFF0A0A0A)
    val Surface = Color(0xFF141414)
    val SurfaceVariant = Color(0xFF1E1E1E)
    val Outline = Color(0xFF2A2A2A)
    val OnBackground = Color(0xFFF2F2F2)
    val Muted = Color(0xFF9A9A9A)

    val Protected = Color(0xFF3DDC84)
    val Attention = Color(0xFFFFB020)
    val Unprotected = Color(0xFFFF4D5E)

    val IconGradient = Brush.linearGradient(listOf(IconStart, IconMid, IconEnd))
}

val Montserrat = FontFamily(
    Font(R.font.montserrat_semibold, FontWeight.SemiBold),
    Font(R.font.montserrat_bold, FontWeight.Bold),
    Font(R.font.montserrat_extrabold, FontWeight.ExtraBold),
)

private val DarkScheme = darkColorScheme(
    primary = VedraColors.AuraPurple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF2A1450),
    onPrimaryContainer = VedraColors.AuraPurpleLight,
    secondary = VedraColors.DigitalCyan,
    onSecondary = Color(0xFF00151A),
    background = VedraColors.Background,
    onBackground = VedraColors.OnBackground,
    surface = VedraColors.Background,
    onSurface = VedraColors.OnBackground,
    surfaceVariant = VedraColors.SurfaceVariant,
    onSurfaceVariant = VedraColors.Muted,
    surfaceContainer = VedraColors.Surface,
    surfaceContainerHigh = VedraColors.SurfaceVariant,
    outline = VedraColors.Outline,
    outlineVariant = VedraColors.Outline,
    error = VedraColors.Unprotected,
)

private val base = Typography()

private val VedraTypography = Typography(
    displaySmall = base.displaySmall.copy(fontFamily = Montserrat, fontWeight = FontWeight.ExtraBold),
    headlineMedium = base.headlineMedium.copy(fontFamily = Montserrat, fontWeight = FontWeight.Bold),
    headlineSmall = base.headlineSmall.copy(fontFamily = Montserrat, fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.copy(fontFamily = Montserrat, fontWeight = FontWeight.Bold),
    titleMedium = base.titleMedium.copy(fontFamily = Montserrat, fontWeight = FontWeight.SemiBold),
)

val PinDigitStyle = TextStyle(fontFamily = Montserrat, fontWeight = FontWeight.SemiBold, fontSize = 28.sp)

private val VedraShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
)

@Composable
fun VedraTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkScheme, typography = VedraTypography, shapes = VedraShapes, content = content)
}
