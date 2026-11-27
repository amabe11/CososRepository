package com.upb.personajes.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.upb.personajes.model.FuenteApi
import com.upb.personajes.ui.components.EstadoChip
import com.upb.personajes.ui.components.ImagenPersonaje
import com.upb.personajes.ui.components.MensajeDeError
import com.upb.personajes.viewmodel.DetalleViewModel
import com.upb.personajes.viewmodel.FichaUi

/** Ficha completa de un personaje. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonajeDetailScreen(
    fuente: FuenteApi,
    id: Int,
    onVolver: () -> Unit,
    viewModel: DetalleViewModel = viewModel(factory = DetalleViewModel.fabrica(fuente, id)),
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(estado.ficha?.nombre ?: fuente.titulo) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
    ) { relleno ->
        Box(Modifier.fillMaxSize().padding(relleno), contentAlignment = Alignment.Center) {
            val ficha = estado.ficha
            val error = estado.error
            when {
                estado.cargando -> CircularProgressIndicator()
                error != null -> MensajeDeError(mensaje = error, onReintentar = viewModel::cargar)
                ficha != null -> Ficha(ficha)
            }
        }
    }
}

@Composable
private fun Ficha(ficha: FichaUi) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .navigationBarsPadding(),
    ) {
        ImagenPersonaje(
            url = ficha.imagenUrl,
            nombre = ficha.nombre,
            tamanoIniciales = 64.sp,
            modifier = Modifier.fillMaxWidth().height(320.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(ficha.nombre, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(ficha.subtitulo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(8.dp))
        EstadoChip(ficha.estado, ficha.tono)
        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 12.dp)) {
            ficha.datos.forEach { dato ->
                Row {
                    Text(
                        text = dato.etiqueta,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(130.dp),
                    )
                    Text(dato.valor, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
