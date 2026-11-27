package com.upb.personajes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import coil.compose.AsyncImage

/**
 * Imagen del personaje. Debajo siempre están las iniciales: se ven mientras carga
 * o si la imagen no se puede descargar (o el personaje no tiene).
 */
@Composable
fun ImagenPersonaje(
    url: String?,
    nombre: String,
    tamanoIniciales: TextUnit,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = iniciales(nombre),
            fontSize = tamanoIniciales,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = nombre,
                contentScale = contentScale,
                alignment = Alignment.TopCenter,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

private fun iniciales(nombre: String): String =
    nombre.split(Regex("\\s+"))
        .mapNotNull { palabra -> palabra.firstOrNull()?.takeIf { it.isLetterOrDigit() }?.uppercaseChar() }
        .take(2)
        .joinToString("")
        .ifEmpty { "?" }
