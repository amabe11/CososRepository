package com.upb.personajes.model

import com.upb.personajes.data.local.PersonajesLocalRepository
import com.upb.personajes.data.remote.AttackOnTitanRemoteRepository
import com.upb.personajes.data.remote.RickAndMortyRemoteRepository

/**
 * Punto de entrada del Modelo para el ViewModel (como `GestorDeTareas` en el diagrama de capas).
 *
 * Decide de dónde salen los datos:
 * 1. Primero la API en línea (`data.remote`).
 * 2. Si no hay conexión, los JSON guardados en la app (`data.local`). Solo tienen la página 1.
 *
 * El ViewModel no sabe que existen Retrofit, internet ni archivos.
 */
class GestorDePersonajes(
    private val rickAndMorty: RickAndMortyRemoteRepository = RickAndMortyRemoteRepository(),
    private val attackOnTitan: AttackOnTitanRemoteRepository = AttackOnTitanRemoteRepository(),
    private val local: PersonajesLocalRepository? = null,
) {

    suspend fun obtenerPagina(fuente: FuenteApi, pagina: Int, nombre: String?): Resultado<Pagina<Personaje>> {
        val remoto: Resultado<Pagina<Personaje>> = when (fuente) {
            FuenteApi.RICK_AND_MORTY -> rickAndMorty.obtenerPagina(pagina, nombre)
            FuenteApi.ATTACK_ON_TITAN -> attackOnTitan.obtenerPagina(pagina, nombre)
        }
        if (remoto !is Resultado.Error || !remoto.sinConexion || local == null) {
            return remoto
        }
        if (pagina != 1) {
            return Resultado.Error("${remoto.mensaje} Sin conexión solo está disponible la página 1.", sinConexion = true)
        }
        return local.obtenerPagina(fuente, nombre)
    }

    suspend fun obtenerPersonaje(fuente: FuenteApi, id: Int): Resultado<Personaje> {
        val remoto: Resultado<Personaje> = when (fuente) {
            FuenteApi.RICK_AND_MORTY -> rickAndMorty.obtenerPersonaje(id)
            FuenteApi.ATTACK_ON_TITAN -> attackOnTitan.obtenerPersonaje(id)
        }
        if (remoto !is Resultado.Error || !remoto.sinConexion || local == null) {
            return remoto
        }
        val guardado = local.obtenerPersonaje(fuente, id)
        return if (guardado is Resultado.Exito) guardado else remoto
    }
}
