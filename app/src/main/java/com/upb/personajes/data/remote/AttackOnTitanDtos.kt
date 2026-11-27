package com.upb.personajes.data.remote

import com.google.gson.annotations.SerializedName

// DTOs de la Attack on Titan API: reflejan EXACTAMENTE el JSON (MANUAL-RETROFIT, paso 5).
//
// GET https://api.attackontitanapi.com/characters?page=1&name=eren
// {
//   "info": { "count": 201, "pages": 11, "next_page": "...?page=2", "prev_page": null },
//   "results": [ { "id": 1, "name": "Armin Arlelt", "img": "...", "age": 19, ... } ]
// }
//
// Particularidades encontradas al analizar las 201 fichas:
// - Sin resultados, la respuesta NO trae "results" (por eso es anulable).
// - "img" falta en 5 personajes.
// - "age" es un número (19) o el texto "unknown".
// - Varios textos traen notas de la wiki: "Wall Rose [1]", "Ambiguous[9]".
// - relatives[].members mezcla nombres y URLs de otros personajes.

data class AttackOnTitanRespuestaDto(
    val info: AttackOnTitanInfoDto?,
    val results: List<AttackOnTitanPersonajeDto>?,
)

data class AttackOnTitanInfoDto(
    val count: Int,
    val pages: Int,
    // La API usa snake_case y Kotlin camelCase: @SerializedName los une (manual, paso 5).
    @SerializedName("next_page") val nextPage: String?,
    @SerializedName("prev_page") val prevPage: String?,
)

data class AttackOnTitanPersonajeDto(
    val id: Int,
    val name: String?,
    val img: String?,
    val alias: List<String>?,
    val species: List<String>?,
    val gender: String?,
    // OJO: llega como número (19) o como texto ("unknown"). Gson convierte el número a "19"
    // si el campo es String, así que se recibe siempre como texto y el mapeo lo pasa a Int.
    val age: String?,
    val height: String?,
    val relatives: List<AttackOnTitanFamiliaDto>?,
    val birthplace: String?,
    val residence: String?,
    val status: String?,     // "Alive" | "Deceased" | "Unknown"
    val occupation: String?,
    val groups: List<AttackOnTitanGrupoDto>?,
    val roles: List<String>?,
    val episodes: List<String>?, // URLs: "https://api.attackontitanapi.com/episodes/1"
)

data class AttackOnTitanFamiliaDto(
    val family: String?,
    val members: List<String>?, // "Unnamed father" o "https://api.attackontitanapi.com/characters/165"
)

data class AttackOnTitanGrupoDto(
    val name: String?,
    @SerializedName("sub_groups") val subGroups: List<String>?,
)
