package mvvm.model.datasource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

/**
 * Lee el JSON guardado en la carpeta {@code resources/} (una copia de la página 1 de la API).
 *
 * <p>Es el respaldo cuando no hay internet. Solo contiene la página 1 y no filtra:
 * el filtro por nombre lo aplica el repositorio en memoria.</p>
 */
public final class LocalCharacterDataSource implements CharacterDataSource {

    private final String resourcePath;

    /** @param resourcePath ruta dentro del classpath, por ejemplo {@code "/characters_aot.json"} */
    public LocalCharacterDataSource(String resourcePath) {
        this.resourcePath = Objects.requireNonNull(resourcePath, "resourcePath");
    }

    @Override
    public Optional<String> fetchPage(int page, String nameQuery) throws IOException {
        if (page != 1) {
            throw new IOException("el archivo local solo contiene la página 1");
        }
        try (InputStream in = LocalCharacterDataSource.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IOException("no se encontró " + resourcePath + " en el classpath");
            }
            return Optional.of(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        }
    }
}
