package com.upb.personajes.data.remote

import com.upb.personajes.respuestaJson
import kotlinx.coroutines.runBlocking
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

/** Valores globales vs. valores propios de cada API en [ApiService]. */
class ApiServiceTest {

    private lateinit var servidorRm: MockWebServer
    private lateinit var servidorAot: MockWebServer

    private val vaciaRm = """{"info":{"count":0,"pages":0,"next":null,"prev":null},"results":[]}"""
    private val vaciaAot = """{"info":{"count":0,"pages":0,"next_page":null,"prev_page":null}}"""

    @Before
    fun preparar() {
        servidorRm = MockWebServer().apply { start() }
        servidorAot = MockWebServer().apply { start() }
        ApiService.restablecer()
        ApiService.configurarGlobal { copy(nivelRegistro = HttpLoggingInterceptor.Level.NONE) }
        ApiService.rickAndMorty.configurar { copy(baseUrl = servidorRm.url("/api/").toString()) }
        ApiService.attackOnTitan.configurar { copy(baseUrl = servidorAot.url("/").toString()) }
    }

    @After
    fun cerrar() {
        servidorRm.shutdown()
        servidorAot.shutdown()
        ApiService.restablecer()
    }

    private fun llamarALasDos() = runBlocking {
        servidorRm.enqueue(respuestaJson(vaciaRm))
        servidorAot.enqueue(respuestaJson(vaciaAot))
        ApiService.rickAndMorty.servicio.obtenerPersonajes(pagina = 1)
        ApiService.attackOnTitan.servicio.obtenerPersonajes(pagina = 1)
        servidorRm.takeRequest() to servidorAot.takeRequest()
    }

    @Test
    fun cada_api_arma_su_propia_direccion() = runBlocking {
        servidorRm.enqueue(respuestaJson(vaciaRm))
        servidorAot.enqueue(respuestaJson(vaciaAot))

        ApiService.rickAndMorty.servicio.obtenerPersonajes(pagina = 2, nombre = "rick sanchez")
        ApiService.attackOnTitan.servicio.obtenerPersonajes(pagina = 3)

        assertEquals("/api/character?page=2&name=rick%20sanchez", servidorRm.takeRequest().path)
        // nombre = null: Retrofit no agrega el parámetro
        assertEquals("/characters?page=3", servidorAot.takeRequest().path)
    }

    @Test
    fun un_valor_global_llega_a_las_dos_apis() {
        ApiService.configurarGlobal {
            copy(agenteDeUsuario = "Global/2.0", cabeceras = mapOf("X-Curso" to "Programacion III"))
        }

        val (rm, aot) = llamarALasDos()

        assertEquals("Global/2.0", rm.getHeader("User-Agent"))
        assertEquals("Global/2.0", aot.getHeader("User-Agent"))
        assertEquals("Programacion III", rm.getHeader("X-Curso"))
        assertEquals("Programacion III", aot.getHeader("X-Curso"))
    }

    @Test
    fun el_valor_propio_de_una_api_no_afecta_a_la_otra() {
        ApiService.configurarGlobal { copy(cabeceras = mapOf("X-Curso" to "global", "X-Global" to "si")) }
        ApiService.attackOnTitan.configurar {
            copy(agenteDeUsuario = "Solo-AoT/1.0", cabeceras = mapOf("X-Curso" to "aot"))
        }

        val (rm, aot) = llamarALasDos()

        // Rick and Morty: todo global
        assertEquals(ApiService.global.agenteDeUsuario, rm.getHeader("User-Agent"))
        assertEquals("global", rm.getHeader("X-Curso"))
        // Attack on Titan: sus valores ganan, y recibe también las cabeceras globales que no pisa
        assertEquals("Solo-AoT/1.0", aot.getHeader("User-Agent"))
        assertEquals("aot", aot.getHeader("X-Curso"))
        assertEquals("si", aot.getHeader("X-Global"))
        assertNull(rm.getHeader("X-Inexistente"))
    }

    @Test
    fun el_tiempo_de_espera_usa_el_propio_o_el_global() {
        ApiService.configurarGlobal { copy(tiempoEsperaSegundos = 30) }
        ApiService.attackOnTitan.configurar { copy(tiempoEsperaSegundos = 60) }

        assertEquals(30, ApiService.rickAndMorty.configuracionEfectiva.tiempoEsperaSegundos)
        assertEquals(60, ApiService.attackOnTitan.configuracionEfectiva.tiempoEsperaSegundos)

        ApiService.attackOnTitan.configurar { copy(tiempoEsperaSegundos = null) } // vuelve al global
        assertEquals(30, ApiService.attackOnTitan.configuracionEfectiva.tiempoEsperaSegundos)
    }

    @Test
    fun cambiar_una_api_solo_recrea_su_servicio_y_el_global_recrea_ambos() {
        val rm1 = ApiService.rickAndMorty.servicio
        val aot1 = ApiService.attackOnTitan.servicio
        assertSame("se reutiliza la instancia", rm1, ApiService.rickAndMorty.servicio)

        ApiService.attackOnTitan.configurar { copy(tiempoEsperaSegundos = 5) }
        assertSame("Rick and Morty no se toca", rm1, ApiService.rickAndMorty.servicio)
        assertNotSame("Attack on Titan se recrea", aot1, ApiService.attackOnTitan.servicio)

        val rm2 = ApiService.rickAndMorty.servicio
        val aot2 = ApiService.attackOnTitan.servicio
        ApiService.configurarGlobal { copy(tiempoEsperaSegundos = 20) }
        assertNotSame(rm2, ApiService.rickAndMorty.servicio)
        assertNotSame(aot2, ApiService.attackOnTitan.servicio)
    }

    @Test(expected = IllegalArgumentException::class)
    fun una_baseUrl_sin_barra_final_se_rechaza() {
        ApiService.rickAndMorty.configurar { copy(baseUrl = "https://rickandmortyapi.com/api") }
    }

    @Test
    fun restablecer_vuelve_a_los_valores_de_fabrica() {
        ApiService.configurarGlobal { copy(tiempoEsperaSegundos = 99) }
        ApiService.restablecer()

        assertEquals("https://rickandmortyapi.com/api/", ApiService.rickAndMorty.configuracion.baseUrl)
        assertEquals("https://api.attackontitanapi.com/", ApiService.attackOnTitan.configuracion.baseUrl)
        assertEquals(15, ApiService.global.tiempoEsperaSegundos)
    }
}
