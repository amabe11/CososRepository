package com.upb.personajes.model

import com.upb.personajes.attackOnTitanDePrueba
import com.upb.personajes.data.local.PersonajesLocalRepository
import com.upb.personajes.data.remote.AttackOnTitanRemoteRepository
import com.upb.personajes.data.remote.RickAndMortyRemoteRepository
import com.upb.personajes.leerAsset
import com.upb.personajes.respuestaJson
import com.upb.personajes.rickAndMortyDePrueba
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** API en línea primero; sin conexión, los datos guardados en la app. */
class GestorDePersonajesTest {

    private lateinit var servidor: MockWebServer
    private lateinit var gestor: GestorDePersonajes

    @Before
    fun preparar() {
        servidor = MockWebServer().apply { start() }
        gestor = GestorDePersonajes(
            rickAndMorty = RickAndMortyRemoteRepository(rickAndMortyDePrueba(servidor)),
            attackOnTitan = AttackOnTitanRemoteRepository(attackOnTitanDePrueba(servidor)),
            local = PersonajesLocalRepository(::leerAsset),
        )
    }

    @After
    fun cerrar() = servidor.shutdown()

    @Test
    fun con_conexion_usa_la_api() = runBlocking {
        servidor.enqueue(respuestaJson(leerAsset(PersonajesLocalRepository.ARCHIVO_RICK_AND_MORTY)))
        val resultado = gestor.obtenerPagina(FuenteApi.RICK_AND_MORTY, 1, null) as Resultado.Exito
        assertEquals(OrigenDatos.API, resultado.datos.origen)
        assertEquals(42, resultado.datos.totalPaginas)
    }

    @Test
    fun sin_conexion_usa_el_archivo_local_y_filtra() = runBlocking {
        servidor.shutdown()

        val todos = gestor.obtenerPagina(FuenteApi.ATTACK_ON_TITAN, 1, null) as Resultado.Exito
        assertEquals(OrigenDatos.ARCHIVO_LOCAL, todos.datos.origen)
        assertEquals(20, todos.datos.personajes.size)
        assertTrue("sin conexión no hay página siguiente", !todos.datos.haySiguiente)

        val ackermann = gestor.obtenerPagina(FuenteApi.ATTACK_ON_TITAN, 1, "ACKERMANN") as Resultado.Exito
        assertTrue(ackermann.datos.personajes.isNotEmpty())
        assertTrue(ackermann.datos.personajes.all { it.nombre.contains("Ackermann") })
    }

    @Test
    fun sin_conexion_la_pagina_2_es_un_error() = runBlocking {
        servidor.shutdown()
        val resultado = gestor.obtenerPagina(FuenteApi.RICK_AND_MORTY, 2, null) as Resultado.Error
        assertTrue(resultado.mensaje.contains("página 1"))
    }

    @Test
    fun sin_conexion_el_detalle_sale_del_archivo_local() = runBlocking {
        servidor.shutdown()
        val levi = gestor.obtenerPersonaje(FuenteApi.ATTACK_ON_TITAN, 5) as Resultado.Exito
        assertEquals("Levi Ackermann", levi.datos.nombre)
    }
}
