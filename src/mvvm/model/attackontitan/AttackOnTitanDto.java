package mvvm.model.attackontitan;

import java.util.List;

/**
 * DTOs de la Attack on Titan API ({@code GET https://api.attackontitanapi.com/characters}).
 *
 * <p>Copia fiel del JSON. Lo que se encontró al analizar las 201 fichas:</p>
 * <ul>
 *     <li>{@code img} falta en algunos personajes (5 de 201).</li>
 *     <li>{@code age} es un número ({@code 19}) o el texto {@code "unknown"}: aquí se guarda como texto.</li>
 *     <li>{@code height}, {@code birthplace}, {@code residence}, {@code occupation} suelen valer {@code "unknown"}.</li>
 *     <li>Algunos textos traen notas de la wiki: {@code "Wall Rose [1]"}, {@code "Ambiguous[9]"}.</li>
 *     <li>En {@code relatives[].members} se mezclan nombres y URLs de otros personajes.</li>
 *     <li>Sin resultados, la respuesta no trae la clave {@code results}.</li>
 * </ul>
 *
 * <pre>
 * {
 *   "info": { "count": 201, "pages": 11, "next_page": "...?page=2", "prev_page": null },
 *   "results": [ { "id": 1, "name": "Armin Arlelt", "img": "...", "alias": [...], ... } ]
 * }
 * </pre>
 */
public final class AttackOnTitanDto {

    private AttackOnTitanDto() {
    }

    /** Raíz de la respuesta. */
    public record ResponseDto(InfoDto info, List<CharacterDto> results) {
    }

    /** {@code info}. En el JSON los campos se llaman {@code next_page} y {@code prev_page}. */
    public record InfoDto(int count, int pages, String nextPage, String prevPage) {
    }

    /**
     * Un elemento de {@code results}.
     *
     * @param img      URL de la imagen; {@code null} si el JSON no la trae
     * @param age      edad como texto: {@code "19"} o {@code "unknown"}
     * @param episodes URLs: {@code "https://api.attackontitanapi.com/episodes/1"}
     */
    public record CharacterDto(
            int id,
            String name,
            String img,
            List<String> alias,
            List<String> species,
            String gender,
            String age,
            String height,
            List<RelativeDto> relatives,
            String birthplace,
            String residence,
            String status,
            String occupation,
            List<GroupDto> groups,
            List<String> roles,
            List<String> episodes) {
    }

    /**
     * Elemento de {@code relatives}: {@code {"family": "Arlelt family", "members": [...]}}.
     * Cada miembro es un nombre o la URL de otro personaje: {@code ".../characters/165"}.
     */
    public record RelativeDto(String family, List<String> members) {
    }

    /** Elemento de {@code groups}: {@code {"name": "Scout Regiment", "sub_groups": [...]}}. */
    public record GroupDto(String name, List<String> subGroups) {
    }
}
