package com.upb.personajes.viewmodel

import com.upb.personajes.ARCHIVO_RM
import com.upb.personajes.attackOnTitanDePrueba
import com.upb.personajes.data.remote.AttackOnTitanRemoteRepository
import com.upb.personajes.data.remote.RickAndMortyRemoteRepository
import com.upb.personajes.leerAsset
import com.upb.personajes.model.FuenteApi
import com.upb.personajes.model.GestorDePersonajes
import com.upb.personajes.respuestaJson
import com.upb.personajes.rickAndMortyDePrueba
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** El ViewModel completo (con Gestor, repositorio y Retrofit reales) contra un servidor falso. */
@OptIn(ExperimentalCoroutinesApi::class)
class PersonajesViewModelTest {

    private lateinit var servidor: MockWebServer
    private lateinit var gestor: GestorDePersonajes

    @Before
    fun preparar() {
        Dispatchers.setMain(Dispatchers.Unconfined) // viewModelScope usa Main; en la JVM no existe
        servidor = MockWebServer().apply { start() }
        gestor = GestorDePersonajes(
            rickAndMorty = RickAndMortyRemoteRepository(rickAndMortyDePrueba(servidor)),
            attackOnTitan = AttackOnTitanRemoteRepository(attackOnTitanDePrueba(servidor)),
        )
    }

    @After
    fun cerrar() {
        servidor.shutdown()
        Dispatchers.resetMain()
    }

    private fun PersonajesViewModel.esperarCarga(): PersonajesUiState = runBlocking {
        withTimeout(5_000) { uiState.first { !it.cargando } }
    }

    @Test
    fun al_abrir_carga_la_pagina_1() {
        servidor.enqueue(respuestaJson(leerAsset(ARCHIVO_RM)))
        val vm = PersonajesViewModel(FuenteApi.RICK_AND_MORTY, gestor)

        val estado = vm.esperarCarga()
        assertEquals(20, estado.personajes.size)
        assertEquals("Rick Sanchez", estado.personajes[0].nombre)
        assertEquals("Vivo", estado.personajes[0].estado)
        assertEquals(42, estado.totalPaginas)
        assertTrue(estado.haySiguiente && !estado.hayAnterior)
        assertEquals("/api/character?page=1", servidor.takeRequest().path)
    }

    @Test
    fun buscar_y_paginar_arman_la_peticion_correcta() {
        servidor.enqueue(respuestaJson(leerAsset(ARCHIVO_RM)))
        val vm = PersonajesViewModel(FuenteApi.RICK_AND_MORTY, gestor)
        vm.esperarCarga()
        servidor.takeRequest()

        servidor.enqueue(respuestaJson(leerAsset(ARCHIVO_RM)))
        vm.onTextoBusquedaCambia("  rick ")
        vm.buscar()
        assertEquals("rick", vm.esperarCarga().busquedaAplicada)
        assertEquals("/api/character?page=1&name=rick", servidor.takeRequest().path)

        servidor.enqueue(respuestaJson(leerAsset(ARCHIVO_RM)))
        vm.paginaSiguiente()
        vm.esperarCarga()
        assertEquals("la búsqueda se mantiene al paginar", "/api/character?page=2&name=rick", servidor.takeRequest().path)
    }

    @Test
    fun un_error_conserva_la_lista_y_se_puede_reintentar() {
        servidor.enqueue(respuestaJson(leerAsset(ARCHIVO_RM)))
        val vm = PersonajesViewModel(FuenteApi.RICK_AND_MORTY, gestor)
        vm.esperarCarga()

        servidor.enqueue(respuestaJson("{}", 500))
        vm.paginaSiguiente()
        val conError = vm.esperarCarga()
        assertTrue(conError.error!!.contains("500"))
        assertEquals(20, conError.personajes.size)

        servidor.enqueue(respuestaJson(leerAsset(ARCHIVO_RM)))
        vm.recargar()
        assertEquals(null, vm.esperarCarga().error)
    }
}
