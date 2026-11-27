package com.upb.personajes.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import com.upb.personajes.model.FuenteApi

/** Pantalla principal: una pestaña por API. Cada pestaña tiene su propio ViewModel. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InicioScreen(onAbrirDetalle: (FuenteApi, Int) -> Unit) {
    var pestana by rememberSaveable { mutableIntStateOf(0) }
    val fuentes = FuenteApi.entries

    Scaffold(topBar = { TopAppBar(title = { Text("Personajes MVVM") }) }) { relleno ->
        Column(Modifier.padding(top = relleno.calculateTopPadding())) {
            PrimaryTabRow(selectedTabIndex = pestana) {
                fuentes.forEachIndexed { indice, fuente ->
                    Tab(
                        selected = pestana == indice,
                        onClick = { pestana = indice },
                        text = { Text(fuente.titulo) },
                    )
                }
            }
            PersonajesListScreen(fuente = fuentes[pestana], onAbrirDetalle = onAbrirDetalle)
        }
    }
}
