package com.upb.personajes.data.remote

import com.upb.personajes.model.Pagina
import com.upb.personajes.model.PersonajeRickAndMorty
import com.upb.personajes.model.Resultado
import retrofit2.HttpException

/**
 * Única clase que llama a la Rick and Morty API (manual, regla de oro).
 * Convierte DTO → modelo y traduce los errores a [Resultado].
 */
class RickAndMortyRemoteRepository(
    private val api: ApiRemota<RickAndMortyApi> = ApiService.rickAndMorty,
) {

    suspend fun obtenerPagina(pagina: Int, nombre: String?): Resultado<Pagina<PersonajeRickAndMorty>> =
        llamarApi(api.nombre) {
            try {
                api.servicio.obtenerPersonajes(pagina, nombre?.takeIf { it.isNotBlank() }).aPagina(pagina)
            } catch (e: HttpException) {
                // Esta API responde 404 cuando una búsqueda no tiene resultados: no es un error.
                if (e.code() == 404) Pagina.vacia(pagina) else throw e
            }
        }

    suspend fun obtenerPersonaje(id: Int): Resultado<PersonajeRickAndMorty> =
        llamarApi(api.nombre) {
            try {
                api.servicio.obtenerPersonaje(id).aModelo()
            } catch (e: HttpException) {
                if (e.code() == 404) throw NoEncontradoException("Rick and Morty no tiene un personaje con id $id.") else throw e
            }
        }
}
