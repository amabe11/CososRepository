package mvvm.model.rickandmorty;

import java.util.List;

/**
 * DTOs de la Rick and Morty API ({@code GET https://rickandmortyapi.com/api/character}).
 *
 * <p>Son una copia fiel del JSON: mismos campos, mismos tipos, sin limpiar nada.
 * La limpieza ocurre al pasar a {@link RickAndMortyCharacter} en {@link RickAndMortyMapper}.</p>
 *
 * <pre>
 * {
 *   "info": { "count": 826, "pages": 42, "next": "...?page=2", "prev": null },
 *   "results": [ { "id": 1, "name": "Rick Sanchez", "status": "Alive", ... } ]
 * }
 * </pre>
 */
public final class RickAndMortyDto {

    private RickAndMortyDto() {
    }

    /** Raíz de la respuesta. */
    public record ResponseDto(InfoDto info, List<CharacterDto> results) {
    }

    /** {@code info}: paginación. {@code next}/{@code prev} pueden ser {@code null}. */
    public record InfoDto(int count, int pages, String next, String prev) {
    }

    /**
     * Un elemento de {@code results}.
     *
     * @param type    subtipo; viene como {@code ""} cuando no tiene
     * @param episode URLs de los episodios: {@code "https://rickandmortyapi.com/api/episode/1"}
     * @param url     URL del propio personaje
     * @param created fecha ISO-8601: {@code "2017-11-04T18:48:46.250Z"}
     */
    public record CharacterDto(
            int id,
            String name,
            String status,
            String species,
            String type,
            String gender,
            LocationRefDto origin,
            LocationRefDto location,
            String image,
            List<String> episode,
            String url,
            String created) {
    }

    /** {@code origin} y {@code location}. Si el lugar es desconocido: {@code {"name":"unknown","url":""}}. */
    public record LocationRefDto(String name, String url) {
    }
}
