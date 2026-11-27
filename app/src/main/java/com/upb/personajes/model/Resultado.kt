package com.upb.personajes.model

/**
 * "Salió bien" o "salió mal" (MANUAL-RETROFIT, paso 8).
 * El repositorio devuelve esto en lugar de lanzar excepciones: la pantalla solo decide qué dibujar.
 */
sealed class Resultado<out T> {
    data class Exito<out T>(val datos: T) : Resultado<T>()

    /** @param sinConexion `true` si falló la red (permite usar los datos locales como respaldo). */
    data class Error(val mensaje: String, val sinConexion: Boolean = false) : Resultado<Nothing>()
}
