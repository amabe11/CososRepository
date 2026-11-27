package com.upb.personajes.data.remote

import com.upb.personajes.model.EstadoVital
import com.upb.personajes.model.Genero

// Utilidades de limpieza que comparten los mapeos de ambas APIs.

private val NOTAS_DE_LA_WIKI = Regex("""\s*\[\d+]""")

/** Quita notas al pie de la wiki ("Liberio [2][3][4]" → "Liberio") y espacios. null → "". */
internal fun String?.limpio(): String = this?.replace(NOTAS_DE_LA_WIKI, "")?.trim().orEmpty()

/** Texto limpio, o null si la API dice que es desconocido ("unknown", "Unknown" o ""). */
internal fun String?.conocido(): String? =
    limpio().takeUnless { it.isEmpty() || it.equals("unknown", ignoreCase = true) }

internal fun List<String>?.conocidos(): List<String> = orEmpty().mapNotNull { it.conocido() }

/** Id al final de una URL REST: ".../episode/28" → 28. Si no hay id, null. */
internal fun String?.idAlFinal(): Int? =
    this?.substringBefore('?')?.trimEnd('/')?.substringAfterLast('/')?.toIntOrNull()

internal fun List<String>?.idsAlFinal(): List<Int> = orEmpty().mapNotNull { it.idAlFinal() }

/** URL sin espacios, o null si viene vacía. */
internal fun String?.urlONull(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

internal fun estadoDesdeApi(texto: String?): EstadoVital = when (texto.limpio().lowercase()) {
    "alive" -> EstadoVital.VIVO
    "dead", "deceased" -> EstadoVital.MUERTO
    else -> EstadoVital.DESCONOCIDO
}

internal fun generoDesdeApi(texto: String?): Genero = when (texto.limpio().lowercase()) {
    "male" -> Genero.MASCULINO
    "female" -> Genero.FEMENINO
    "genderless" -> Genero.SIN_GENERO
    "ambiguous" -> Genero.AMBIGUO
    else -> Genero.DESCONOCIDO
}
