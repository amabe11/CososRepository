package com.upb.personajes.viewmodel

import com.upb.personajes.model.EstadoVital
import com.upb.personajes.model.Familia
import com.upb.personajes.model.FuenteApi
import com.upb.personajes.model.Genero
import com.upb.personajes.model.Personaje
import com.upb.personajes.model.PersonajeAttackOnTitan
import com.upb.personajes.model.PersonajeRickAndMorty
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

// Convierte el modelo del dominio en textos listos para mostrar.
// Así la pantalla solo dibuja: no decide etiquetas, idiomas ni formatos.

/** Intención visual del estado; la pantalla elige el color. */
enum class TonoEstado { POSITIVO, NEGATIVO, NEUTRO }

/** Una fila de la lista. */
data class PersonajeUi(
    val fuente: FuenteApi,
    val id: Int,
    val nombre: String,
    val subtitulo: String,
    val estado: String,
    val tono: TonoEstado,
    val imagenUrl: String?,
)

/** La ficha completa de un personaje. */
data class FichaUi(
    val nombre: String,
    val subtitulo: String,
    val estado: String,
    val tono: TonoEstado,
    val imagenUrl: String?,
    val datos: List<DatoUi>,
)

/** Un dato de la ficha: "Especie" → "Human". */
data class DatoUi(val etiqueta: String, val valor: String)

private const val SIN_DATOS = "Sin datos"
private const val NINGUNO = "—"
private val FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneOffset.UTC)

fun Personaje.aItemUi() = PersonajeUi(
    fuente = fuente,
    id = id,
    nombre = nombre,
    subtitulo = subtitulo(),
    estado = estado.texto(),
    tono = estado.tono(),
    imagenUrl = imagenUrl,
)

fun Personaje.aFichaUi() = FichaUi(
    nombre = nombre,
    subtitulo = subtitulo(),
    estado = estado.texto(),
    tono = estado.tono(),
    imagenUrl = imagenUrl,
    // `when` sobre una interfaz sellada: si se agrega otra API, Kotlin obliga a contemplarla aquí.
    datos = when (this) {
        is PersonajeRickAndMorty -> datos()
        is PersonajeAttackOnTitan -> datos()
    },
)

private fun PersonajeRickAndMorty.datos() = listOf(
    DatoUi("ID", "#$id"),
    DatoUi("Especie", especie ?: SIN_DATOS),
    DatoUi("Tipo", subtipo ?: NINGUNO),
    DatoUi("Género", genero.texto()),
    DatoUi("Origen", origen.nombre ?: SIN_DATOS),
    DatoUi("Última ubicación", ubicacion.nombre ?: SIN_DATOS),
    DatoUi("Episodios", episodiosTexto(episodios)),
    DatoUi("Alta en la API", creado?.let(FECHA::format) ?: SIN_DATOS),
)

private fun PersonajeAttackOnTitan.datos() = listOf(
    DatoUi("ID", "#$id"),
    DatoUi("Alias", alias.unidos()),
    DatoUi("Especie", especies.joinToString(", ").ifEmpty { SIN_DATOS }),
    DatoUi("Género", genero.texto()),
    DatoUi("Edad", edad?.let { "$it años" } ?: SIN_DATOS),
    DatoUi("Altura", altura ?: SIN_DATOS),
    DatoUi("Lugar de nacimiento", lugarNacimiento ?: SIN_DATOS),
    DatoUi("Residencia", residencia ?: SIN_DATOS),
    DatoUi("Ocupación", ocupacion ?: SIN_DATOS),
    DatoUi("Grupos", grupos.joinToString("; ") { g ->
        if (g.subgrupos.isEmpty()) g.nombre else "${g.nombre} (${g.subgrupos.joinToString(", ")})"
    }.ifEmpty { NINGUNO }),
    DatoUi("Roles", roles.unidos()),
    DatoUi("Familia", familias.joinToString("\n") { it.texto() }.ifEmpty { NINGUNO }),
    DatoUi("Episodios", episodiosTexto(episodios)),
)

private fun Personaje.subtitulo(): String {
    val especie = when (this) {
        is PersonajeRickAndMorty -> especie
        is PersonajeAttackOnTitan -> especies.joinToString(", ").ifEmpty { null }
    }
    return "${especie ?: "Especie desconocida"} · ${genero.texto()}"
}

/** "Ackermann family: Unnamed relative · personajes #165, #57" */
private fun Familia.texto(): String {
    val partes = buildList {
        if (miembros.isNotEmpty()) add(miembros.joinToString(", "))
        if (idsPersonajes.isNotEmpty()) {
            add((if (idsPersonajes.size == 1) "personaje " else "personajes ") + idsPersonajes.joinToString(", ") { "#$it" })
        }
    }
    return if (partes.isEmpty()) nombre else "$nombre: ${partes.joinToString(" · ")}"
}

private fun List<String>.unidos() = joinToString(", ").ifEmpty { NINGUNO }

fun EstadoVital.texto() = when (this) {
    EstadoVital.VIVO -> "Vivo"
    EstadoVital.MUERTO -> "Muerto"
    EstadoVital.DESCONOCIDO -> "Desconocido"
}

fun EstadoVital.tono() = when (this) {
    EstadoVital.VIVO -> TonoEstado.POSITIVO
    EstadoVital.MUERTO -> TonoEstado.NEGATIVO
    EstadoVital.DESCONOCIDO -> TonoEstado.NEUTRO
}

fun Genero.texto() = when (this) {
    Genero.MASCULINO -> "Masculino"
    Genero.FEMENINO -> "Femenino"
    Genero.SIN_GENERO -> "Sin género"
    Genero.AMBIGUO -> "Ambiguo"
    Genero.DESCONOCIDO -> "Género desconocido"
}

/** [1,2,3,5] → "4 episodios (1–3, 5)" */
fun episodiosTexto(ids: List<Int>): String {
    if (ids.isEmpty()) return NINGUNO
    val ordenados = ids.distinct().sorted()
    val rangos = mutableListOf<String>()
    var inicio = ordenados.first()
    var anterior = inicio
    for (actual in ordenados.drop(1) + listOf<Int?>(null)) {
        if (actual != null && actual == anterior + 1) {
            anterior = actual
            continue
        }
        rangos += if (inicio == anterior) "$inicio" else "$inicio–$anterior"
        if (actual != null) {
            inicio = actual
            anterior = actual
        }
    }
    val n = ordenados.size
    return "$n ${if (n == 1) "episodio" else "episodios"} (${rangos.joinToString(", ")})"
}
