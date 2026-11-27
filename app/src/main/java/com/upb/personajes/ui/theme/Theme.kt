package com.upb.personajes.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.upb.personajes.viewmodel.TonoEstado

private val EsquemaClaro = lightColorScheme(
    primary = Verde40,
    primaryContainer = VerdeContenedor,
    secondary = Azul40,
)

private val EsquemaOscuro = darkColorScheme(
    primary = Verde80,
    secondary = Azul80,
)

@Composable
fun PersonajesTheme(oscuro: Boolean = isSystemInDarkTheme(), contenido: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (oscuro) EsquemaOscuro else EsquemaClaro,
        typography = Tipografia,
        content = contenido,
    )
}

/** Color que corresponde al tono que decide el ViewModel. */
fun TonoEstado.color(): Color = when (this) {
    TonoEstado.POSITIVO -> EstadoVivo
    TonoEstado.NEGATIVO -> EstadoMuerto
    TonoEstado.NEUTRO -> EstadoDesconocido
}
