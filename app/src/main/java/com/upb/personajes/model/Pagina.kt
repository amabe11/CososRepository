package com.upb.personajes.model

/** De dónde salieron los datos de una página. */
enum class OrigenDatos { API, ARCHIVO_LOCAL }

/**
 * Una página de resultados, igual para las dos APIs.
 * Rick and Morty pagina con `info.next/prev` y Attack on Titan con `info.next_page/prev_page`;
 * el mapeo traduce ambos formatos a esta clase.
 */
data class Pagina<out T>(
    val personajes: List<T>,
    val numero: Int,
    val totalPaginas: Int,
    val total: Int,
    val origen: OrigenDatos,
) {
    val hayAnterior: Boolean get() = numero > 1 && totalPaginas > 0
    val haySiguiente: Boolean get() = numero < totalPaginas

    companion object {
        fun vacia(numero: Int, origen: OrigenDatos = OrigenDatos.API): Pagina<Nothing> =
            Pagina(emptyList(), numero, totalPaginas = 0, total = 0, origen = origen)
    }
}
