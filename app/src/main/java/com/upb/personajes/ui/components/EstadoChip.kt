package com.upb.personajes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.upb.personajes.ui.theme.color
import com.upb.personajes.viewmodel.TonoEstado

/** Etiqueta redondeada con el estado: "Vivo", "Muerto", "Desconocido". */
@Composable
fun EstadoChip(texto: String, tono: TonoEstado, modifier: Modifier = Modifier) {
    Text(
        text = texto,
        color = Color.White,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .background(tono.color(), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 3.dp),
    )
}
