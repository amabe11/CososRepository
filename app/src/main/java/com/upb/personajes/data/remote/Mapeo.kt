package com.upb.personajes.data.remote

import com.upb.personajes.model.Familia
import com.upb.personajes.model.Grupo
import com.upb.personajes.model.Lugar
import com.upb.personajes.model.OrigenDatos
import com.upb.personajes.model.Pagina
import com.upb.personajes.model.PersonajeAttackOnTitan
import com.upb.personajes.model.PersonajeRickAndMorty
import java.time.Instant
import java.time.format.DateTimeParseException

// Mapeo DTO → modelo del dominio (MANUAL-RETROFIT, "DTO frente a modelo del dominio").
// Si una API cambia su JSON, solo se toca este archivo y su DTO, no el resto de la app.

// ─────────────────────────────── Rick and Morty

fun RickAndMortyPersonajeDto.aModelo(): PersonajeRickAndMorty = PersonajeRickAndMorty(
    id = id,
    nombre = name.limpio(),
    estado = estadoDesdeApi(status),
    especie = species.conocido(),
    subtipo = type.conocido(),
    genero = generoDesdeApi(gender),
    origen = origin.aLugar(),
    ubicacion = location.aLugar(),
    imagenUrl = image.urlONull(),
    episodios = episode.idsAlFinal(),
    creado = created.aInstante(),
)

private fun RickAndMortyLugarDto?.aLugar() = Lugar(nombre = this?.name.conocido(), id = this?.url.idAlFinal())

private fun String?.aInstante(): Instant? = try {
    this?.let(Instant::parse)
} catch (e: DateTimeParseException) {
    null
}

fun RickAndMortyRespuestaDto.aPagina(numero: Int, origen: OrigenDatos = OrigenDatos.API): Pagina<PersonajeRickAndMorty> =
    Pagina(
        personajes = results.orEmpty().map { it.aModelo() },
        numero = numero,
        totalPaginas = info?.pages ?: 0,
        total = info?.count ?: 0,
        origen = origen,
    )

// ─────────────────────────────── Attack on Titan

fun AttackOnTitanPersonajeDto.aModelo(): PersonajeAttackOnTitan = PersonajeAttackOnTitan(
    id = id,
    nombre = name.limpio(),
    imagenUrl = img.urlONull(),
    alias = alias.conocidos(),
    especies = species.conocidos(),
    genero = generoDesdeApi(gender),
    edad = age.conocido()?.toIntOrNull(), // "19" → 19, "unknown" → null
    altura = height.conocido(),
    familias = relatives.orEmpty().map { it.aFamilia() },
    lugarNacimiento = birthplace.conocido(),
    residencia = residence.conocido(),
    estado = estadoDesdeApi(status),
    ocupacion = occupation.conocido(),
    grupos = groups.orEmpty().map { Grupo(nombre = it.name.limpio(), subgrupos = it.subGroups.conocidos()) },
    roles = roles.conocidos(),
    episodios = episodes.idsAlFinal(),
)

/** Separa los miembros que son URL de otro personaje de los que son un nombre. */
private fun AttackOnTitanFamiliaDto.aFamilia(): Familia {
    val (urls, nombres) = members.orEmpty().partition { it.startsWith("http") }
    return Familia(
        nombre = family.limpio(),
        miembros = nombres.conocidos(),
        idsPersonajes = urls.idsAlFinal(),
    )
}

fun AttackOnTitanRespuestaDto.aPagina(numero: Int, origen: OrigenDatos = OrigenDatos.API): Pagina<PersonajeAttackOnTitan> =
    Pagina(
        personajes = results.orEmpty().map { it.aModelo() }, // sin resultados no viene "results"
        numero = numero,
        totalPaginas = info?.pages ?: 0,
        total = info?.count ?: 0,
        origen = origen,
    )
