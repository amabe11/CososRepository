package com.upb.personajes.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Botones Anterior / Siguiente con "página X de Y" en medio. */
@Composable
fun BarraPaginacion(
    pagina: Int,
    totalPaginas: Int,
    hayAnterior: Boolean,
    haySiguiente: Boolean,
    onAnterior: () -> Unit,
    onSiguiente: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        OutlinedButton(onClick = onAnterior, enabled = hayAnterior) { Text("‹ Anterior") }
        Text(
            text = if (totalPaginas == 0) "—" else "$pagina / $totalPaginas",
            style = MaterialTheme.typography.titleSmall,
        )
        OutlinedButton(onClick = onSiguiente, enabled = haySiguiente) { Text("Siguiente ›") }
    }
}
