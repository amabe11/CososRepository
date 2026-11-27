package mvvm.model.repository;

import mvvm.core.json.JsonException;
import mvvm.model.common.DataOrigin;
import mvvm.model.common.FictionalCharacter;
import mvvm.model.common.Page;
import mvvm.model.datasource.CharacterDataSource;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Base común de los repositorios de ambas APIs.
 *
 * <p>Flujo: pide el JSON a la API en línea → lo mapea con {@link #parsePage}.
 * Si no hay conexión y se pide la página 1, usa el archivo local y filtra por nombre en memoria.</p>
 *
 * <p>Cada API solo tiene que implementar {@link #parsePage} con su propio mapper.</p>
 */
public abstract class ApiCharacterRepository<T extends FictionalCharacter> implements CharacterRepository<T> {

    private final CharacterDataSource remote;
    private final CharacterDataSource localFallback;

    /**
     * @param remote        fuente principal (API en línea)
     * @param localFallback respaldo sin conexión; puede ser {@code null}
     */
    protected ApiCharacterRepository(CharacterDataSource remote, CharacterDataSource localFallback) {
        this.remote = Objects.requireNonNull(remote, "remote");
        this.localFallback = localFallback;
    }

    /** Convierte el JSON crudo de la API en una página del modelo de dominio. */
    protected abstract Page<T> parsePage(String json, int pageNumber, DataOrigin origin);

    @Override
    public Page<T> findCharacters(int page, String nameQuery) throws RepositoryException {
        if (page < 1) {
            throw new IllegalArgumentException("La primera página es la 1");
        }
        String query = nameQuery == null ? "" : nameQuery.strip();

        Optional<String> json;
        try {
            json = remote.fetchPage(page, query);
        } catch (IOException networkError) {
            return loadFromLocalFile(page, query, networkError);
        }

        try {
            return json.map(body -> parsePage(body, page, DataOrigin.REMOTE))
                    .orElseGet(() -> Page.empty(page, DataOrigin.REMOTE));
        } catch (JsonException e) {
            throw new RepositoryException("La API respondió algo que no se pudo mapear: " + e.getMessage(), e);
        }
    }

    private Page<T> loadFromLocalFile(int page, String query, IOException networkError) throws RepositoryException {
        String reason = "No se pudo conectar con la API (" + describe(networkError) + ")";
        if (localFallback == null) {
            throw new RepositoryException(reason, networkError);
        }
        if (page != 1) {
            throw new RepositoryException(reason + ". Sin conexión solo está disponible la página 1.", networkError);
        }
        try {
            String json = localFallback.fetchPage(1, query)
                    .orElseThrow(() -> new IOException("el archivo local está vacío"));
            Page<T> local = parsePage(json, 1, DataOrigin.LOCAL);
            List<T> matches = local.items().stream()
                    .filter(character -> nameContains(character, query))
                    .toList();
            return new Page<>(matches, 1, matches.isEmpty() ? 0 : 1, matches.size(), DataOrigin.LOCAL);
        } catch (IOException | JsonException e) {
            e.addSuppressed(networkError);
            throw new RepositoryException(reason + " y tampoco se pudo leer el archivo local: " + describe(e), e);
        }
    }

    private static boolean nameContains(FictionalCharacter character, String query) {
        return query.isEmpty()
                || character.name().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT));
    }

    private static String describe(Exception e) {
        // Varias excepciones de red de Java (p. ej. ConnectException) no traen mensaje.
        return e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
    }
}
