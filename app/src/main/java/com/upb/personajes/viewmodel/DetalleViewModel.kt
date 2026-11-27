package com.upb.personajes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.upb.personajes.PersonajesApp
import com.upb.personajes.model.FuenteApi
import com.upb.personajes.model.GestorDePersonajes
import com.upb.personajes.model.Resultado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estado de la pantalla de detalle. */
data class DetalleUiState(
    val cargando: Boolean = false,
    val ficha: FichaUi? = null,
    val error: String? = null,
)

/**
 * Carga un personaje por id con el endpoint de detalle (`@Path`, manual, ejercicio 1).
 */
class DetalleViewModel(
    private val fuente: FuenteApi,
    private val id: Int,
    private val gestor: GestorDePersonajes,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleUiState())
    val uiState: StateFlow<DetalleUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            when (val resultado = gestor.obtenerPersonaje(fuente, id)) {
                is Resultado.Exito -> _uiState.update { it.copy(cargando = false, ficha = resultado.datos.aFichaUi()) }
                is Resultado.Error -> _uiState.update { it.copy(cargando = false, error = resultado.mensaje) }
            }
        }
    }

    companion object {
        fun fabrica(fuente: FuenteApi, id: Int): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as PersonajesApp
                DetalleViewModel(fuente, id, app.gestor)
            }
        }
    }
}
