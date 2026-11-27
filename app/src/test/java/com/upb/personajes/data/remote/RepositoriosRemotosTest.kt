package com.upb.personajes.data.remote

import com.google.gson.JsonParser
import com.upb.personajes.ARCHIVO_AOT
import com.upb.personajes.ARCHIVO_RM
import com.upb.personajes.attackOnTitanDePrueba
import com.upb.personajes.leerAsset
import com.upb.personajes.model.Resultado
import com.upb.personajes.respuestaJson
import com.upb.personajes.rickAndMortyDePrueba
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Cada caso de error del manual (paso 14), simulado con un servidor falso. */
class RepositoriosRemotosTest {

    private lateinit var servidor: MockWebServer
    private lateinit var rickAndMorty: RickAndMortyRemoteRepository
    private lateinit var attackOnTitan: AttackOnTitanRemoteRepository

    @Before
    fun preparar() {
        servidor = MockWebServer().apply { start() }
        rickAndMorty = RickAndMortyRemoteRepository(rickAndMortyDePrueba(servidor))
        attackOnTitan = AttackOnTitanRemoteRepository(attackOnTitanDePrueba(servidor))
    }

    @After
    fun cerrar() = servidor.shutdown()

    @Test
    fun pagina_correcta() = runBlocking {
        servidor.enqueue(respuestaJson(leerAsset(ARCHIVO_RM)))
        val resultado = rickAndMorty.obtenerPagina(1, null)
        resultado as Resultado.Exito
        assertEquals(20, resultado.datos.personajes.size)
    }

    @Test
    fun rick_and_morty_busqueda_sin_resultados_responde_404_y_no_es_error() = runBlocking {
        servidor.enqueue(respuestaJson("""{"error":"There is nothing here"}""", 404))
        val resultado = rickAndMorty.obtenerPagina(1, "zzz")
        resultado as Resultado.Exito
        assertTrue(resultado.datos.personajes.isEmpty())
    }

    @Test
    fun rick_and_morty_detalle_inexistente() = runBlocking {
        servidor.enqueue(respuestaJson("""{"error":"Character not found"}""", 404))
        val resultado = rickAndMorty.obtenerPersonaje(99999)
        resultado as Resultado.Error
        assertTrue(resultado.mensaje.contains("99999"))
    }

    @Test
    fun attack_on_titan_detalle_existente_llega_como_objeto() = runBlocking {
        val armin = JsonParser().parse(leerAsset(ARCHIVO_AOT)).asJsonObject.getAsJsonArray("results")[0]
        servidor.enqueue(respuestaJson(armin.toString()))
        val resultado = attackOnTitan.obtenerPersonaje(1)
        resultado as Resultado.Exito
        assertEquals("Armin Arlelt", resultado.datos.nombre)
        assertEquals("/characters/1", servidor.takeRequest().path)
    }

    @Test
    fun attack_on_titan_detalle_inexistente_responde_un_arreglo_vacio() = runBlocking {
        servidor.enqueue(respuestaJson("[]"))
        val resultado = attackOnTitan.obtenerPersonaje(9999)
        resultado as Resultado.Error
        assertTrue(resultado.mensaje.contains("9999"))
    }

    @Test
    fun error_del_servidor() = runBlocking {
        servidor.enqueue(respuestaJson("{}", 500))
        val resultado = attackOnTitan.obtenerPagina(1, null)
        resultado as Resultado.Error
        assertTrue(resultado.mensaje.contains("500"))
        assertTrue(!resultado.sinConexion)
    }

    @Test
    fun limite_de_peticiones() = runBlocking {
        servidor.enqueue(respuestaJson("{}", 429))
        val resultado = rickAndMorty.obtenerPagina(1, null)
        resultado as Resultado.Error
        assertTrue(resultado.mensaje.contains("429"))
    }

    @Test
    fun json_que_no_coincide_con_el_dto() = runBlocking {
        servidor.enqueue(respuestaJson("""{"info": "esto no es un objeto"}"""))
        val resultado = rickAndMorty.obtenerPagina(1, null)
        resultado as Resultado.Error
        assertTrue(resultado.mensaje.contains("inesperado"))
    }

    @Test
    fun servidor_caido_es_sin_conexion() = runBlocking {
        servidor.shutdown()
        val resultado = rickAndMorty.obtenerPagina(1, null)
        resultado as Resultado.Error
        assertTrue(resultado.sinConexion)
    }
}
