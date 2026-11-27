package com.upb.personajes.viewmodel

import com.google.gson.Gson
import com.upb.personajes.ARCHIVO_AOT
import com.upb.personajes.ARCHIVO_RM
import com.upb.personajes.data.remote.AttackOnTitanRespuestaDto
import com.upb.personajes.data.remote.RickAndMortyRespuestaDto
import com.upb.personajes.data.remote.aPagina
import com.upb.personajes.leerAsset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Textos que el ViewModel le entrega a la pantalla. */
class PresentacionPersonajeTest {

    private val gson = Gson()

    private fun FichaUi.dato(etiqueta: String) = datos.first { it.etiqueta == etiqueta }.valor

    @Test
    fun episodios_en_rangos() {
        assertEquals("4 episodios (1–3, 5)", episodiosTexto(listOf(5, 1, 2, 3)))
        assertEquals("1 episodio (7)", episodiosTexto(listOf(7)))
        assertEquals("—", episodiosTexto(emptyList()))
    }

    @Test
    fun ficha_de_rick_and_morty() {
        val rick = gson.fromJson(leerAsset(ARCHIVO_RM), RickAndMortyRespuestaDto::class.java).aPagina(1).personajes[0]
        val ficha = rick.aFichaUi()
        assertEquals("Vivo", ficha.estado)
        assertEquals(TonoEstado.POSITIVO, ficha.tono)
        assertEquals("Human · Masculino", ficha.subtitulo)
        assertEquals("51 episodios (1–51)", ficha.dato("Episodios"))
        assertEquals("04/11/2017", ficha.dato("Alta en la API"))
    }

    @Test
    fun ficha_de_attack_on_titan() {
        val pagina = gson.fromJson(leerAsset(ARCHIVO_AOT), AttackOnTitanRespuestaDto::class.java).aPagina(1)
        val armin = pagina.personajes[0].aFichaUi()
        assertEquals("19 años", armin.dato("Edad"))
        assertEquals("Scout Regiment (Special Operations Squad)", armin.dato("Grupos"))

        val levi = pagina.personajes.first { it.nombre == "Levi Ackermann" }.aFichaUi()
        assertTrue(levi.dato("Familia").contains("personajes #165"))
        assertEquals("Sin datos", levi.dato("Altura"))
    }
}
