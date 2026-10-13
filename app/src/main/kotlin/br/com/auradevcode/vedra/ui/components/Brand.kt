package br.com.auradevcode.vedra.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.auradevcode.vedra.R
import br.com.auradevcode.vedra.ui.theme.VedraColors

/** Ícone do Vedra: símbolo branco sobre o gradiente da marca. */
@Composable
fun VedraIcon(size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.25f))
            .background(VedraColors.IconGradient),
        contentAlignment = Alignment.Center,
    ) {
        Image(painterResource(R.drawable.ic_vedra_glyph), contentDescription = null, modifier = Modifier.fillMaxSize())
    }
}

/** Aura: brilho radial roxo e ciano atrás de um elemento (specs/08, formas e movimento). */
@Composable
fun Aura(modifier: Modifier = Modifier, tint: Color = VedraColors.AuraPurple, alpha: Float = 0.28f) {
    Canvas(modifier) {
        val radius = size.minDimension / 2
        drawCircle(
            brush = Brush.radialGradient(
                listOf(tint.copy(alpha = alpha), Color.Transparent),
                center = Offset(size.width * 0.42f, size.height * 0.45f),
                radius = radius,
            ),
            radius = radius,
            center = Offset(size.width * 0.42f, size.height * 0.45f),
        )
        drawCircle(
            brush = Brush.radialGradient(
                listOf(VedraColors.DigitalCyan.copy(alpha = alpha * 0.6f), Color.Transparent),
                center = Offset(size.width * 0.6f, size.height * 0.58f),
                radius = radius * 0.8f,
            ),
            radius = radius * 0.8f,
            center = Offset(size.width * 0.6f, size.height * 0.58f),
        )
    }
}

@Composable
fun BrandHeader(modifier: Modifier = Modifier) {
    Box(modifier.padding(8.dp), contentAlignment = Alignment.Center) {
        Aura(Modifier.size(220.dp))
        VedraIcon(96.dp)
    }
}
