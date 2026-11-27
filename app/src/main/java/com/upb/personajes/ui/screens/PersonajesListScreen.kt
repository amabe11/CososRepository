package com.upb.personajes.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.upb.personajes.model.FuenteApi
import com.upb.personajes.ui.components.BarraPaginacion
import com.upb.personajes.ui.components.CampoBusqueda
import com.upb.personajes.ui.components.MensajeDeError
import com.upb.personajes.ui.components.PersonajeCard
import com.upb.personajes.ui.theme.AvisoLocal
import com.upb.personajes.viewmodel.PersonajesUiState
import com.upb.personajes.viewmodel.PersonajesViewModel

/**
 * Lista de personajes de una API. Obtiene su ViewModel (uno por API, gracias a `key`)
 * y le pasa el estado a [ContenidoLista], que solo dibuja.
 */
@Composable
fun PersonajesListScreen(
    fuente: FuenteApi,
    onAbrirDetalle: (FuenteApi, Int) -> Unit,
    viewModel: PersonajesViewModel = viewModel(key = fuente.name, factory = PersonajesViewModel.fabrica(fuente)),
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    ContenidoLista(
        estado = estado,
        onTextoBusquedaCambia = viewModel::onTextoBusquedaCambia,
        onBuscar = viewModel::buscar,
        onLimpiar = viewModel::limpiarBusqueda,
        onReintentar = viewModel::recargar,
        onAnterior = viewModel::paginaAnterior,
        onSiguiente = viewModel::paginaSiguiente,
        onAbrirDetalle = { id -> onAbrirDetalle(fuente, id) },
    )
}

@Composable
private fun ContenidoLista(
    estado: PersonajesUiState,
    onTextoBusquedaCambia: (String) -> Unit,
    onBuscar: () -> Unit,
    onLimpiar: () -> Unit,
    onReintentar: () -> Unit,
    onAnterior: () -> Unit,
    onSiguiente: () -> Unit,
    onAbrirDetalle: (Int) -> Unit,
) {
    val lista = rememberLazyListState()
    // Al cambiar de página o de búsqueda, volver arriba.
    LaunchedEffect(estado.pagina, estado.busquedaAplicada) { lista.scrollToItem(0) }

    Column(Modifier.fillMaxSize()) {
        CampoBusqueda(
            texto = estado.textoBusqueda,
            onTextoCambia = onTextoBusquedaCambia,
            onBuscar = onBuscar,
            onLimpiar = onLimpiar,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp),
        )
        if (estado.datosLocales) {
            Text(
                text = "Sin conexión: mostrando los datos guardados en la app (solo la página 1).",
                color = AvisoLocal,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
        if (estado.resumen.isNotEmpty()) {
            Text(
                text = estado.resumen,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            )
        }
        Box(Modifier.fillMaxWidth().padding(top = 2.dp)) {
            if (estado.cargando && estado.personajes.isNotEmpty()) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }
        }
        // Error con datos en pantalla: se avisa arriba y se conserva la lista.
        if (estado.error != null && estado.personajes.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = estado.error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onReintentar) { Text("Reintentar") }
            }
        }

        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            when {
                // Tres estados, tres dibujos (manual, paso 10)
                estado.cargando && estado.personajes.isEmpty() -> CircularProgressIndicator()
                estado.error != null && estado.personajes.isEmpty() ->
                    MensajeDeError(mensaje = estado.error, onReintentar = onReintentar)
                estado.sinResultados -> Text(
                    text = if (estado.busquedaAplicada.isEmpty()) "La API no devolvió personajes."
                    else "No hay personajes que coincidan con «${estado.busquedaAplicada}».",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp),
                )
                else -> LazyColumn(
                    state = lista,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(estado.personajes, key = { it.id }) { personaje ->
                        PersonajeCard(personaje = personaje, onClick = { onAbrirDetalle(personaje.id) })
                    }
                }
            }
        }

        HorizontalDivider()
        BarraPaginacion(
            pagina = estado.pagina,
            totalPaginas = estado.totalPaginas,
            hayAnterior = estado.hayAnterior,
            haySiguiente = estado.haySiguiente,
            onAnterior = onAnterior,
            onSiguiente = onSiguiente,
            modifier = Modifier.navigationBarsPadding(),
        )
    }
}
