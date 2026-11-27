package com.upb.personajes

import com.upb.personajes.data.remote.ApiRemota
import com.upb.personajes.data.remote.AttackOnTitanApi
import com.upb.personajes.data.remote.ConfiguracionApi
import com.upb.personajes.data.remote.ConfiguracionGlobal
import com.upb.personajes.data.remote.RickAndMortyApi
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.io.File

/** Lee un JSON de app/src/main/assets (las pruebas se ejecutan desde la carpeta app/). */
fun leerAsset(nombre: String): String = File("src/main/assets/$nombre").readText()

const val ARCHIVO_RM = "character_rick&morty.json"
const val ARCHIVO_AOT = "characters_aot.json"

fun respuestaJson(cuerpo: String, codigo: Int = 200): MockResponse =
    MockResponse().setResponseCode(codigo).addHeader("Content-Type", "application/json").setBody(cuerpo)

private val globalDePrueba = ConfiguracionGlobal(nivelRegistro = HttpLoggingInterceptor.Level.NONE)

/**
 * APIs propias para cada prueba, apuntando a un servidor falso. No tocan el objeto global
 * ApiService, así las pruebas no se afectan entre sí.
 */
fun rickAndMortyDePrueba(servidor: MockWebServer) = ApiRemota(
    nombre = "Rick and Morty",
    configuracionDeFabrica = ConfiguracionApi(baseUrl = servidor.url("/api/").toString()),
    tipoServicio = RickAndMortyApi::class.java,
    global = { globalDePrueba },
)

fun attackOnTitanDePrueba(servidor: MockWebServer) = ApiRemota(
    nombre = "Attack on Titan",
    configuracionDeFabrica = ConfiguracionApi(baseUrl = servidor.url("/").toString()),
    tipoServicio = AttackOnTitanApi::class.java,
    global = { globalDePrueba },
)
