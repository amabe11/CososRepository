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
import com.upb.personajes.model.OrigenDatos
import com.upb.personajes.model.Resultado
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado y acciones de la lista de personajes de UNA API. Hay una instancia por pestaña.
 *
 * La pantalla observa [uiState] y llama a los eventos (buscar, paginaSiguiente…).
 * El ViewModel no sabe que existen Retrofit ni internet: solo habla con el [GestorDePersonajes].
 */
class PersonajesViewModel(
    private val fuente: FuenteApi,
    private val gestor: GestorDePersonajes,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PersonajesUiState(fuente))
    val uiState: StateFlow<PersonajesUiState> = _uiState.asStateFlow()

    private var cargaActual: Job? = null

    init {
        cargar(pagina = 1, busqueda = "")
    }

    // ─────────────────────────────── Eventos de la pantalla

    fun onTextoBusquedaCambia(texto: String) = _uiState.update { it.copy(textoBusqueda = texto) }

    fun buscar() = cargar(pagina = 1, busqueda = _uiState.value.textoBusqueda.trim())

    fun limpiarBusqueda() {
        _uiState.update { it.copy(textoBusqueda = "") }
        cargar(pagina = 1, busqueda = "")
    }

    fun paginaSiguiente() {
        if (_uiState.value.haySiguiente) cargar(_uiState.value.pagina + 1, _uiState.value.busquedaAplicada)
    }

    fun paginaAnterior() {
        if (_uiState.value.hayAnterior) cargar(_uiState.value.pagina - 1, _uiState.value.busquedaAplicada)
    }

    /** También sirve como "Reintentar". */
    fun recargar() = cargar(_uiState.value.pagina, _uiState.value.busquedaAplicada)

    // ─────────────────────────────── Carga

    private fun cargar(pagina: Int, busqueda: String) {
        cargaActual?.cancel() // si el usuario pide otra página, la anterior ya no interesa
        cargaActual = viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            when (val resultado = gestor.obtenerPagina(fuente, pagina, busqueda.ifEmpty { null })) {
                is Resultado.Exito -> {
                    val datos = resultado.datos
                    _uiState.update {
                        it.copy(
                            cargando = false,
                            personajes = datos.personajes.map { p -> p.aItemUi() },
                            pagina = datos.numero,
                            totalPaginas = datos.totalPaginas,
                            total = datos.total,
                            busquedaAplicada = busqueda,
                            datosLocales = datos.origen == OrigenDatos.ARCHIVO_LOCAL,
                        )
                    }
                }
                // Si falla se conserva la lista anterior: el error se muestra encima.
                is Resultado.Error -> _uiState.update { it.copy(cargando = false, error = resultado.mensaje) }
            }
        }
    }

    companion object {
        fun fabrica(fuente: FuenteApi): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as PersonajesApp
                PersonajesViewModel(fuente, app.gestor)
            }
        }
    }
}
