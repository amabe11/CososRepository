package com.upb.personajes.data.local

import android.content.Context
import com.google.gson.Gson
import com.upb.personajes.data.remote.AttackOnTitanRespuestaDto
import com.upb.personajes.data.remote.RickAndMortyRespuestaDto
import com.upb.personajes.data.remote.aPagina
import com.upb.personajes.model.FuenteApi
import com.upb.personajes.model.OrigenDatos
import com.upb.personajes.model.Pagina
import com.upb.personajes.model.Personaje
import com.upb.personajes.model.Resultado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Datos guardados dentro de la app (`app/src/main/assets`): la página 1 de cada API.
 * Se usan como respaldo cuando no hay internet.
 *
 * Usa los mismos DTO y el mismo mapeo que la API en línea, porque los archivos tienen
 * exactamente el mismo formato que la respuesta de la API.
 *
 * @param leerArchivo cómo leer un archivo por nombre. En la app lee de `assets`; en las pruebas, del disco.
 */
class PersonajesLocalRepository(private val leerArchivo: (String) -> String) {

    constructor(context: Context) : this({ nombre ->
        context.assets.open(nombre).bufferedReader().use { it.readText() }
    })

    private val gson = Gson()

    /** Página 1 del archivo, filtrada por nombre en memoria (el archivo no sabe filtrar). */
    suspend fun obtenerPagina(fuente: FuenteApi, nombre: String?): Resultado<Pagina<Personaje>> =
        leer {
            val todos = leerTodos(fuente)
            val filtro = nombre?.trim().orEmpty()
            val encontrados = todos.filter { filtro.isEmpty() || it.nombre.contains(filtro, ignoreCase = true) }
            Pagina(
                personajes = encontrados,
                numero = 1,
                totalPaginas = if (encontrados.isEmpty()) 0 else 1,
                total = encontrados.size,
                origen = OrigenDatos.ARCHIVO_LOCAL,
            )
        }

    suspend fun obtenerPersonaje(fuente: FuenteApi, id: Int): Resultado<Personaje> =
        when (val pagina = leer { leerTodos(fuente).firstOrNull { it.id == id } }) {
            is Resultado.Exito -> pagina.datos?.let { Resultado.Exito(it) }
                ?: Resultado.Error("El personaje #$id no está en los datos guardados.", sinConexion = true)
            is Resultado.Error -> pagina
        }

    private fun leerTodos(fuente: FuenteApi): List<Personaje> = when (fuente) {
        FuenteApi.RICK_AND_MORTY ->
            gson.fromJson(leerArchivo(ARCHIVO_RICK_AND_MORTY), RickAndMortyRespuestaDto::class.java)
                .aPagina(1, OrigenDatos.ARCHIVO_LOCAL).personajes
        FuenteApi.ATTACK_ON_TITAN ->
            gson.fromJson(leerArchivo(ARCHIVO_ATTACK_ON_TITAN), AttackOnTitanRespuestaDto::class.java)
                .aPagina(1, OrigenDatos.ARCHIVO_LOCAL).personajes
    }

    /** Lee en un hilo de entrada/salida para no congelar la pantalla. */
    private suspend fun <T> leer(bloque: () -> T): Resultado<T> = withContext(Dispatchers.IO) {
        try {
            Resultado.Exito(bloque())
        } catch (e: Exception) {
            Resultado.Error("No hay conexión y no se pudieron leer los datos guardados.", sinConexion = true)
        }
    }

    companion object {
        const val ARCHIVO_RICK_AND_MORTY = "character_rick&morty.json"
        const val ARCHIVO_ATTACK_ON_TITAN = "characters_aot.json"
    }
}
