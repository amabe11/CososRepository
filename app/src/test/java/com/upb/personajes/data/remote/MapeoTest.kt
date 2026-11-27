package com.upb.personajes.data.remote

import com.google.gson.Gson
import com.upb.personajes.ARCHIVO_AOT
import com.upb.personajes.ARCHIVO_RM
import com.upb.personajes.leerAsset
import com.upb.personajes.model.EstadoVital
import com.upb.personajes.model.Genero
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/** JSON real (los archivos de assets) → DTO → modelo del dominio. */
class MapeoTest {

    private val gson = Gson()

    @Test
    fun rick_and_morty_se_mapea_completo() {
        val pagina = gson.fromJson(leerAsset(ARCHIVO_RM), RickAndMortyRespuestaDto::class.java).aPagina(1)

        assertEquals(20, pagina.personajes.size)
        assertEquals(42, pagina.totalPaginas)
        assertEquals(826, pagina.total)
        assertTrue(pagina.haySiguiente && !pagina.hayAnterior)

        val rick = pagina.personajes[0]
        assertEquals("Rick Sanchez", rick.nombre)
        assertEquals(EstadoVital.VIVO, rick.estado)
        assertEquals(Genero.MASCULINO, rick.genero)
        assertEquals("Human", rick.especie)
        assertNull("type \"\" → null", rick.subtipo)
        assertEquals("Earth (C-137)", rick.origen.nombre)
        assertEquals(1, rick.origen.id)
        assertEquals(3, rick.ubicacion.id)
        assertEquals(51, rick.episodios.size)
        assertEquals(Instant.parse("2017-11-04T18:48:46.250Z"), rick.creado)

        val morty = pagina.personajes[1]
        assertNull("origen unknown → null", morty.origen.nombre)
        assertNull("origen sin URL → sin id", morty.origen.id)
        assertTrue(pagina.personajes.any { it.estado == EstadoVital.MUERTO })
    }

    @Test
    fun attack_on_titan_se_mapea_completo() {
        val pagina = gson.fromJson(leerAsset(ARCHIVO_AOT), AttackOnTitanRespuestaDto::class.java).aPagina(1)

        assertEquals(20, pagina.personajes.size)
        assertEquals("next_page/prev_page → páginas", 11, pagina.totalPaginas)
        assertEquals(201, pagina.total)

        val armin = pagina.personajes[0]
        assertEquals("Armin Arlelt", armin.nombre)
        assertEquals("age numérico (19) → Gson lo da como \"19\" → 19", 19, armin.edad)
        assertEquals(listOf("Colossal Titan"), armin.alias)
        assertEquals(listOf("Human", "Intelligent Titan"), armin.especies)
        assertEquals("Scout Regiment", armin.grupos[0].nombre)
        assertEquals("sub_groups → subgrupos", listOf("Special Operations Squad"), armin.grupos[0].subgrupos)
        assertEquals(3, armin.familias[0].miembros.size)

        val mikasa = pagina.personajes[1]
        assertTrue("familiar como URL → id", mikasa.familias.any { 165 in it.idsPersonajes })
        assertTrue(pagina.personajes.flatMap { it.familias }.flatMap { it.miembros }.none { it.startsWith("http") })

        assertTrue("hay uno sin 'img'", pagina.personajes.any { it.imagenUrl == null })
        assertTrue("edad 'unknown' → null", pagina.personajes.any { it.edad == null })
        assertTrue("'Deceased' → MUERTO", pagina.personajes.any { it.estado == EstadoVital.MUERTO })
    }

    @Test
    fun attack_on_titan_sin_resultados_no_trae_results() {
        val json = """{"info":{"count":0,"pages":0,"next_page":null,"prev_page":null}}"""
        val pagina = gson.fromJson(json, AttackOnTitanRespuestaDto::class.java).aPagina(1)
        assertTrue(pagina.personajes.isEmpty())
        assertTrue(!pagina.haySiguiente && !pagina.hayAnterior)
    }

    @Test
    fun limpieza_de_textos() {
        assertEquals("Liberio", "Liberio [2][3][4]".limpio())
        assertEquals("Wall Rose", "Wall Rose [1]".limpio())
        assertNull("unknown".conocido())
        assertNull("".conocido())
        assertEquals(Genero.AMBIGUO, generoDesdeApi("Ambiguous[9]"))
        assertEquals(EstadoVital.MUERTO, estadoDesdeApi("Deceased"))
        assertEquals(EstadoVital.MUERTO, estadoDesdeApi("Dead"))
        assertEquals(EstadoVital.DESCONOCIDO, estadoDesdeApi("Unknown"))
        assertEquals(28, "https://rickandmortyapi.com/api/episode/28".idAlFinal())
        assertEquals(3, "https://pokeapi.co/api/v2/pokemon/3/".idAlFinal())
        assertNull("".idAlFinal())
    }
}
