package mvvm.model.common;

import java.util.List;
import java.util.Objects;

/**
 * Una página de resultados, independiente de la API.
 *
 * <p>Rick and Morty pagina con {@code info.next}/{@code info.prev} y Attack on Titan
 * con {@code info.next_page}/{@code info.prev_page}; los mappers traducen ambos
 * formatos a este record.</p>
 *
 * @param items      personajes de esta página
 * @param number     número de página (empieza en 1)
 * @param totalPages total de páginas (0 si no hay resultados)
 * @param totalCount total de personajes en todas las páginas
 * @param origin     de dónde salieron los datos
 */
public record Page<T>(List<T> items, int number, int totalPages, int totalCount, DataOrigin origin) {

    public Page {
        items = List.copyOf(items);
        Objects.requireNonNull(origin, "origin");
    }

    public static <T> Page<T> empty(int number, DataOrigin origin) {
        return new Page<>(List.of(), number, 0, 0, origin);
    }

    public boolean hasNext() {
        return number < totalPages;
    }

    public boolean hasPrevious() {
        return number > 1 && totalPages > 0;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
