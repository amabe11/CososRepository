package com.upb.personajes.data.remote

import com.google.gson.Gson
import com.upb.personajes.model.Pagina
import com.upb.personajes.model.PersonajeAttackOnTitan
import com.upb.personajes.model.Resultado

/**
 * Única clase que llama a la Attack on Titan API (manual, regla de oro).
 * Convierte DTO → modelo y traduce los errores a [Resultado].
 */
class AttackOnTitanRemoteRepository(
    private val api: ApiRemota<AttackOnTitanApi> = ApiService.attackOnTitan,
) {
    private val gson = Gson()

    suspend fun obtenerPagina(pagina: Int, nombre: String?): Resultado<Pagina<PersonajeAttackOnTitan>> =
        llamarApi(api.nombre) {
            api.servicio.obtenerPersonajes(pagina, nombre?.takeIf { it.isNotBlank() }).aPagina(pagina)
        }

    suspend fun obtenerPersonaje(id: Int): Resultado<PersonajeAttackOnTitan> =
        llamarApi(api.nombre) {
            val respuesta = api.servicio.obtenerPersonaje(id)
            // Si el id no existe, esta API responde 200 con "[]" en lugar de un objeto.
            if (!respuesta.isJsonObject) {
                throw NoEncontradoException("Attack on Titan no tiene un personaje con id $id.")
            }
            gson.fromJson(respuesta, AttackOnTitanPersonajeDto::class.java).aModelo()
        }
}
