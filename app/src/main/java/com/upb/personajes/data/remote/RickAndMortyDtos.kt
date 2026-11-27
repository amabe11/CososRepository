package com.upb.personajes.data.remote

// DTOs de la Rick and Morty API: reflejan EXACTAMENTE el JSON (MANUAL-RETROFIT, paso 5).
// Todos los campos son anulables: si la API omite uno, Gson deja null y el mapeo decide qué hacer.
//
// GET https://rickandmortyapi.com/api/character?page=1&name=rick
// {
//   "info": { "count": 826, "pages": 42, "next": "...?page=2", "prev": null },
//   "results": [ { "id": 1, "name": "Rick Sanchez", "status": "Alive", ... } ]
// }

data class RickAndMortyRespuestaDto(
    val info: RickAndMortyInfoDto?,
    val results: List<RickAndMortyPersonajeDto>?,
)

data class RickAndMortyInfoDto(
    val count: Int,
    val pages: Int,
    val next: String?, // null en la última página
    val prev: String?, // null en la primera página
)

data class RickAndMortyPersonajeDto(
    val id: Int,
    val name: String?,
    val status: String?,     // "Alive" | "Dead" | "unknown"
    val species: String?,
    val type: String?,       // casi siempre "" (sin subtipo)
    val gender: String?,     // "Male" | "Female" | "Genderless" | "unknown"
    val origin: RickAndMortyLugarDto?,
    val location: RickAndMortyLugarDto?,
    val image: String?,
    val episode: List<String>?, // URLs: "https://rickandmortyapi.com/api/episode/1"
    val url: String?,
    val created: String?,    // ISO-8601: "2017-11-04T18:48:46.250Z"
)

/** `origin` y `location`. Si es desconocido llega `{"name":"unknown","url":""}`. */
data class RickAndMortyLugarDto(
    val name: String?,
    val url: String?,
)
