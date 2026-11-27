package com.upb.personajes.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Fabrica las instancias de Retrofit (en el manual se llama `FabricaRetrofit`, paso 7).
 * La usa [ApiService]; el resto de la app no la toca.
 */
object RetrofitInstance {

    /**
     * Cliente base compartido. Cada API crea el suyo con `newBuilder()`, así tienen tiempos y
     * cabeceras independientes pero reutilizan las mismas conexiones e hilos.
     */
    private val clienteBase: OkHttpClient by lazy { OkHttpClient() }

    fun crear(config: ConfiguracionEfectiva): Retrofit =
        Retrofit.Builder()
            .baseUrl(config.baseUrl)
            .client(clienteHttp(config.tiempoEsperaSegundos, config.agenteDeUsuario, config.cabeceras, config.nivelRegistro))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    /** Cliente para descargar imágenes con los valores globales (lo usa Coil). */
    fun clienteImagenes(): OkHttpClient {
        val global = ApiService.global
        return clienteHttp(global.tiempoEsperaSegundos, global.agenteDeUsuario, global.cabeceras, HttpLoggingInterceptor.Level.NONE)
    }

    private fun clienteHttp(
        tiempoEsperaSegundos: Long,
        agenteDeUsuario: String,
        cabeceras: Map<String, String>,
        nivelRegistro: HttpLoggingInterceptor.Level,
    ): OkHttpClient {
        val builder = clienteBase.newBuilder()
            .connectTimeout(tiempoEsperaSegundos, TimeUnit.SECONDS) // tiempo máximo para conectarse
            .readTimeout(tiempoEsperaSegundos, TimeUnit.SECONDS)    // tiempo máximo esperando la respuesta
            .writeTimeout(tiempoEsperaSegundos, TimeUnit.SECONDS)   // tiempo máximo enviando el cuerpo
            .addInterceptor { cadena ->
                val peticion = cadena.request().newBuilder()
                    .header("User-Agent", agenteDeUsuario)
                    .apply { cabeceras.forEach { (nombre, valor) -> header(nombre, valor) } }
                    .build()
                cadena.proceed(peticion)
            }
        if (nivelRegistro != HttpLoggingInterceptor.Level.NONE) {
            // Logcat, filtro "okhttp" (manual, paso 15)
            builder.addInterceptor(HttpLoggingInterceptor().apply { level = nivelRegistro })
        }
        return builder.build()
    }
}
