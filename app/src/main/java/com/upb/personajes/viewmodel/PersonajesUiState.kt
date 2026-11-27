package com.upb.personajes.viewmodel

import com.upb.personajes.model.FuenteApi

/**
 * Todo lo que necesita la pantalla de lista, en un solo objeto inmutable (manual, paso 9).
 * La pantalla lo lee; solo el ViewModel crea uno nuevo con `copy(...)`.
 */
data class PersonajesUiState(
    val fuente: FuenteApi,
    val cargando: Boolean = false,
    val personajes: List<PersonajeUi> = emptyList(),
    val pagina: Int = 1,
    val totalPaginas: Int = 0,
    val total: Int = 0,
    /** Lo que el usuario está escribiendo. */
    val textoBusqueda: String = "",
    /** La búsqueda que produjo la lista actual. */
    val busquedaAplicada: String = "",
    val error: String? = null,
    val datosLocales: Boolean = false,
) {
    val hayAnterior: Boolean get() = !cargando && pagina > 1 && totalPaginas > 0
    val haySiguiente: Boolean get() = !cargando && pagina < totalPaginas
    val sinResultados: Boolean get() = !cargando && error == null && personajes.isEmpty() && totalPaginas == 0

    val resumen: String
        get() = when {
            totalPaginas == 0 -> ""
            busquedaAplicada.isNotEmpty() -> "$total resultados para «$busquedaAplicada» · página $pagina de $totalPaginas"
            else -> "$total personajes · página $pagina de $totalPaginas"
        }
}
