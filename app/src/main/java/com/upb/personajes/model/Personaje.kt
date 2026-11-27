package com.upb.personajes.model

import java.time.Instant

/** De qué API viene un personaje. */
enum class FuenteApi(val titulo: String) {
    RICK_AND_MORTY("Rick and Morty"),
    ATTACK_ON_TITAN("Attack on Titan"),
}

/**
 * Estado vital normalizado. Cada API lo escribe distinto:
 * Rick and Morty usa "Alive" / "Dead" / "unknown" y Attack on Titan "Alive" / "Deceased" / "Unknown".
 */
enum class EstadoVital { VIVO, MUERTO, DESCONOCIDO }

/** Género normalizado ("Genderless" en R&M, "Ambiguous[9]" en AoT…). */
enum class Genero { MASCULINO, FEMENINO, SIN_GENERO, AMBIGUO, DESCONOCIDO }

/**
 * Modelo del dominio: lo que la app entiende, ya limpio (MANUAL-RETROFIT, "DTO frente a modelo del dominio").
 *
 * Es `sealed`: solo existen los dos tipos de abajo. Así el ViewModel puede usar `when` sobre un
 * personaje y Kotlin obliga a contemplar las dos APIs.
 */
sealed interface Personaje {
    val fuente: FuenteApi
    val id: Int
    val nombre: String
    val estado: EstadoVital
    val genero: Genero
    val imagenUrl: String?
    val episodios: List<Int>
}

/** Personaje de Rick and Morty. Lo que en la API valía "unknown" o "" aquí es `null`. */
data class PersonajeRickAndMorty(
    override val id: Int,
    override val nombre: String,
    override val estado: EstadoVital,
    val especie: String?,
    /** Campo `type` de la API; casi siempre viene vacío. */
    val subtipo: String?,
    override val genero: Genero,
    val origen: Lugar,
    val ubicacion: Lugar,
    override val imagenUrl: String?,
    override val episodios: List<Int>,
    val creado: Instant?,
) : Personaje {
    override val fuente: FuenteApi get() = FuenteApi.RICK_AND_MORTY
}

/** Lugar de origen o ubicación. El `id` sale de la URL `.../location/3`. */
data class Lugar(val nombre: String?, val id: Int?)

/** Personaje de Attack on Titan. Lo que en la API valía "unknown" aquí es `null`. */
data class PersonajeAttackOnTitan(
    override val id: Int,
    override val nombre: String,
    override val imagenUrl: String?,
    val alias: List<String>,
    val especies: List<String>,
    override val genero: Genero,
    /** En la API llega como número (19) o como texto ("unknown"). */
    val edad: Int?,
    val altura: String?,
    val familias: List<Familia>,
    val lugarNacimiento: String?,
    val residencia: String?,
    override val estado: EstadoVital,
    val ocupacion: String?,
    val grupos: List<Grupo>,
    val roles: List<String>,
    override val episodios: List<Int>,
) : Personaje {
    override val fuente: FuenteApi get() = FuenteApi.ATTACK_ON_TITAN
}

/**
 * Una familia. En la API cada miembro puede ser un nombre ("Unnamed father") o la URL de otro
 * personaje (".../characters/165"); aquí se separan.
 */
data class Familia(val nombre: String, val miembros: List<String>, val idsPersonajes: List<Int>)

/** Un grupo ("Scout Regiment") con sus subgrupos. */
data class Grupo(val nombre: String, val subgrupos: List<String>)
