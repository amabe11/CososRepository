package com.upb.personajes.data.remote

import com.upb.personajes.model.Resultado
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

/** La API respondió bien, pero lo pedido no existe. */
class NoEncontradoException(mensaje: String) : Exception(mensaje)

/**
 * Ejecuta una llamada a una API y traduce las excepciones a un [Resultado]
 * (MANUAL-RETROFIT, pasos 8 y 14). Lo comparten los repositorios remotos de ambas APIs.
 */
internal suspend fun <T> llamarApi(nombreApi: String, llamada: suspend () -> T): Resultado<T> =
    try {
        Resultado.Exito(llamada())
    } catch (e: CancellationException) {
        throw e // cancelar la corrutina no es un error
    } catch (e: NoEncontradoException) {
        Resultado.Error(e.message ?: "No se encontró lo que pediste.")
    } catch (e: HttpException) {
        Resultado.Error(mensajeHttp(nombreApi, e.code()))
    } catch (e: IOException) {
        Resultado.Error("No hay conexión con $nombreApi. Revisa tu red e inténtalo de nuevo.", sinConexion = true)
    } catch (e: Exception) {
        Resultado.Error("$nombreApi respondió algo inesperado.")
    }

private fun mensajeHttp(nombreApi: String, codigo: Int): String = when (codigo) {
    404 -> "$nombreApi no tiene lo que pediste (código 404)."
    429 -> "$nombreApi está limitando las peticiones (código 429). Espera unos segundos y reintenta."
    in 500..599 -> "$nombreApi tiene un problema interno (código $codigo). Intenta más tarde."
    else -> "$nombreApi respondió con un error (código $codigo)."
}
